package com.minicommerce.controller;

import com.minicommerce.dto.CheckoutRequest;
import com.minicommerce.dto.OrderSummary;
import com.minicommerce.entity.Order;
import com.minicommerce.entity.enums.PaymentMethod;
import com.minicommerce.service.OrderService;
import com.minicommerce.util.InputHelper;
import com.minicommerce.util.ReceiptPrinter;

// [Session 3 - SOLID] S: controller chỉ lo nhập/xuất console, nghiệp vụ giao cho OrderService, in hoá đơn giao cho ReceiptPrinter.
public class OrderController {
    private final OrderService orderService;
    private static final String DEFAULT_USER_ID = "USR-DEMO";

    public OrderController(OrderService orderService) { this.orderService = orderService; }

    public void checkout() {
        try {
            String recipient = InputHelper.readString("Recipient name: ");
            String phone = InputHelper.readString("Phone: ");
            String address = InputHelper.readString("Delivery address: ");
            PaymentMethod payment = InputHelper.readEnum("Payment method (COD/BANK_TRANSFER): ", PaymentMethod.class);
            Order order = orderService.checkout(new CheckoutRequest(
                    DEFAULT_USER_ID, recipient, phone, address, payment));
            ReceiptPrinter.print(order);
        } catch (RuntimeException ex) {
            System.out.println("Checkout failed: " + ex.getMessage());
        }
    }

    public void showMyOrders() {
        var orders = orderService.getOrdersByUser(DEFAULT_USER_ID);
        if (orders.isEmpty()) {
            System.out.println("No orders found.");
            return;
        }
        // [Lambda/Stream] Constructor reference OrderSummary::new + method reference System.out::println
        orders.stream().map(OrderSummary::new).forEach(System.out::println);
    }

    public void cancelOrder() {
        try {
            String orderId = InputHelper.readString("Order ID to cancel: ");
            orderService.cancelOrder(orderId, DEFAULT_USER_ID);
            System.out.println("Order cancelled and stock restored.");
        } catch (RuntimeException ex) {
            System.out.println("Cannot cancel order: " + ex.getMessage());
        }
    }
}
