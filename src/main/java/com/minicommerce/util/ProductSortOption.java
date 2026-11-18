package com.minicommerce.util;

import com.minicommerce.entity.Product;
import java.util.Comparator;

/**
 * [Lambda/Stream] Mỗi cách sắp xếp là một Comparator được tạo bằng
 * Comparator.comparing(...) / thenComparing(...) / reversed().
 */
// [Session 3 - OOP] enum có field + constructor + method: mỗi hằng mang theo hành vi riêng (Comparator).
public enum ProductSortOption {
    NAME_ASC(Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER)),
    PRICE_ASC(Comparator.comparingLong(ProductSortOption::minPriceOrMax)
            .thenComparing(Product::getName)),
    PRICE_DESC(Comparator.comparingLong(ProductSortOption::minPriceOrMax).reversed()
            .thenComparing(Product::getName)),
    STOCK_DESC(Comparator.comparingInt(Product::getTotalStock).reversed());

    private final Comparator<Product> comparator;

    ProductSortOption(Comparator<Product> comparator) { this.comparator = comparator; }

    public Comparator<Product> comparator() { return comparator; }

    private static long minPriceOrMax(Product product) {
        return product.getMinPrice().orElse(Long.MAX_VALUE);
    }
}
