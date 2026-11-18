package com.minicommerce.entity;

public class CartItem {
    private String sku;
    private int quantity;

    public CartItem() {}
    public CartItem(String sku, int quantity) {
        this.sku = sku;
        setQuantity(quantity);
    }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public int getQuantity() { return quantity; }
    // [Session 3 - OOP] Encapsulation: validate trong setter, constructor cũng gọi setter để dùng chung luật.
    public void setQuantity(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be greater than zero.");
        this.quantity = quantity;
    }
}
