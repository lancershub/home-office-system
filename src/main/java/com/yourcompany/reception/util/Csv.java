package com.yourcompany.reception.util;
public final class Csv {
    private Csv() {}
    public static String cell(Object value) {
        String text = value == null ? "" : String.valueOf(value);
        String trimmed = text.trim();
        if ((!trimmed.isEmpty() && "=+-@".indexOf(trimmed.charAt(0)) >= 0) || text.startsWith("\t") || text.startsWith("\r") || text.startsWith("\n")) text = "'" + text;
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }
}
