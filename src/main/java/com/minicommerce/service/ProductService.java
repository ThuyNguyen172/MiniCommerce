package com.minicommerce.service;

import com.minicommerce.entity.Product;
import com.minicommerce.entity.ProductVariant;
import com.minicommerce.entity.enums.Category;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public interface ProductService {
    Product getProductById(UUID id);
    Product getProductBySku(String sku);
    List<Product> getAllActiveProducts();
    List<Product> searchProducts(String keyword);
    List<Product> getProductsByCategory(Category category);
    /** [Lambda/Stream] Lọc sản phẩm đang bán theo điều kiện bất kỳ và sắp xếp theo comparator truyền vào. */
    // [Session 4 - Generic] Tham số là interface generic Predicate<Product>, Comparator<Product>.
    List<Product> filterProducts(Predicate<Product> criteria, Comparator<Product> sortOrder);
    void addProduct(Product product);
    void updateProduct(Product product);
    void deactivateProduct(UUID id);
    void addVariant(UUID productId, ProductVariant variant);
}
