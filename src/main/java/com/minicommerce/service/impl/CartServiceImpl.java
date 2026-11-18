package com.minicommerce.service.impl;

import com.minicommerce.entity.Cart;
import com.minicommerce.entity.CartItem;
import com.minicommerce.entity.Product;
import com.minicommerce.entity.ProductVariant;
import com.minicommerce.exception.ProductNotFoundException;
import com.minicommerce.service.CartService;
import com.minicommerce.service.InventoryService;
import com.minicommerce.service.ProductService;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CartServiceImpl implements CartService {
    // [Session 6] ConcurrentHashMap: computeIfAbsent là thao tác nguyên tử -> mỗi user chỉ có đúng 1 Cart
    // kể cả khi nhiều thread gọi getCart() cùng lúc.
    private final Map<String, Cart> carts = new ConcurrentHashMap<>();
    private final ProductService productService;
    private final InventoryService inventoryService;

    public CartServiceImpl(ProductService productService, InventoryService inventoryService) {
        this.productService = productService;
        this.inventoryService = inventoryService;
    }

    @Override
    public Cart getCart(String userId) {
        // [Session 5 - Map] computeIfAbsent: chưa có giỏ thì tạo mới rồi put, có rồi thì trả lại - gọn hơn get/null-check/put.
        return carts.computeIfAbsent(userId, Cart::new);
    }

    @Override
    public void addItem(String userId, String sku, int quantity) {
        if (userId == null || userId.isBlank()) throw new IllegalArgumentException("User ID is required.");
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be greater than zero.");
        Product product = productService.getProductBySku(sku);
        ProductVariant variant = product.findVariantBySku(sku);
        if (variant == null) throw new ProductNotFoundException("SKU not found: " + sku);

        Cart cart = getCart(userId);
        // [Session 6] Khoá trên chính object cart: chỉ chặn các thread cùng sửa giỏ của user này,
        // giỏ của user khác vẫn chạy song song.
        synchronized (cart) {
            CartItem existing = cart.getItems().get(sku);
            int newQuantity = quantity + (existing == null ? 0 : existing.getQuantity());
            inventoryService.checkStock(sku, newQuantity);
            cart.getItems().put(sku, new CartItem(sku, newQuantity));
        }
    }

    @Override
    public void removeItem(String userId, String sku) {
        Cart cart = getCart(userId);
        synchronized (cart) { cart.getItems().remove(sku); }
    }

    @Override
    public void clearCart(String userId) {
        Cart cart = getCart(userId);
        synchronized (cart) { cart.clear(); }
    }

    @Override
    public List<CartItem> snapshotItems(String userId) {
        Cart cart = getCart(userId);
        // [Session 4 - Collection] List.copyOf: trả bản sao immutable, người gọi không sửa được giỏ thật.
        synchronized (cart) { return List.copyOf(cart.getItems().values()); }
    }
}
