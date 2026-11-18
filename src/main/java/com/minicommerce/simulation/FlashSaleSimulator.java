package com.minicommerce.simulation;

import com.minicommerce.dto.CheckoutRequest;
import com.minicommerce.entity.enums.PaymentMethod;
import com.minicommerce.service.CartService;
import com.minicommerce.service.OrderService;
import com.minicommerce.service.ProductService;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * [Session 6] Giả lập "flash sale": N khách hàng (N thread) cùng lúc mua 1 sản phẩm của cùng một SKU.
 * Mục tiêu: chứng minh tồn kho không bao giờ âm và số đơn thành công = số hàng bị trừ.
 *
 * Kỹ thuật dùng:
 *  - ExecutorService (thread pool) để chạy các khách hàng.
 *  - CountDownLatch "startGate" để tất cả thread xuất phát cùng lúc (tăng khả năng tranh chấp).
 *  - CountDownLatch "finished" để main thread chờ tất cả xong.
 *  - AtomicInteger / ConcurrentHashMap để đếm kết quả mà không cần synchronized.
 */
public class FlashSaleSimulator {
    private static final int MAX_POOL_SIZE = 10;

    private final ProductService productService;
    private final CartService cartService;
    private final OrderService orderService;

    public FlashSaleSimulator(ProductService productService, CartService cartService, OrderService orderService) {
        this.productService = productService;
        this.cartService = cartService;
        this.orderService = orderService;
    }

    public record Result(int buyers, int succeeded, int failed, int stockBefore, int stockAfter,
                         Map<String, Integer> failureReasons, long elapsedMillis) {
        public boolean isConsistent() {
            return stockAfter >= 0 && stockBefore - succeeded == stockAfter;
        }
    }

    // [Session 5 - Exception] Checked exception: khai báo throws để bắt buộc người gọi (AdminController) xử lý.
    public Result run(String sku, int buyers) throws InterruptedException {
        if (buyers <= 0) throw new IllegalArgumentException("Number of buyers must be greater than zero.");
        int stockBefore = currentStock(sku);

        ExecutorService pool = Executors.newFixedThreadPool(Math.min(buyers, MAX_POOL_SIZE));
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch finished = new CountDownLatch(buyers);
        AtomicInteger succeeded = new AtomicInteger();
        AtomicInteger failed = new AtomicInteger();
        Map<String, Integer> failureReasons = new ConcurrentHashMap<>();
        long start = System.currentTimeMillis();

        try {
            for (int i = 1; i <= buyers; i++) {
                String userId = "SIM-USER-" + i;
                String phone = String.format("09%08d", i);
                pool.submit(() -> {
                    try {
                        startGate.await();                       // chờ hiệu lệnh xuất phát
                        cartService.addItem(userId, sku, 1);
                        orderService.checkout(new CheckoutRequest(userId, "Flash Sale Bot " + userId,
                                phone, "Simulation address", PaymentMethod.COD));
                        succeeded.incrementAndGet();
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        failed.incrementAndGet();
                    } catch (RuntimeException ex) {
                        failed.incrementAndGet();
                        // [Session 5 - Map] merge(): chưa có key thì put 1, có rồi thì cộng thêm 1 (đếm tần suất).
                        failureReasons.merge(ex.getClass().getSimpleName(), 1, Integer::sum);
                        cartService.clearCart(userId);            // dọn giỏ của khách mua hụt
                    } finally {
                        finished.countDown();
                    }
                });
            }
            startGate.countDown();                                // tất cả cùng chạy
            if (!finished.await(30, TimeUnit.SECONDS))
                throw new IllegalStateException("Simulation timed out.");
        } finally {
            pool.shutdown();
        }

        return new Result(buyers, succeeded.get(), failed.get(), stockBefore, currentStock(sku),
                // [Session 5 - Map] TreeMap: Map tự sắp xếp theo key -> in lý do lỗi theo thứ tự alphabet.
                new TreeMap<>(failureReasons), System.currentTimeMillis() - start);
    }

    private int currentStock(String sku) {
        return productService.getProductBySku(sku).findVariantBySku(sku).getStockQuantity();
    }
}
