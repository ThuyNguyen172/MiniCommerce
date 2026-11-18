package com.minicommerce.entity;

import com.minicommerce.entity.enums.ProductColor;
import com.minicommerce.entity.enums.ProductSize;

public class ProductVariant {
    private String sku;
    private ProductSize size;
    private ProductColor color;
    private long price;
    // [Session 6] volatile: giá trị mới ghi bởi thread này luôn được thread khác đọc thấy ngay.
    // (Việc đọc-sửa-ghi vẫn phải nằm trong lock của InventoryServiceImpl.)
    private volatile int stockQuantity;

    public ProductVariant() {}

    public ProductVariant(String sku, ProductSize size, ProductColor color, long price, int stockQuantity) {
        this.sku = sku;
        this.size = size;
        this.color = color;
        setPrice(price);
        setStockQuantity(stockQuantity);
    }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public ProductSize getSize() { return size; }
    public void setSize(ProductSize size) { this.size = size; }
    public ProductColor getColor() { return color; }
    public void setColor(ProductColor color) { this.color = color; }
    public long getPrice() { return price; }
    // [Session 3 - OOP] Encapsulation: setter kiểm tra dữ liệu hợp lệ -> object không bao giờ ở trạng thái sai (giá <= 0, kho âm).
    public void setPrice(long price) {
        if (price <= 0) throw new IllegalArgumentException("Price must be greater than zero.");
        this.price = price;
    }
    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) {
        if (stockQuantity < 0) throw new IllegalArgumentException("Stock cannot be negative.");
        this.stockQuantity = stockQuantity;
    }

    @Override
    public String toString() {
        return sku + " | " + size + " | " + color + " | " + String.format("%,d VND", price)
                + " | stock: " + stockQuantity;
    }
}
