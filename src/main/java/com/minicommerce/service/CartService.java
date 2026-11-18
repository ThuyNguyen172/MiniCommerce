package com.minicommerce.service;

import com.minicommerce.entity.Cart;
import com.minicommerce.entity.CartItem;
import java.util.List;

// [Session 3 - SOLID] I (Interface Segregation): mỗi nghiệp vụ một interface nhỏ (Cart/Inventory/Order/Product...)
// thay vì một interface "ShopService" khổng lồ.
public interface CartService {
    Cart getCart(String userId);
    void addItem(String userId, String sku, int quantity);
    void removeItem(String userId, String sku);
    void clearCart(String userId);

    /** [Session 6] Bản sao các item trong giỏ tại một thời điểm (đọc an toàn khi có thread khác sửa giỏ). */
    List<CartItem> snapshotItems(String userId);
}
