package com.minicommerce.service.impl;

import com.minicommerce.entity.Product;
import com.minicommerce.entity.ProductVariant;
import com.minicommerce.entity.enums.Category;
import com.minicommerce.exception.ProductNotFoundException;
import com.minicommerce.repository.ProductRepository;
import com.minicommerce.service.ProductService;
import com.minicommerce.util.ProductFilters;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Product getProductById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + id));
    }

    @Override
    public Product getProductBySku(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ProductNotFoundException("SKU not found: " + sku));
        if (!product.isActive()) throw new ProductNotFoundException("Product is inactive: " + product.getId());
        return product;
    }

    @Override
    public List<Product> getAllActiveProducts() {
        return productRepository.findAll().stream()
                .filter(ProductFilters.active())
                .toList();
    }

    // [Lambda/Stream] searchProducts và getProductsByCategory giờ chỉ là các trường hợp đặc biệt của filterProducts.
    @Override
    public List<Product> searchProducts(String keyword) {
        return filterProducts(ProductFilters.nameOrIdContains(keyword), Comparator.comparing(Product::getId));
    }

    @Override
    public List<Product> getProductsByCategory(Category category) {
        return filterProducts(ProductFilters.inCategory(category), Comparator.comparing(Product::getId));
    }

    @Override
    public List<Product> filterProducts(Predicate<Product> criteria, Comparator<Product> sortOrder) {
        return productRepository.findAll().stream()
                .filter(ProductFilters.active().and(criteria))   // kết hợp Predicate
                .sorted(sortOrder)
                .toList();
    }

    @Override
    public void addProduct(Product product) {
        if (product == null || product.getId() == null || product.getId().isBlank())
            // [Session 5 - Exception] Fail-fast: validate đầu vào, ném IllegalArgumentException (exception có sẵn của Java).
            throw new IllegalArgumentException("Product and product ID are required.");
        if (productRepository.findById(product.getId()).isPresent())
            throw new IllegalArgumentException("Product ID already exists: " + product.getId());
        // [Lambda/Stream] Thay vòng for bằng stream: tìm SKU đầu tiên đã tồn tại (nếu có) thì báo lỗi.
        product.getVariants().stream()
                .map(ProductVariant::getSku)
                .filter(sku -> productRepository.findBySku(sku).isPresent())
                .findFirst()
                .ifPresent(sku -> { throw new IllegalArgumentException("SKU already exists: " + sku); });
        productRepository.save(product);
    }

    @Override
    public void updateProduct(Product product) {
        if (product == null || product.getId() == null || productRepository.findById(product.getId()).isEmpty())
            throw new ProductNotFoundException("Cannot update a product that does not exist.");
        productRepository.save(product);
    }

    @Override
    public void deactivateProduct(String id) {
        Product product = getProductById(id);
        product.setActive(false);
        productRepository.save(product);
    }

    @Override
    public void addVariant(String productId, ProductVariant variant) {
        Product product = getProductById(productId);
        if (productRepository.findBySku(variant.getSku()).isPresent())
            throw new IllegalArgumentException("SKU already exists: " + variant.getSku());
        product.addVariant(variant);
        productRepository.save(product);
    }
}
