package com.minicommerce.util;

import com.minicommerce.entity.Order;

// [Session 3 - SOLID] S: class chỉ có một trách nhiệm là định dạng & in hoá đơn.
public final class ReceiptPrinter {
    private ReceiptPrinter() {}

    public static void print(Order order) {
        System.out.println("\n========== RECEIPT ==========");
        System.out.println("Order: " + order.getId());
        System.out.println("Customer: " + order.getRecipientName());
        System.out.println("Phone: " + order.getPhone());
        System.out.println("Address: " + order.getAddress());
        System.out.println("Payment: " + order.getPaymentMethod());
        System.out.println("-----------------------------");
        // [Lambda/Stream] for-each -> forEach + lambda
        order.getItems().forEach(item -> System.out.printf("%s (%s/%s) x%d = %,d VND%n",
                item.getProductName(), item.getSize(), item.getColor(),
                item.getQuantity(), item.getSubtotal()));
        System.out.println("-----------------------------");
        System.out.printf("Subtotal: %,d VND%n", order.getSubtotal());
        System.out.printf("Shipping: %,d VND%n", order.getShippingFee());
        System.out.printf("Discount: %,d VND%n", order.getDiscount());
        System.out.printf("TOTAL: %,d VND%n", order.getTotal());
        System.out.println("Status: " + order.getStatus());
        System.out.println("=============================\n");
    }
}
