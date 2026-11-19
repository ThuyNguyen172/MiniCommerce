package com.minicommerce.config;

import com.minicommerce.entity.Product;
import com.minicommerce.entity.ProductVariant;
import com.minicommerce.entity.User;
import com.minicommerce.entity.enums.Category;
import com.minicommerce.entity.enums.ProductColor;
import com.minicommerce.entity.enums.ProductSize;
import com.minicommerce.entity.enums.UserRole;
import com.minicommerce.repository.OrderRepository;
import com.minicommerce.repository.ProductRepository;
import com.minicommerce.repository.UserRepository;
import com.minicommerce.repository.impl.InMemoryOrderRepository;
import com.minicommerce.repository.impl.InMemoryProductRepository;
import com.minicommerce.repository.impl.InMemoryUserRepository;
import com.minicommerce.service.AuthService;
import com.minicommerce.service.CartService;
import com.minicommerce.service.DiscountPolicy;
import com.minicommerce.service.InventoryService;
import com.minicommerce.service.NotificationService;
import com.minicommerce.service.OrderService;
import com.minicommerce.service.ProductService;
import com.minicommerce.service.ReportService;
import com.minicommerce.service.impl.AuthServiceImpl;
import com.minicommerce.service.impl.CartServiceImpl;
import com.minicommerce.service.impl.InventoryServiceImpl;
import com.minicommerce.service.impl.NotificationServiceImpl;
import com.minicommerce.service.impl.OrderServiceImpl;
import com.minicommerce.service.impl.ProductServiceImpl;
import com.minicommerce.service.impl.ReportServiceImpl;
import com.minicommerce.simulation.FlashSaleSimulator;

import java.util.UUID;

// [Session 3 - SOLID] Composition root: nơi DUY NHẤT biết class cụ thể nào được dùng và "lắp ráp" chúng (manual DI).
// Đổi InMemory -> Jdbc repository chỉ cần sửa ở đây.
// [Session 3 - OOP] Polymorphism: biến kiểu interface, object kiểu implementation.
public class AppConfig {
    private final ProductRepository productRepository = new InMemoryProductRepository();
    private final UserRepository userRepository = new InMemoryUserRepository();
    private final OrderRepository orderRepository = new InMemoryOrderRepository();

    private final ProductService productService = new ProductServiceImpl(productRepository);
    private final InventoryService inventoryService = new InventoryServiceImpl(productRepository);
    private final CartService cartService = new CartServiceImpl(productService, inventoryService);
    // [Lambda/Stream] Chính sách giảm giá tạo bằng lambda + kết hợp: giảm 5% cho đơn từ 1.000.000đ,
    // hoặc giảm 20.000đ cho đơn từ 500.000đ -> lấy mức có lợi nhất cho khách.
    private final DiscountPolicy discountPolicy =
            DiscountPolicy.percentOver(1_000_000, 5).bestOf(DiscountPolicy.flatOver(500_000, 20_000));
    // [Session 6] Các service có thread pool riêng -> phải shutdown khi thoát app.
    private final NotificationService notificationService = new NotificationServiceImpl();
    private final OrderService orderService = new OrderServiceImpl(
            orderRepository, cartService, productService, inventoryService, discountPolicy, notificationService);
    private final ReportService reportService = new ReportServiceImpl(orderService, inventoryService);
    private final FlashSaleSimulator flashSaleSimulator =
            new FlashSaleSimulator(productService, cartService, orderService);
    private final AuthService authService = new AuthServiceImpl(userRepository);

    public ProductService getProductService() { return productService; }
    public InventoryService getInventoryService() { return inventoryService; }
    public CartService getCartService() { return cartService; }
    public OrderService getOrderService() { return orderService; }
    public AuthService getAuthService() { return authService; }
    public NotificationService getNotificationService() { return notificationService; }
    public ReportService getReportService() { return reportService; }
    public FlashSaleSimulator getFlashSaleSimulator() { return flashSaleSimulator; }

    /** [Session 6] Dừng các thread pool nền một cách có kiểm soát. */
    public void shutdown() {
        reportService.shutdown();
        notificationService.shutdown();
    }

    public void seedDemoData() {
        userRepository.save(new User("USR-DEMO", "demo", "demo123", UserRole.CUSTOMER));
        userRepository.save(new User("USR-ADMIN", "admin", "admin123", UserRole.ADMIN));

        Product tshirt = new Product(UUID.randomUUID(), "Basic Cotton T-shirt", Category.T_SHIRT,
                "Everyday cotton T-shirt.");
        tshirt.addVariant(new ProductVariant("TS-BLK-M", ProductSize.M, ProductColor.BLACK, 159000, 20));
        tshirt.addVariant(new ProductVariant("TS-WHT-L", ProductSize.L, ProductColor.WHITE, 159000, 12));
        productService.addProduct(tshirt);

        Product shirt = new Product(UUID.randomUUID(), "Oxford Shirt", Category.SHIRT,
                "Smart-casual Oxford shirt.");
        shirt.addVariant(new ProductVariant("SH-BLU-M", ProductSize.M, ProductColor.BLUE, 299000, 8));
        shirt.addVariant(new ProductVariant("SH-BEI-L", ProductSize.L, ProductColor.BEIGE, 319000, 5));
        productService.addProduct(shirt);

        Product dress = new Product(UUID.randomUUID(), "Everyday Dress", Category.DRESS,
                "Comfortable dress for everyday wear.");
        dress.addVariant(new ProductVariant("DR-RED-S", ProductSize.S, ProductColor.RED, 399000, 6));
        productService.addProduct(dress);
    }
}
