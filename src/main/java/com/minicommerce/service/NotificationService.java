package com.minicommerce.service;

import com.minicommerce.entity.Order;
import java.util.List;

/** [Session 6] Gửi thông báo bất đồng bộ (giả lập email/SMS) để checkout không phải chờ. */
public interface NotificationService {
    void sendOrderConfirmationAsync(Order order);
    List<String> getSentLog();
    void shutdown();
}
