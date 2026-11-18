package com.minicommerce.entity;

import com.minicommerce.entity.enums.ProductColor;
import com.minicommerce.entity.enums.ProductSize;

public class OrderItem {
    private String sku;
    private String productName;
    private ProductSize size;
    private ProductColor color;
    private long unitPrice;
    private int quantity;

    public OrderItem() {}
    public OrderItem(String sku, String productName, ProductSize size, ProductColor color,
                     long unitPrice, int quantity) {
        this.sku = sku;
        this.productName = productName;
        this.size = size;
        this.color = color;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public String getSku() { return sku; }
    public String getProductName() { return productName; }
    public ProductSize getSize() { return size; }
    public ProductColor getColor() { return color; }
    public long getUnitPrice() { return unitPrice; }
    public int getQuantity() { return quantity; }
    public long getSubtotal() { return unitPrice * quantity; }
}
