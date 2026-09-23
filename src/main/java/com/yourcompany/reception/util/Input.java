package com.yourcompany.reception.util;
import java.nio.charset.StandardCharsets;
public final class Input {
    private Input() {}
    public static String text(String value, String field, int max, boolean required) {
        String s = value == null ? "" : value.trim();
        if ((required && s.isEmpty()) || s.length() > max || s.indexOf('\0') >= 0) throw new IllegalArgumentException(field + "格式或长度不正确");
        return s;
    }
    public static String password(String value) {
        if (value == null || value.length() < 12 || value.getBytes(StandardCharsets.UTF_8).length > 72)
            throw new IllegalArgumentException("密码至少 12 个字符，UTF-8 编码后最多 72 字节");
        return value;
    }
    public static int id(Integer value) {
        if (value == null || value <= 0) throw new IllegalArgumentException("ID 不正确");
        return value;
    }
}
