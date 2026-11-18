package com.minicommerce.service.impl;

import com.minicommerce.entity.Product;
import com.minicommerce.entity.ProductVariant;
import com.minicommerce.exception.InsufficientStockException;
import com.minicommerce.exception.ProductNotFoundException;
import com.minicommerce.repository.ProductRepository;
import com.minicommerce.service.InventoryService;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class InventoryServiceImpl implements InventoryService {
    private final ProductRepository productRepository;

    /**
     * [Session 6] Một lock chung cho mọi thao tác đọc-sửa-ghi tồn kho.
     * Không có lock: 2 thread cùng đọc stock = 1, cùng thấy "đủ hàng", cùng trừ -> bán vượt tồn kho
     * (race condition kiểu check-then-act).
     * Dùng ReentrantLock (thay vì synchronized) vì decreaseStock gọi lại checkStock trong cùng thread,
     * và để minh hoạ lock()/unlock() trong try/finally.
     */
    private final Lock stockLock = new ReentrantLock();

    public InventoryServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    private ProductVariant findVariant(String sku) {
        Product product = productRepository.findBySku(sku)
                // [Session 5 - Exception] Optional.orElseThrow: không thấy thì ném exception thay vì trả null.
                .orElseThrow(() -> new ProductNotFoundException("SKU not found: " + sku));
        ProductVariant variant = product.findVariantBySku(sku);
        if (variant == null) throw new ProductNotFoundException("SKU not found: " + sku);
        return variant;
    }

    @Override
    // [Session 5 - Exception] Service THROW exception nghiệp vụ (InsufficientStockException), controller CATCH và báo người dùng.
    // [Session 5 - Exception] try/finally bên dưới: luôn unlock kể cả khi có exception.
    public void checkStock(String sku, int requestedQuantity) {
        if (requestedQuantity <= 0) throw new IllegalArgumentException("Quantity must be greater than zero.");
        stockLock.lock();
        try {
            ProductVariant variant = findVariant(sku);
            if (variant.getStockQuantity() < requestedQuantity)
                throw new InsufficientStockException("Not enough stock for " + sku + ". Available: " + variant.getStockQuantity());
        } finally {
            stockLock.unlock();
        }
    }

    @Override
    public void decreaseStock(String sku, int quantity) {
        stockLock.lock();
        try {
            checkStock(sku, quantity);          // reentrant: cùng thread lấy lại lock được
            ProductVariant variant = findVariant(sku);
            variant.setStockQuantity(variant.getStockQuantity() - quantity);
        } finally {
            stockLock.unlock();
        }
    }

    @Override
    public void increaseStock(String sku, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be greater than zero.");
        stockLock.lock();
        try {
            ProductVariant variant = findVariant(sku);
            variant.setStockQuantity(variant.getStockQuantity() + quantity);
        } finally {
            stockLock.unlock();
        }
    }

    @Override
    public void restock(String sku, int quantity) { increaseStock(sku, quantity); }

    @Override
    public void reserveAll(Map<String, Integer> quantitiesBySku) {
        stockLock.lock();
        try {
            // Bước 1: kiểm tra tất cả trước. Bước 2: mới trừ. Cả 2 bước nằm trong cùng một lock
            // nên không thread nào chen vào giữa được.
            quantitiesBySku.forEach(this::checkStock);
            quantitiesBySku.forEach((sku, qty) -> {
                ProductVariant variant = findVariant(sku);
                variant.setStockQuantity(variant.getStockQuantity() - qty);
            });
        } finally {
            stockLock.unlock();
        }
    }

    @Override
    public void releaseAll(Map<String, Integer> quantitiesBySku) {
        stockLock.lock();
        try {
            quantitiesBySku.forEach(this::increaseStock);
        } finally {
            stockLock.unlock();
        }
    }

    @Override
    public List<ProductVariant> findLowStockVariants(int threshold) {
        // [Lambda/Stream] flatMap: List<Product> -> Stream<ProductVariant>
        return productRepository.findAll().stream()
                .filter(Product::isActive)
                .flatMap(product -> product.getVariants().stream())
                .filter(variant -> variant.getStockQuantity() <= threshold)
                .sorted(Comparator.comparingInt(ProductVariant::getStockQuantity)
                        .thenComparing(ProductVariant::getSku))
                .toList();
    }
}
