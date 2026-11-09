package com.example.scores.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 控制台输出小工具：表格线、文本对齐、空值处理。
 * 只做展示，不参与业务逻辑。
 */
public final class ConsoleUtil {

    private ConsoleUtil() {
    }

    /** 打印分隔线 */
    public static void line() {
        System.out.println("------------------------------------------------------------");
    }

    /** 打印标题 */
    public static void title(String text) {
        System.out.println();
        System.out.println("=== " + text + " ===");
    }

    /**
     * 把字符串按显示宽度右侧补空格（中文按 2 个宽度计算，避免表格错位）。
     */
    public static String padRight(String text, int width) {
        String s = text == null ? "" : text;
        int displayWidth = displayWidth(s);
        if (displayWidth >= width) {
            return s;
        }
        return s + " ".repeat(width - displayWidth);
    }

    /**
     * 把字符串按显示宽度左侧补空格。
     */
    public static String padLeft(String text, int width) {
        String s = text == null ? "" : text;
        int displayWidth = displayWidth(s);
        if (displayWidth >= width) {
            return s;
        }
        return " ".repeat(width - displayWidth) + s;
    }

    /** 计算字符串在等宽终端中的显示宽度：中文/全角字符按 2 计 */
    public static int displayWidth(String text) {
        if (text == null) {
            return 0;
        }
        int width = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            width += (c > 0x7F) ? 2 : 1;
        }
        return width;
    }

    /** 空值转空字符串 */
    public static String nvl(String text) {
        return text == null ? "" : text;
    }

    /** 金额/分数保留 2 位小数，空值显示 "-" */
    public static String num(BigDecimal value) {
        if (value == null) {
            return "-";
        }
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    /** 百分比显示，例如 66.67% */
    public static String percent(BigDecimal value) {
        if (value == null) {
            return "-";
        }
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString() + "%";
    }
}
