package com.minicommerce.exception;

// [Session 5 - Exception] Custom unchecked exception cho đơn hàng không hợp lệ (kế thừa RuntimeException - Session 3).
public class InvalidOrderException extends RuntimeException {
    public InvalidOrderException(String message) { super(message); }
}
