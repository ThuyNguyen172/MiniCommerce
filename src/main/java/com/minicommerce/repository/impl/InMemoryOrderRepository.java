package com.minicommerce.repository.impl;

import com.minicommerce.entity.Order;
import com.minicommerce.repository.OrderRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryOrderRepository implements OrderRepository {
    // [Session 6] Nhiều thread checkout cùng lúc sẽ cùng save() -> cần map thread-safe.
    // [Session 5 - Map] Map<orderId, Order>.
    private final Map<String, Order> orders = new ConcurrentHashMap<>();
    // [Session 4 - Collection] Comparator<Order> generic để sắp xếp collection theo thời gian tạo.
    private static final Comparator<Order> BY_CREATED_AT = Comparator.comparing(Order::getCreatedAt);

    @Override
    public Optional<Order> findById(String id) { return Optional.ofNullable(orders.get(id)); }

    @Override
    public List<Order> findAll() {
        return orders.values().stream().sorted(BY_CREATED_AT).toList();
    }

    @Override
    public List<Order> findByUserId(String userId) {
        return orders.values().stream()
                .filter(order -> order.getUserId().equals(userId))
                .sorted(BY_CREATED_AT)
                .toList();
    }

    @Override
    public void save(Order order) { orders.put(order.getId(), order); }
}
