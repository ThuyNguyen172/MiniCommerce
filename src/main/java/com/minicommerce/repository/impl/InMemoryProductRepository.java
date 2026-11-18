package com.minicommerce.repository.impl;

import com.minicommerce.entity.Product;
import com.minicommerce.repository.ProductRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

// [Session 3 - OOP] Polymorphism: class implements interface ProductRepository.
public class InMemoryProductRepository implements ProductRepository {
    // [Session 6] ConcurrentHashMap thay cho LinkedHashMap để đọc/ghi an toàn từ nhiều thread.
    // [Session 5 - Map] Map<id, Product>: tra cứu theo ID O(1) thay vì duyệt List.
    // Map<UUID, Product>
    private final Map<String, Product> products = new ConcurrentHashMap<>();

    @Override
    public Optional<Product> findById(String id) {
        return Optional.ofNullable(products.get(id));
    }

    @Override
    public Optional<Product> findBySku(String sku) {
        // [Lambda/Stream] Bản cũ flatMap ra variant rồi lại stream lại products để tìm ngược -> O(n^2).
        // Giờ chỉ cần lọc product có biến thể khớp SKU bằng anyMatch.
        return products.values().stream()
                .filter(product -> product.getVariants().stream()
                        .anyMatch(variant -> variant.getSku().equalsIgnoreCase(sku)))
                .findFirst();
    }

    @Override
    public List<Product> findAll() {
        // [Session 6] ConcurrentHashMap không giữ thứ tự chèn -> sắp xếp theo ID để output ổn định.
        return products.values().stream()
                .sorted(Comparator.comparing(Product::getId))
                .toList();
    }

    @Override
    public void save(Product product) {
        products.put(product.getId(), product);
    }

    @Override
    public void deleteById(String id) {
        products.remove(id);
    }
}
