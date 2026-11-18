package com.minicommerce.controller;

import com.minicommerce.entity.CartItem;
import com.minicommerce.entity.Product;
import com.minicommerce.entity.ProductVariant;
import com.minicommerce.service.CartService;
import com.minicommerce.service.ProductService;
import com.minicommerce.util.InputHelper;
import java.util.List;

public class CartController {
    private final CartService cartService;
    private final ProductService productService;
    private static final String DEFAULT_USER_ID = "USR-DEMO";

    public CartController(CartService cartService, ProductService productService) {
        this.cartService = cartService;
        this.productService = productService;
    }

    /** [Lambda/Stream] Một dòng trong giỏ hàng đã được "làm giàu" thông tin sản phẩm. */
    private record CartLine(Product product, ProductVariant variant, int quantity) {
        long subtotal() { return variant.getPrice() * quantity; }
    }

    public void addItem() {
        try {
            String sku = InputHelper.readString("SKU (see product list): ");
            int quantity = InputHelper.readInt("Quantity: ");
            cartService.addItem(DEFAULT_USER_ID, sku, quantity);
            System.out.println("Added to cart.");
        } catch (RuntimeException ex) {
            // [Session 5 - Exception] Bắt exception ở tầng controller -> app không crash, in message thân thiện.
            System.out.println("Could not add item: " + ex.getMessage());
        }
    }

    public void showCart() {
        List<CartItem> items = cartService.snapshotItems(DEFAULT_USER_ID);
        if (items.isEmpty()) {
            System.out.println("Your cart is empty.");
            return;
        }
        // [Lambda/Stream] Vòng for + biến total cộng dồn -> map sang CartLine, rồi mapToLong().sum()
        List<CartLine> lines = items.stream()
                .map(this::toCartLine)
                .toList();

        System.out.println("\n--- Cart ---");
        lines.forEach(line -> System.out.printf("%s | %s | %s | qty %d | %,d VND%n",
                line.product().getName(), line.variant().getSize(), line.variant().getColor(),
                line.quantity(), line.subtotal()));
        long total = lines.stream().mapToLong(CartLine::subtotal).sum();
        System.out.printf("Subtotal: %,d VND%n", total);
    }

    private CartLine toCartLine(CartItem item) {
        Product product = productService.getProductBySku(item.getSku());
        return new CartLine(product, product.findVariantBySku(item.getSku()), item.getQuantity());
    }
}
