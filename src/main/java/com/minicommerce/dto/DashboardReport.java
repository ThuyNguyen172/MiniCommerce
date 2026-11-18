package com.minicommerce.dto;

import com.minicommerce.entity.ProductVariant;
import com.minicommerce.entity.enums.OrderStatus;
import java.util.List;
import java.util.Map;

/**
 * [Session 6] Kết quả tổng hợp của dashboard, được build từ nhiều tác vụ chạy song song.
 * Dùng record (Java 16+): immutable nên chia sẻ giữa các thread an toàn.
 */
public record DashboardReport(
        long deliveredRevenue,
        Map<OrderStatus, Long> orderCountByStatus,
        Map<String, Integer> topSellingProducts,
        List<ProductVariant> lowStockVariants,
        List<String> taskLog,
        long elapsedMillis) {
}
