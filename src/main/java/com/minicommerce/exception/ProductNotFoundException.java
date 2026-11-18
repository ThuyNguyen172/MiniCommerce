package com.minicommerce.exception;

// [Session 3 - OOP] Inheritance: kế thừa RuntimeException, gọi super(message).
// [Session 5 - Exception] Custom unchecked exception: không bắt buộc try/catch, nổi lên tới controller xử lý.
public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String message) { super(message); }
}
