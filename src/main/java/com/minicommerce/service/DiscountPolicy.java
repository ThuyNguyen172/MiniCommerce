package com.minicommerce.service;

/**
 * [Lambda/Stream] Functional interface tự định nghĩa: nhận subtotal, trả về số tiền giảm.
 * Các chính sách được tạo bằng lambda và kết hợp bằng default method bestOf().
 */
@FunctionalInterface
// [Session 3 - SOLID] O (Open/Closed): thêm chính sách giảm giá mới = tạo policy mới, KHÔNG sửa OrderServiceImpl.
// (Strategy pattern)
public interface DiscountPolicy {
    long calculateDiscount(long subtotal);

    static DiscountPolicy none() {
        return subtotal -> 0L;
    }

    /** Giảm percent% khi subtotal >= threshold. */
    static DiscountPolicy percentOver(long threshold, int percent) {
        return subtotal -> subtotal >= threshold ? subtotal * percent / 100 : 0L;
    }

    /** Giảm cố định amount khi subtotal >= threshold. */
    static DiscountPolicy flatOver(long threshold, long amount) {
        return subtotal -> subtotal >= threshold ? Math.min(amount, subtotal) : 0L;
    }

    /** Higher-order function: trả về policy mới chọn mức giảm lớn nhất giữa 2 policy. */
    default DiscountPolicy bestOf(DiscountPolicy other) {
        return subtotal -> Math.max(this.calculateDiscount(subtotal), other.calculateDiscount(subtotal));
    }
}
