package com.minicommerce.entity;

import com.minicommerce.entity.enums.OrderStatus;
import com.minicommerce.entity.enums.PaymentMethod;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private String id;
    private String userId;
    private String recipientName;
    private String phone;
    private String address;
    private PaymentMethod paymentMethod;
    // [Session 6] volatile để thay đổi trạng thái từ thread khác luôn được nhìn thấy.
    private volatile OrderStatus status = OrderStatus.PENDING;
    private List<OrderItem> items = new ArrayList<>();
    private long shippingFee;
    private long discount;
    private LocalDateTime createdAt = LocalDateTime.now();

    public Order() {}

    public Order(String id, String userId, String recipientName, String phone, String address,
                 PaymentMethod paymentMethod, List<OrderItem> items, long shippingFee, long discount) {
        this.id = id;
        this.userId = userId;
        this.recipientName = recipientName;
        this.phone = phone;
        this.address = address;
        this.paymentMethod = paymentMethod;
        // [Session 4 - Collection] Defensive copy: copy list đầu vào để bên ngoài sửa list gốc không ảnh hưởng Order.
        this.items = new ArrayList<>(items);
        this.shippingFee = shippingFee;
        this.discount = discount;
        this.status = OrderStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getRecipientName() { return recipientName; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public List<OrderItem> getItems() { return items; }
    public long getShippingFee() { return shippingFee; }
    public long getDiscount() { return discount; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public long getSubtotal() {
        return items.stream().mapToLong(OrderItem::getSubtotal).sum();
    }
    // [Session 3 - OOP] Giá trị dẫn xuất (derived) tính từ state bên trong, không lưu field riêng -> không bị lệch dữ liệu.
    public long getTotal() {
        return Math.max(0, getSubtotal() + shippingFee - discount);
    }
}
