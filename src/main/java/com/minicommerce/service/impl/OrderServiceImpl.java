package com.minicommerce.service.impl;

import com.minicommerce.dto.CheckoutRequest;
import com.minicommerce.entity.CartItem;
import com.minicommerce.entity.Order;
import com.minicommerce.entity.OrderItem;
import com.minicommerce.entity.Product;
import com.minicommerce.entity.ProductVariant;
import com.minicommerce.entity.enums.OrderStatus;
import com.minicommerce.exception.InvalidOrderException;
import com.minicommerce.exception.ProductNotFoundException;
import com.minicommerce.repository.OrderRepository;
import com.minicommerce.service.CartService;
import com.minicommerce.service.DiscountPolicy;
import com.minicommerce.service.InventoryService;
import com.minicommerce.service.NotificationService;
import com.minicommerce.service.OrderService;
import com.minicommerce.service.ProductService;
import com.minicommerce.util.IdGenerator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

// [Session 3 - SOLID] D (Dependency Inversion): phụ thuộc vào interface (OrderRepository, CartService, ...),
// được truyền vào qua constructor (constructor injection), không tự new implementation.
public class OrderServiceImpl implements OrderService {
    private static final long SHIPPING_FEE = 30000L;
    // [Session 5 - Set] Set.of(...): tập trạng thái được huỷ; contains() nhanh, immutable, không trùng phần tử.
    private static final Set<OrderStatus> CANCELLABLE = Set.of(OrderStatus.PENDING, OrderStatus.CONFIRMED);

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final ProductService productService;
    private final InventoryService inventoryService;
    private final DiscountPolicy discountPolicy;               // [Lambda/Stream]
    private final NotificationService notificationService;     // [Session 6]

    public OrderServiceImpl(OrderRepository orderRepository, CartService cartService,
                            ProductService productService, InventoryService inventoryService,
                            DiscountPolicy discountPolicy, NotificationService notificationService) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.productService = productService;
        this.inventoryService = inventoryService;
        this.discountPolicy = discountPolicy == null ? DiscountPolicy.none() : discountPolicy;
        this.notificationService = notificationService;
    }

    @Override
    public Order checkout(CheckoutRequest request) {
        if (request == null || isBlank(request.getUserId()) || isBlank(request.getRecipientName())
                || isBlank(request.getPhone()) || isBlank(request.getAddress())
                || request.getPaymentMethod() == null) {
            throw new InvalidOrderException("Please provide user, recipient, phone, address, and payment method.");
        }

        // [Session 6] Lấy bản sao giỏ hàng để không bị ảnh hưởng nếu giỏ bị sửa trong lúc checkout.
        List<CartItem> cartItems = cartService.snapshotItems(request.getUserId());
        if (cartItems.isEmpty()) throw new InvalidOrderException("Cannot checkout an empty cart.");

        // [Lambda/Stream] Vòng for cũ -> stream().map(...).toList()
        List<OrderItem> items = cartItems.stream()
                .map(this::toOrderItem)
                .toList();

        // [Lambda/Stream] groupingBy + summingInt: gộp số lượng theo SKU chuẩn (tránh trùng do khác hoa/thường).
        Map<String, Integer> quantitiesBySku = items.stream()
                .collect(Collectors.groupingBy(OrderItem::getSku, Collectors.summingInt(OrderItem::getQuantity)));

        // [Session 6] Kiểm tra + trừ kho nguyên tử. Nếu 2 khách cùng mua món cuối cùng,
        // chỉ 1 người thành công, người còn lại nhận InsufficientStockException.
        inventoryService.reserveAll(quantitiesBySku);

        long subtotal = items.stream().mapToLong(OrderItem::getSubtotal).sum();
        long discount = discountPolicy.calculateDiscount(subtotal);   // [Lambda/Stream] gọi functional interface

        Order order = new Order(IdGenerator.nextId("ORD"), request.getUserId(),
                request.getRecipientName(), request.getPhone(), request.getAddress(),
                request.getPaymentMethod(), items, SHIPPING_FEE, discount);
        orderRepository.save(order);
        cartService.clearCart(request.getUserId());

        // [Session 6] Gửi thông báo ở background thread, không chặn luồng chính.
        if (notificationService != null) notificationService.sendOrderConfirmationAsync(order);
        return order;
    }

    private OrderItem toOrderItem(CartItem cartItem) {
        Product product = productService.getProductBySku(cartItem.getSku());
        ProductVariant variant = product.findVariantBySku(cartItem.getSku());
        if (variant == null) throw new ProductNotFoundException("SKU not found: " + cartItem.getSku());
        return new OrderItem(variant.getSku(), product.getName(), variant.getSize(),
                variant.getColor(), variant.getPrice(), cartItem.getQuantity());
    }

    @Override
    public List<Order> getOrdersByUser(String userId) { return orderRepository.findByUserId(userId); }

    @Override
    public List<Order> getAllOrders() { return orderRepository.findAll(); }

    @Override
    public Order getOrderById(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new InvalidOrderException("Order not found: " + orderId));
    }

    @Override
    public void cancelOrder(String orderId, String userId) {
        Order order = getOrderById(orderId);
        if (!order.getUserId().equals(userId))
            throw new InvalidOrderException("This order does not belong to user " + userId);
        // [Session 6] Khoá theo từng đơn: nếu 2 thread cùng huỷ một đơn, chỉ lần đầu hoàn kho,
        // tránh hoàn kho 2 lần.
        synchronized (order) {
            if (!CANCELLABLE.contains(order.getStatus()))
                throw new InvalidOrderException("Only PENDING or CONFIRMED orders can be cancelled.");
            order.setStatus(OrderStatus.CANCELLED);
            inventoryService.releaseAll(order.getItems().stream()
                    .collect(Collectors.groupingBy(OrderItem::getSku, Collectors.summingInt(OrderItem::getQuantity))));
            orderRepository.save(order);
        }
    }

    @Override
    public void updateOrderStatus(String orderId, OrderStatus status) {
        Order order = getOrderById(orderId);
        synchronized (order) {
            if (order.getStatus() == OrderStatus.CANCELLED)
                throw new InvalidOrderException("A cancelled order cannot be updated.");
            if (status == null || status == OrderStatus.CANCELLED)
                throw new InvalidOrderException("Use cancelOrder to cancel an order.");
            order.setStatus(status);
            orderRepository.save(order);
        }
    }

    @Override
    public long getDeliveredRevenue() {
        return orderRepository.findAll().stream()
                .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                .mapToLong(Order::getTotal).sum();
    }

    @Override
    // [Session 5 - Map] EnumMap: Map chuyên cho key là enum, nhanh và giữ thứ tự khai báo enum.
    public Map<OrderStatus, Long> countOrdersByStatus() {
        // [Lambda/Stream] groupingBy với EnumMap để kết quả theo đúng thứ tự khai báo enum.
        return orderRepository.findAll().stream()
                .collect(Collectors.groupingBy(Order::getStatus,
                        () -> new EnumMap<>(OrderStatus.class), Collectors.counting()));
    }

    @Override
    public Map<OrderStatus, Long> getRevenueByStatus() {
        return orderRepository.findAll().stream()
                .collect(Collectors.groupingBy(Order::getStatus,
                        () -> new EnumMap<>(OrderStatus.class), Collectors.summingLong(Order::getTotal)));
    }

    @Override
    // [Session 5 - Map] LinkedHashMap để GIỮ thứ tự đã sort (HashMap sẽ làm mất thứ tự xếp hạng); duyệt entrySet().
    public Map<String, Integer> getTopSellingProducts(int limit) {
        // [Lambda/Stream] Pipeline: lọc đơn hợp lệ -> flatMap ra từng OrderItem -> gộp theo tên
        // -> sắp xếp giảm dần -> lấy top N -> giữ thứ tự bằng LinkedHashMap.
        return orderRepository.findAll().stream()
                .filter(order -> order.getStatus() != OrderStatus.CANCELLED)
                .flatMap(order -> order.getItems().stream())
                .collect(Collectors.groupingBy(OrderItem::getProductName,
                        Collectors.summingInt(OrderItem::getQuantity)))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(Math.max(0, limit))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (a, b) -> a, LinkedHashMap::new));
    }

    private boolean isBlank(String value) { return value == null || value.isBlank(); }
}
