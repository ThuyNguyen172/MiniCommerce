package com.minicommerce;

import com.minicommerce.config.AppConfig;
import com.minicommerce.controller.AdminController;
import com.minicommerce.controller.CartController;
import com.minicommerce.controller.OrderController;
import com.minicommerce.controller.ProductController;
import com.minicommerce.util.InputHelper;

public class Main {
    public static void main(String[] args) {
        AppConfig config = new AppConfig();
        config.seedDemoData();

        ProductController productController = new ProductController(config.getProductService());
        CartController cartController = new CartController(config.getCartService(), config.getProductService());
        OrderController orderController = new OrderController(config.getOrderService());
        AdminController adminController = new AdminController(
                config.getProductService(), config.getOrderService(), config.getInventoryService(),
                config.getReportService(), config.getNotificationService(), config.getFlashSaleSimulator());

        boolean running = true;
        while (running) {
            System.out.println("\n=== MINICOMMERCE: FASHION SHOP ===");
            System.out.println("1. Browse products");
            System.out.println("2. View product details");
            System.out.println("9. Search & filter products");
            System.out.println("3. Add product variant to cart");
            System.out.println("4. View cart");
            System.out.println("5. Checkout");
            System.out.println("6. View my orders");
            System.out.println("7. Cancel an order");
            System.out.println("8. Admin menu");
            System.out.println("0. Exit");

            switch (InputHelper.readInt("Choose: ")) {
                case 1 -> productController.listProducts();
                case 2 -> productController.showProductDetails();
                case 3 -> cartController.addItem();
                case 4 -> cartController.showCart();
                case 5 -> orderController.checkout();
                case 6 -> orderController.showMyOrders();
                case 7 -> orderController.cancelOrder();
                case 8 -> adminController.showMenu();
                case 9 -> productController.searchAndFilter();
                case 0 -> running = false;
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
        config.shutdown();   // [Session 6] tắt các thread pool nền trước khi thoát
        System.out.println("Thank you for visiting MiniCommerce!");
    }
}
