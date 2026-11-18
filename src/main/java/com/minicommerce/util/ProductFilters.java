package com.minicommerce.util;

import com.minicommerce.entity.Product;
import com.minicommerce.entity.enums.Category;
import com.minicommerce.entity.enums.ProductSize;
import java.util.function.Predicate;

/**
 * [Lambda/Stream] Factory tạo các Predicate<Product> có thể kết hợp với nhau
 * bằng and() / or() / negate() thay vì viết nhiều hàm search riêng lẻ.
 */
public final class ProductFilters {
    private ProductFilters() {}

    public static Predicate<Product> active() {
        return Product::isActive;                       // method reference
    }

    public static Predicate<Product> nameOrIdContains(String keyword) {
        String key = keyword == null ? "" : keyword.trim().toLowerCase();
        return p -> p.getName().toLowerCase().contains(key)
                || p.getId().toLowerCase().contains(key);  // lambda capture biến "effectively final"
    }

    public static Predicate<Product> inCategory(Category category) {
        return p -> p.getCategory() == category;
    }

    public static Predicate<Product> inStock() {
        return p -> p.getTotalStock() > 0;
    }

    /** Sản phẩm có ít nhất một biến thể giá <= maxPrice. */
    public static Predicate<Product> minPriceAtMost(long maxPrice) {
        return p -> p.getMinPrice().stream().anyMatch(price -> price <= maxPrice);
    }

    public static Predicate<Product> hasSize(ProductSize size) {
        return p -> p.getVariants().stream().anyMatch(v -> v.getSize() == size);
    }
}
