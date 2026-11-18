package com.minicommerce.util;

import java.util.Arrays;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

public final class InputHelper {
    private static final Scanner SCANNER = new Scanner(System.in);
    private InputHelper() {}

    public static String readString(String prompt) {
        System.out.print(prompt);
        return SCANNER.nextLine().trim();
    }

    // [Session 5 - Exception] try/catch NumberFormatException trong vòng lặp: nhập sai thì hỏi lại, không crash.
    public static int readInt(String prompt) {
        while (true) {
            String input = readString(prompt);
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException ex) {
                System.out.println("Please enter a valid integer.");
            }
        }
    }

    public static long readLong(String prompt) {
        while (true) {
            String input = readString(prompt);
            try {
                return Long.parseLong(input);
            } catch (NumberFormatException ex) {
                System.out.println("Please enter a valid whole number.");
            }
        }
    }

    // [Session 4 - Generic] Generic method với bounded type <E extends Enum<E>>: một hàm đọc được MỌI enum,
    // type-safe (trả đúng kiểu E, không cần ép kiểu).
    public static <E extends Enum<E>> E readEnum(String prompt, Class<E> enumType) {
        while (true) {
            String input = readString(prompt);
            try {
                return Enum.valueOf(enumType, input.toUpperCase().trim());
            } catch (IllegalArgumentException ex) {
                System.out.println("Invalid value. Options: " + optionsOf(enumType));
            }
        }
    }

    /** [Lambda/Stream] Cho phép bỏ trống -> Optional.empty() (dùng cho bộ lọc không bắt buộc). */
    public static <E extends Enum<E>> Optional<E> readOptionalEnum(String prompt, Class<E> enumType) {
        while (true) {
            String input = readString(prompt);
            if (input.isBlank()) return Optional.empty();
            try {
                return Optional.of(Enum.valueOf(enumType, input.toUpperCase()));
            } catch (IllegalArgumentException ex) {
                System.out.println("Invalid value. Options: " + optionsOf(enumType) + " (or leave blank)");
            }
        }
    }

    /** [Lambda/Stream] Bỏ trống -> Optional.empty(). */
    public static Optional<Long> readOptionalLong(String prompt) {
        while (true) {
            String input = readString(prompt);
            if (input.isBlank()) return Optional.empty();
            try {
                return Optional.of(Long.parseLong(input));
            } catch (NumberFormatException ex) {
                System.out.println("Please enter a valid whole number (or leave blank).");
            }
        }
    }

    public static boolean readYesNo(String prompt) {
        return readString(prompt).equalsIgnoreCase("y");
    }

    /** [Lambda/Stream] Stream + Collectors.joining thay cho toArray + String.join. */
    public static <E extends Enum<E>> String optionsOf(Class<E> enumType) {
        return Arrays.stream(enumType.getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.joining("/"));
    }
}
