package com.minicommerce.repository;

import com.minicommerce.entity.Order;
import java.util.List;
import java.util.Optional;

// [Session 3 - SOLID] Abstraction tầng lưu trữ đơn hàng (xem ProductRepository).
public interface OrderRepository {
    Optional<Order> findById(String id);
    List<Order> findAll();
    List<Order> findByUserId(String userId);
    void save(Order order);
}
