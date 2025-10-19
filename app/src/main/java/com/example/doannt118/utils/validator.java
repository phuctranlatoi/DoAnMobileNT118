package com.example.doannt118.utils;

public class validator {
    public static boolean isValidPhone(String phone) {
        return phone != null && phone.matches("\\d{10,11}");
    }

    public static boolean isNotEmpty(String text) {
        return text != null && !text.trim().isEmpty();
    }
}

