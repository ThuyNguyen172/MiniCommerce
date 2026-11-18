package com.minicommerce.dto;

import com.minicommerce.entity.enums.PaymentMethod;

// [Session 3 - OOP] DTO / Parameter Object: gom 5 tham số checkout thành 1 object, method signature gọn và dễ mở rộng.
public class CheckoutRequest {
    private String userId;
    private String recipientName;
    private String phone;
    private String address;
    private PaymentMethod paymentMethod;

    public CheckoutRequest(String userId, String recipientName, String phone, String address,
                           PaymentMethod paymentMethod) {
        this.userId = userId;
        this.recipientName = recipientName;
        this.phone = phone;
        this.address = address;
        this.paymentMethod = paymentMethod;
    }

    public String getUserId() { return userId; }
    public String getRecipientName() { return recipientName; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
}
