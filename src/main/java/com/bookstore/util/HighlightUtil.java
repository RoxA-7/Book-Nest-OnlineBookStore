package com.bookstore.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HighlightUtil {
    private HighlightUtil() {
    }

    public static String highlight(String text, String keyword) {
        String escapedText = escapeHtml(text == null ? "" : text);
        String normalized = keyword == null ? "" : keyword.trim();
        if (normalized.isEmpty()) {
            return escapedText;
        }
        Pattern pattern = Pattern.compile(Pattern.quote(escapeHtml(normalized)), Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(escapedText);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(result, "<mark>" + Matcher.quoteReplacement(matcher.group()) + "</mark>");
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private static String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
