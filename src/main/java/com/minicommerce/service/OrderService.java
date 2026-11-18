package com.minicommerce.service;

import com.minicommerce.dto.CheckoutRequest;
import com.minicommerce.entity.Order;
import com.minicommerce.entity.enums.OrderStatus;
import java.util.List;
import java.util.Map;

public interface OrderService {
    Order checkout(CheckoutRequest request);
    List<Order> getOrdersByUser(String userId);
    List<Order> getAllOrders();
    Order getOrderById(String orderId);
    void cancelOrder(String orderId, String userId);
    void updateOrderStatus(String orderId, OrderStatus status);
    long getDeliveredRevenue();

    /** [Lambda/Stream] Số đơn theo từng trạng thái. */
    Map<OrderStatus, Long> countOrdersByStatus();

    /** [Lambda/Stream] Tổng tiền theo từng trạng thái. */
    Map<OrderStatus, Long> getRevenueByStatus();

    /** [Lambda/Stream] Top sản phẩm bán chạy (không tính đơn đã huỷ): tên sản phẩm -> tổng số lượng. */
    Map<String, Integer> getTopSellingProducts(int limit);
}
