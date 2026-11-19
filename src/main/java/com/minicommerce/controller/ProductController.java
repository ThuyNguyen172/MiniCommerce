package com.minicommerce.controller;

import com.minicommerce.entity.Product;
import com.minicommerce.entity.enums.Category;
import com.minicommerce.entity.enums.ProductSize;
import com.minicommerce.service.ProductService;
import com.minicommerce.util.InputHelper;
import com.minicommerce.util.ProductFilters;
import com.minicommerce.util.ProductSortOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) { this.productService = productService; }

    public void listProducts() {
        System.out.println("\n--- Products ---");
        productService.getAllActiveProducts().forEach(this::printProductWithVariants);   // method reference
    }

    public void showProductDetails() {
        UUID id = UUID.fromString(InputHelper.readString("Product ID: "));
        try {
            Product product = productService.getProductById(id);
            System.out.println(product);
            System.out.println(product.getDescription());
            product.getVariants().forEach(variant -> System.out.println("  " + variant));
        } catch (RuntimeException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }

    /**
     * [Lambda/Stream] Tìm kiếm & lọc nâng cao: mỗi tiêu chí người dùng nhập là một Optional<Predicate>.
     * Gom các Predicate có mặt lại bằng reduce(Predicate::and) thành một điều kiện duy nhất.
     */
    public void searchAndFilter() {
        System.out.println("\n--- Search & filter (leave blank to skip a filter) ---");
        String keyword = InputHelper.readString("Keyword (name or ID): ");
        Optional<Category> category = InputHelper.readOptionalEnum(
                "Category (" + InputHelper.optionsOf(Category.class) + "): ", Category.class);
        Optional<ProductSize> size = InputHelper.readOptionalEnum(
                "Size (" + InputHelper.optionsOf(ProductSize.class) + "): ", ProductSize.class);
        Optional<Long> maxPrice = InputHelper.readOptionalLong("Max price in VND: ");
        boolean onlyInStock = InputHelper.readYesNo("Only in-stock products? (y/n): ");
        ProductSortOption sort = InputHelper.readOptionalEnum(
                "Sort by (" + InputHelper.optionsOf(ProductSortOption.class) + ") [default NAME_ASC]: ",
                ProductSortOption.class).orElse(ProductSortOption.NAME_ASC);

        Predicate<Product> criteria = Stream.of(
                        Optional.of(keyword).filter(k -> !k.isBlank()).map(ProductFilters::nameOrIdContains),
                        category.map(ProductFilters::inCategory),
                        size.map(ProductFilters::hasSize),
                        maxPrice.map(ProductFilters::minPriceAtMost),
                        onlyInStock ? Optional.of(ProductFilters.inStock()) : Optional.<Predicate<Product>>empty())
                .flatMap(Optional::stream)                     // bỏ các tiêu chí rỗng
                .reduce(product -> true, Predicate::and);      // AND tất cả lại

        List<Product> results = productService.filterProducts(criteria, sort.comparator());
        if (results.isEmpty()) {
            System.out.println("No products match your filters.");
            return;
        }
        System.out.println("Found " + results.size() + " product(s):");
        results.forEach(this::printProductWithVariants);
    }

    private void printProductWithVariants(Product product) {
        String priceFrom = product.getMinPrice().stream()
                .mapToObj(price -> String.format(" | from %,d VND", price))
                .findFirst().orElse("");
        System.out.println(product + priceFrom);
        product.getVariants().forEach(variant -> System.out.println("   " + variant));
    }
}
