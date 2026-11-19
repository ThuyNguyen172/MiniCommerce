package com.minicommerce.entity;

import com.minicommerce.entity.enums.Category;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;
import java.util.UUID;

// [Session 3 - OOP] Encapsulation: field private, truy cập qua getter/setter. Composition: Product "has-a" List<ProductVariant>.
public class Product {
    private UUID id;
    private String name;
    private Category category;
    private String description;
    private boolean active = true;
    // [Session 4 - Collection/Generic] Khai báo theo interface List<E>, khởi tạo ArrayList (diamond <>).
    private List<ProductVariant> variants = new ArrayList<>();

    public Product() {}

    public Product(UUID id, String name, Category category, String description) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.description = description;
        this.active = true;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public List<ProductVariant> getVariants() { return variants; }
    public void setVariants(List<ProductVariant> variants) {
        this.variants = variants == null ? new ArrayList<>() : new ArrayList<>(variants);
    }
    public void addVariant(ProductVariant variant) {
        if (variant == null) throw new IllegalArgumentException("Variant cannot be null.");
        variants.add(variant);
    }
    public ProductVariant findVariantBySku(String sku) {
        return variants.stream()
                .filter(v -> v.getSku().equalsIgnoreCase(sku))
                .findFirst().orElse(null);
    }

    /** [Lambda/Stream] Giá thấp nhất trong các biến thể (rỗng nếu chưa có biến thể). */
    public OptionalLong getMinPrice() {
        return variants.stream().mapToLong(ProductVariant::getPrice).min();
    }

    /** [Lambda/Stream] Tổng tồn kho của tất cả biến thể. */
    public int getTotalStock() {
        return variants.stream().mapToInt(ProductVariant::getStockQuantity).sum();
    }

    @Override
    public String toString() {
        return id + " | " + name + " | " + category + (active ? "" : " | INACTIVE");
    }
}
