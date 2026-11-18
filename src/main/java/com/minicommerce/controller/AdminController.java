package com.minicommerce.controller;

import com.minicommerce.dto.DashboardReport;
import com.minicommerce.entity.Product;
import com.minicommerce.entity.ProductVariant;
import com.minicommerce.entity.enums.Category;
import com.minicommerce.entity.enums.OrderStatus;
import com.minicommerce.entity.enums.ProductColor;
import com.minicommerce.entity.enums.ProductSize;
import com.minicommerce.service.InventoryService;
import com.minicommerce.service.NotificationService;
import com.minicommerce.service.OrderService;
import com.minicommerce.service.ProductService;
import com.minicommerce.service.ReportService;
import com.minicommerce.simulation.FlashSaleSimulator;
import com.minicommerce.util.InputHelper;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AdminController {
    private final ProductService productService;
    private final OrderService orderService;
    private final InventoryService inventoryService;
    private final ReportService reportService;              // [Session 6]
    private final NotificationService notificationService;  // [Session 6]
    private final FlashSaleSimulator flashSaleSimulator;    // [Session 6]

    public AdminController(ProductService productService, OrderService orderService,
                           InventoryService inventoryService, ReportService reportService,
                           NotificationService notificationService, FlashSaleSimulator flashSaleSimulator) {
        this.productService = productService;
        this.orderService = orderService;
        this.inventoryService = inventoryService;
        this.reportService = reportService;
        this.notificationService = notificationService;
        this.flashSaleSimulator = flashSaleSimulator;
    }

    public void showMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- ADMIN MENU (demo only; no login security) ---");
            System.out.println("1. List products");
            System.out.println("2. Add product with one variant");
            System.out.println("3. Deactivate product");
            System.out.println("4. Restock SKU");
            System.out.println("5. List all orders");
            System.out.println("6. Update order status");
            System.out.println("7. Show delivered revenue");
            System.out.println("8. Sales report (by status + top sellers)   [Lambda/Stream]");
            System.out.println("9. Low-stock report                         [Lambda/Stream]");
            System.out.println("10. Dashboard (parallel reports)            [Session 6]");
            System.out.println("11. Flash sale simulation (multi-thread)    [Session 6]");
            System.out.println("12. Notification log (async)                [Session 6]");
            System.out.println("0. Back");
            switch (InputHelper.readInt("Choose: ")) {
                case 1 -> listProducts();
                case 2 -> addProduct();
                case 3 -> deactivateProduct();
                case 4 -> restock();
                case 5 -> orderService.getAllOrders().forEach(o ->
                        System.out.println(o.getId() + " | " + o.getUserId() + " | " + o.getStatus()
                                + " | " + String.format("%,d VND", o.getTotal())));
                case 6 -> updateStatus();
                case 7 -> System.out.printf("Delivered revenue: %,d VND%n", orderService.getDeliveredRevenue());
                case 8 -> salesReport();
                case 9 -> lowStockReport();
                case 10 -> dashboard();
                case 11 -> flashSale();
                case 12 -> showNotificationLog();
                case 0 -> back = true;
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void listProducts() {
        productService.getAllActiveProducts().forEach(p -> {
            System.out.println(p);
            p.getVariants().forEach(v -> System.out.println("  " + v));
        });
    }

    private void addProduct() {
        try {
            String id = InputHelper.readString("Product ID: ");
            String name = InputHelper.readString("Name: ");
            Category category = InputHelper.readEnum("Category (T_SHIRT/SHIRT/PANTS/JACKET/DRESS): ", Category.class);
            String description = InputHelper.readString("Description: ");
            Product product = new Product(id, name, category, description);
            String sku = InputHelper.readString("Variant SKU: ");
            ProductSize size = InputHelper.readEnum("Size (XS/S/M/L/XL/XXL): ", ProductSize.class);
            ProductColor color = InputHelper.readEnum("Color (BLACK/WHITE/BLUE/BEIGE/RED/GREEN/GREY): ", ProductColor.class);
            long price = InputHelper.readLong("Price in VND: ");
            int stock = InputHelper.readInt("Initial stock: ");
            product.addVariant(new ProductVariant(sku, size, color, price, stock));
            productService.addProduct(product);
            System.out.println("Product added.");
        } catch (RuntimeException ex) {
            System.out.println("Could not add product: " + ex.getMessage());
        }
    }

    private void deactivateProduct() {
        try {
            String id = InputHelper.readString("Product ID: ");
            productService.deactivateProduct(id);
            System.out.println("Product deactivated.");
        } catch (RuntimeException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }

    private void restock() {
        try {
            String sku = InputHelper.readString("SKU: ");
            int quantity = InputHelper.readInt("Quantity to add: ");
            inventoryService.restock(sku, quantity);
            System.out.println("Stock updated.");
        } catch (RuntimeException ex) {
            System.out.println("Restock failed: " + ex.getMessage());
        }
    }

    private void updateStatus() {
        try {
            String id = InputHelper.readString("Order ID: ");
            OrderStatus status = InputHelper.readEnum("New status (PENDING/CONFIRMED/SHIPPED/DELIVERED): ", OrderStatus.class);
            orderService.updateOrderStatus(id, status);
            System.out.println("Order status updated.");
        } catch (RuntimeException ex) {
            System.out.println("Could not update order: " + ex.getMessage());
        }
    }

    // ===================== [Lambda/Stream] Stream-based reports =====================

    private void salesReport() {
        Map<OrderStatus, Long> counts = orderService.countOrdersByStatus();
        // [Session 5 - Map] Map.forEach((k, v) -> ...) và getOrDefault() bên dưới tránh NullPointerException khi thiếu key.
        Map<OrderStatus, Long> revenue = orderService.getRevenueByStatus();
        if (counts.isEmpty()) {
            System.out.println("No orders yet.");
            return;
        }
        System.out.println("\n--- Orders by status ---");
        counts.forEach((status, count) -> System.out.printf("%-10s | %3d order(s) | %,d VND%n",
                status, count, revenue.getOrDefault(status, 0L)));
        System.out.println("--- Top 3 best sellers (excluding cancelled) ---");
        printRanking(orderService.getTopSellingProducts(3));
    }

    private void lowStockReport() {
        int threshold = InputHelper.readInt("Show variants with stock <= ");
        List<ProductVariant> lowStock = inventoryService.findLowStockVariants(threshold);
        if (lowStock.isEmpty()) System.out.println("No variants at or below " + threshold + ".");
        else lowStock.forEach(v -> System.out.println("  " + v));
    }

    private void printRanking(Map<String, Integer> ranking) {
        if (ranking.isEmpty()) {
            System.out.println("  (no sales yet)");
            return;
        }
        int[] rank = {1};   // lambda chỉ capture biến effectively final -> dùng mảng 1 phần tử làm bộ đếm
        ranking.forEach((name, qty) -> System.out.printf("  #%d %s - %d sold%n", rank[0]++, name, qty));
    }

    // ===================== [Session 6] Multi-thread features =====================

    private void dashboard() {
        System.out.println("Building dashboard: 4 reports are running in parallel...");
        CompletableFuture<DashboardReport> future = reportService.buildDashboardAsync();
        try {
            DashboardReport report = future.get(10, TimeUnit.SECONDS);   // chờ có giới hạn thời gian
            System.out.println("\n=========== DASHBOARD ===========");
            System.out.printf("Delivered revenue: %,d VND%n", report.deliveredRevenue());
            System.out.println("Orders by status: " + report.orderCountByStatus());
            System.out.println("Top sellers:");
            printRanking(report.topSellingProducts());
            System.out.println("Low stock (<= 5):");
            if (report.lowStockVariants().isEmpty()) System.out.println("  (none)");
            else report.lowStockVariants().forEach(v -> System.out.println("  " + v));
            System.out.println("--- task log ---");
            report.taskLog().forEach(line -> System.out.println("  " + line));
            System.out.printf("Built in %d ms (each task sleeps ~400 ms; sequential would take ~1600 ms)%n",
                    report.elapsedMillis());
            System.out.println("=================================");
        } catch (TimeoutException ex) {
            // [Session 5 - Exception] Nhiều khối catch cho từng loại checked exception, xử lý khác nhau.
            future.cancel(true);
            System.out.println("Dashboard timed out.");
        } catch (ExecutionException ex) {
            System.out.println("Dashboard failed: " + ex.getCause().getMessage());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            System.out.println("Dashboard interrupted.");
        }
    }

    private void flashSale() {
        String sku = InputHelper.readString("SKU for flash sale (e.g. DR-RED-S): ");
        int buyers = InputHelper.readInt("Number of concurrent buyers: ");
        try {
            FlashSaleSimulator.Result result = flashSaleSimulator.run(sku, buyers);
            System.out.println("\n--- Flash sale result ---");
            System.out.printf("Buyers: %d | Succeeded: %d | Failed: %d | %d ms%n",
                    result.buyers(), result.succeeded(), result.failed(), result.elapsedMillis());
            System.out.printf("Stock before: %d -> after: %d%n", result.stockBefore(), result.stockAfter());
            if (!result.failureReasons().isEmpty()) System.out.println("Failure reasons: " + result.failureReasons());
            System.out.println(result.isConsistent()
                    ? "OK: stock is consistent (before - succeeded = after), no overselling."
                    : "WARNING: stock is inconsistent -> race condition!");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            System.out.println("Simulation interrupted.");
        } catch (RuntimeException ex) {
            System.out.println("Simulation failed: " + ex.getMessage());
        }
    }

    private void showNotificationLog() {
        List<String> log = notificationService.getSentLog();
        if (log.isEmpty()) System.out.println("No notifications sent yet (they are sent ~300 ms after checkout).");
        else log.forEach(System.out::println);
    }
}
