package com.minicommerce.exception;

// [Session 5 - Exception] Custom unchecked exception cho lỗi nghiệp vụ "không đủ hàng" (kế thừa RuntimeException - Session 3).
public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) { super(message); }
}
