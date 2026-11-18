package com.minicommerce.util;

import java.util.UUID;

// [Session 3 - OOP] Utility class: final + private constructor -> không kế thừa, không tạo instance được.
public final class IdGenerator {
    private IdGenerator() {}
    public static String nextId(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
