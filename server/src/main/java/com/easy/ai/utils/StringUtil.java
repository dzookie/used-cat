package com.easy.ai.utils;

public class StringUtil {

    private StringUtil() {
    }

    public static String extractFirstLine(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        int index = text.indexOf('\n');
        if (index > 0 && text.charAt(index - 1) == '\r') {
            return text.substring(0, index - 1);
        }
        if (index >= 0) {
            return text.substring(0, index);
        }
        return text;
    }
}
