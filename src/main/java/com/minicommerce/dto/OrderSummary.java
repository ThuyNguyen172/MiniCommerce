package com.minicommerce.dto;

import com.minicommerce.entity.Order;
import com.minicommerce.entity.enums.OrderStatus;
import java.time.LocalDateTime;

public class OrderSummary {
    private final String orderId;
    private final OrderStatus status;
    private final long total;
    private final LocalDateTime createdAt;

    public OrderSummary(Order order) {
        this.orderId = order.getId();
        this.status = order.getStatus();
        this.total = order.getTotal();
        this.createdAt = order.getCreatedAt();
    }

    public String getOrderId() { return orderId; }
    public OrderStatus getStatus() { return status; }
    public long getTotal() { return total; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return orderId + " | " + status + " | " + String.format("%,d VND", total) + " | " + createdAt;
    }
}
