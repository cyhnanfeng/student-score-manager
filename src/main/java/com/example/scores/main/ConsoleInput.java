package com.example.scores.main;

import com.example.scores.util.InputClosedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * 控制台输入工具：统一处理 Scanner 读取、空输入、数字与日期格式错误。
 * 所有界面类都通过它读输入，避免重复写 try-catch。
 */
public class ConsoleInput {

    private final Scanner scanner;

    public ConsoleInput(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * 读取一行文本，返回去除首尾空格后的内容（可能为空串）。
     *
     * @throws InputClosedException 输入流已结束（EOF / Ctrl+Z）
     */
    public String readLine(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            // 输入流结束：抛出异常让 MainApp 干净退出，避免菜单无限循环
            System.out.println();
            throw new InputClosedException("输入流已结束");
        }
        return scanner.nextLine().trim();
    }

    /** 读取必填文本，为空时提示并返回 null */
    public String readRequired(String prompt) {
        while (true) {
            String value = readLine(prompt);
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("输入不能为空，请重新输入。");
        }
    }

    /** 读取整数，格式错误时重新输入 */
    public int readInt(String prompt) {
        while (true) {
            String value = readLine(prompt);
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                System.out.println("请输入合法的整数。");
            }
        }
    }

    /** 读取可空整数，直接回车返回 null */
    public Integer readOptionalInt(String prompt) {
        while (true) {
            String value = readLine(prompt);
            if (value.isEmpty()) {
                return null;
            }
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                System.out.println("请输入合法的整数，或直接回车跳过。");
            }
        }
    }

    /** 读取可空小数，直接回车返回 null */
    public BigDecimal readOptionalDecimal(String prompt) {
        while (true) {
            String value = readLine(prompt);
            if (value.isEmpty()) {
                return null;
            }
            try {
                return new BigDecimal(value);
            } catch (NumberFormatException e) {
                System.out.println("请输入合法的数字，或直接回车跳过。");
            }
        }
    }

    /** 读取必填小数 */
    public BigDecimal readRequiredDecimal(String prompt) {
        while (true) {
            BigDecimal value = readOptionalDecimal(prompt);
            if (value != null) {
                return value;
            }
            System.out.println("输入不能为空，请重新输入。");
        }
    }

    /** 读取可空日期（yyyy-MM-dd），直接回车返回 null */
    public LocalDate readOptionalDate(String prompt) {
        while (true) {
            String value = readLine(prompt);
            if (value.isEmpty()) {
                return null;
            }
            try {
                return LocalDate.parse(value);
            } catch (DateTimeParseException e) {
                System.out.println("日期格式应为 yyyy-MM-dd，例如 2025-01-10。");
            }
        }
    }

    /** 确认操作：输入 y 返回 true */
    public boolean confirm(String prompt) {
        String value = readLine(prompt + "（y/N）：");
        return "y".equalsIgnoreCase(value);
    }

    /** 暂停，等待用户按回车继续 */
    public void pause() {
        System.out.print("按回车键继续...");
        if (!scanner.hasNextLine()) {
            System.out.println();
            throw new InputClosedException("输入流已结束");
        }
        scanner.nextLine();
    }
}
