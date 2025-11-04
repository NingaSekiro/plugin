package org.aopbuddy.plugin.infra.util;

import java.util.regex.Pattern;

public class DatabaseUtils {
    private static final Pattern INVALID_CHARS = Pattern.compile("[^a-zA-Z0-9_-]");
    private static final Pattern LEADING_INVALID = Pattern.compile("^[^a-zA-Z]");

    /**
     * 安全的数据库表名构造方法。
     * - 替换 - 等特殊符号为 _。
     * - 移除其他无效字符。
     * - 确保以字母开头。
     * - 转换为小写，限制长度 <= 64。
     *
     * @param input 输入字符串
     * @return 安全的表名
     */
    public static String safeTableName(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "table";  // 默认值，避免空表名
        }

        String safe = input.trim()
                .toLowerCase()  // 转换为小写
                .replaceAll("[-\\s./\\\\]", "_");  // 将 -、空格、点、斜杠等替换为 _

        // 移除其他无效字符（保留 a-z、0-9、_）
        safe = INVALID_CHARS.matcher(safe).replaceAll("");

        // 确保不为空且以字母开头
        if (safe.isEmpty()) {
            safe = "table";
        } else if (LEADING_INVALID.matcher(safe).find()) {
            safe = "t_" + safe;
        }

        // 限制长度
        if (safe.length() > 64) {
            safe = safe.substring(0, 64);
            // 确保结尾不是 _，如果需要
            safe = safe.replaceAll("_+$", "");
        }

        // 避免连续多个 _
        safe = safe.replaceAll("_+", "_");

        return safe;
    }
}