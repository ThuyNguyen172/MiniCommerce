package com.minicommerce.service;

import com.minicommerce.entity.ProductVariant;
import java.util.List;
import java.util.Map;

// [Session 3 - SOLID] S (Single Responsibility): mọi thao tác tồn kho gom về một chỗ, tách khỏi ProductService.
public interface InventoryService {
    void checkStock(String sku, int requestedQuantity);
    void decreaseStock(String sku, int quantity);
    void increaseStock(String sku, int quantity);
    void restock(String sku, int quantity);

    /** [Lambda/Stream] Các biến thể có tồn kho <= threshold, tồn kho thấp nhất lên đầu. */
    List<ProductVariant> findLowStockVariants(int threshold);

    /**
     * [Session 6] Trừ kho cho nhiều SKU một cách nguyên tử (all-or-nothing):
     * hoặc trừ được hết, hoặc không trừ SKU nào. Map: sku -> quantity.
     */
    void reserveAll(Map<String, Integer> quantitiesBySku);

    /** [Session 6] Hoàn kho cho nhiều SKU (dùng khi huỷ đơn). */
    void releaseAll(Map<String, Integer> quantitiesBySku);
}
