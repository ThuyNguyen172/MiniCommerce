package com.minicommerce.service;

import com.minicommerce.dto.DashboardReport;
import java.util.concurrent.CompletableFuture;

/** [Session 6] Tạo báo cáo bằng cách chạy song song nhiều truy vấn độc lập. */
public interface ReportService {
    CompletableFuture<DashboardReport> buildDashboardAsync();
    void shutdown();
}
