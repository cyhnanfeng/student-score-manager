package com.example.scores.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * SHA-256 工具类。
 * <p>
 * 用于登录密码的单向哈希：数据库只保存 64 位小写十六进制字符串，不保存明文。
 * 注意：这是课程项目级别的做法，真实生产环境应使用 BCrypt 等带盐慢哈希。
 */
public final class Sha256Util {

    private static final char[] HEX = "0123456789abcdef".toCharArray();

    private Sha256Util() {
    }

    /**
     * 计算字符串的 SHA-256 哈希（UTF-8 编码）。
     *
     * @param text 原文
     * @return 64 位小写十六进制字符串
     */
    public static String hash(String text) {
        if (text == null) {
            throw new IllegalArgumentException("待哈希内容不能为 null");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(HEX[(b >> 4) & 0x0F]).append(HEX[b & 0x0F]);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // JDK 一定内置 SHA-256，正常不会走到这里
            throw new IllegalStateException("当前 JDK 不支持 SHA-256", e);
        }
    }

    /**
     * 校验明文密码与数据库中的哈希是否匹配。
     */
    public static boolean matches(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null) {
            return false;
        }
        return hash(rawPassword).equalsIgnoreCase(storedHash.trim());
    }
}
