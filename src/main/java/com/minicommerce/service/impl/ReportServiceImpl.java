package com.minicommerce.service.impl;

import com.minicommerce.dto.DashboardReport;
import com.minicommerce.entity.ProductVariant;
import com.minicommerce.entity.enums.OrderStatus;
import com.minicommerce.service.InventoryService;
import com.minicommerce.service.OrderService;
import com.minicommerce.service.ReportService;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * [Session 6] 4 truy vấn báo cáo độc lập với nhau -> chạy song song trên thread pool
 * bằng CompletableFuture.supplyAsync, rồi gom kết quả bằng allOf().thenApply().
 * Mỗi tác vụ có delay giả lập (như query DB chậm) để thấy rõ chạy song song nhanh hơn tuần tự.
 */
public class ReportServiceImpl implements ReportService {
    private static final int LOW_STOCK_THRESHOLD = 5;
    private static final int TOP_N = 3;
    private static final long SIMULATED_QUERY_DELAY_MS = 400;

    private final OrderService orderService;
    private final InventoryService inventoryService;
    private final ExecutorService executor;

    public ReportServiceImpl(OrderService orderService, InventoryService inventoryService) {
        this.orderService = orderService;
        this.inventoryService = inventoryService;
        this.executor = Executors.newFixedThreadPool(4, namedDaemonThreads("report-worker-"));
    }

    @Override
    public CompletableFuture<DashboardReport> buildDashboardAsync() {
        long start = System.currentTimeMillis();
        List<String> taskLog = new CopyOnWriteArrayList<>();

        CompletableFuture<Long> revenue = runTask("deliveredRevenue", orderService::getDeliveredRevenue, taskLog);
        CompletableFuture<Map<OrderStatus, Long>> counts = runTask("countByStatus", orderService::countOrdersByStatus, taskLog);
        CompletableFuture<Map<String, Integer>> top = runTask("topSelling", () -> orderService.getTopSellingProducts(TOP_N), taskLog);
        CompletableFuture<List<ProductVariant>> lowStock = runTask("lowStock",
                () -> inventoryService.findLowStockVariants(LOW_STOCK_THRESHOLD), taskLog);

        return CompletableFuture.allOf(revenue, counts, top, lowStock)
                .thenApply(ignored -> new DashboardReport(
                        revenue.join(), counts.join(), top.join(), lowStock.join(),
                        List.copyOf(taskLog), System.currentTimeMillis() - start));
    }

    /** Higher-order helper: nhận một Supplier (lambda) và chạy nó bất đồng bộ trên pool. */
    // [Session 4 - Generic] Generic method <T>: dùng chung cho các truy vấn trả về Long, Map, List...
    private <T> CompletableFuture<T> runTask(String name, Supplier<T> query, List<String> taskLog) {
        return CompletableFuture.supplyAsync(() -> {
            sleepQuietly(SIMULATED_QUERY_DELAY_MS);
            T result = query.get();
            taskLog.add(name + " done on " + Thread.currentThread().getName());
            return result;
        }, executor);
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private static ThreadFactory namedDaemonThreads(String prefix) {
        AtomicInteger counter = new AtomicInteger(1);   // AtomicInteger: tăng số an toàn giữa các thread
        return runnable -> {
            Thread thread = new Thread(runnable, prefix + counter.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        };
    }

    @Override
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(2, TimeUnit.SECONDS)) executor.shutdownNow();
        } catch (InterruptedException ex) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
