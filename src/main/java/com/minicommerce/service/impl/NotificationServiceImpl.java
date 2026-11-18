package com.minicommerce.service.impl;

import com.minicommerce.entity.Order;
import com.minicommerce.service.NotificationService;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * [Session 6] Checkout xong sẽ "nộp" việc gửi thông báo cho một background thread rồi trả kết quả ngay,
 * thay vì bắt người dùng đợi việc gửi email (chậm) hoàn tất.
 */
public class NotificationServiceImpl implements NotificationService {
    private static final long SIMULATED_SEND_DELAY_MS = 300;
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    // Single thread: các thông báo được gửi lần lượt theo thứ tự nộp vào hàng đợi.
    private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "notification-worker");
        thread.setDaemon(true);   // không giữ JVM sống khi chương trình thoát
        return thread;
    });

    // CopyOnWriteArrayList: an toàn khi worker thread ghi còn main thread đọc log.
    private final List<String> sentLog = new CopyOnWriteArrayList<>();

    @Override
    public void sendOrderConfirmationAsync(Order order) {
        try {
            executor.submit(() -> send(order));     // Runnable dạng lambda
        } catch (RejectedExecutionException ex) {
            sentLog.add("[SKIPPED] " + order.getId() + " - notification service is shut down");
        }
    }

    private void send(Order order) {
        try {
            Thread.sleep(SIMULATED_SEND_DELAY_MS);   // giả lập gọi email/SMS server
            sentLog.add(String.format("[%s] %s -> sent confirmation for %s to %s (%,d VND)",
                    LocalTime.now().format(TIME), Thread.currentThread().getName(),
                    order.getId(), order.getPhone(), order.getTotal()));
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();     // giữ lại cờ interrupt, không "nuốt" exception
        }
    }

    @Override
    public List<String> getSentLog() { return List.copyOf(sentLog); }

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
