package com.minicommerce.entity;

import java.util.LinkedHashMap;
import java.util.Map;

public class Cart {
    private String userId;
    // [Session 5 - Map] Map key = SKU: thêm cùng SKU thì cộng dồn chứ không tạo dòng trùng.
    // LinkedHashMap giữ thứ tự thêm vào giỏ (HashMap thì không).
    private Map<String, CartItem> items = new LinkedHashMap<>();

    public Cart() {}
    public Cart(String userId) { this.userId = userId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public Map<String, CartItem> getItems() { return items; }
    public void setItems(Map<String, CartItem> items) {
        this.items = items == null ? new LinkedHashMap<>() : new LinkedHashMap<>(items);
    }
    public boolean isEmpty() { return items.isEmpty(); }
    public void clear() { items.clear(); }
}
