package com.motorparts.common.util;

import cn.hutool.crypto.digest.BCrypt;

/**
 * 密码工具：统一使用 BCrypt 进行加密与校验。
 *
 * <p>BCrypt 自带随机盐，同一明文每次加密结果不同，校验时用 {@link #matches} 比对。
 * 数据库中存储的始终是 {@code $2a$...} 形式的哈希串，不再保存明文。</p>
 */
public final class PasswordUtil {

    private PasswordUtil() {
    }

    /**
     * 加密明文密码。
     *
     * @param rawPassword 明文密码
     * @return BCrypt 哈希串
     */
    public static String encode(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    /**
     * 校验明文密码是否与哈希匹配。
     *
     * @param rawPassword     明文密码
     * @param encodedPassword 数据库中存储的 BCrypt 哈希
     * @return 匹配返回 true
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null || encodedPassword.isEmpty()) {
            return false;
        }
        try {
            return BCrypt.checkpw(rawPassword, encodedPassword);
        } catch (IllegalArgumentException e) {
            // 历史明文密码等非法哈希格式，直接视为不匹配
            return false;
        }
    }

    /**
     * 判断字符串是否已是 BCrypt 哈希（用于避免重复加密）。
     *
     * @param value 待判断的字符串
     * @return 是 BCrypt 哈希返回 true
     */
    public static boolean isEncoded(String value) {
        return value != null
                && (value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$"));
    }

    public static void main(String[] args) {
        System.out.println(encode("123456"));
    }
}
