package com.example.shorturl.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * 短码生成器（Base62编码）
 */
@Component
public class ShortCodeGenerator {

    private static final String BASE62_CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int SHORT_CODE_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * 生成6位短码
     */
    public String generate() {
        // 使用UUID生成随机数，然后转换为Base62
        String uuid = UUID.randomUUID().toString().replace("-", "");
        long value = Math.abs(uuid.hashCode());

        StringBuilder shortCode = new StringBuilder();
        while (shortCode.length() < SHORT_CODE_LENGTH) {
            shortCode.append(BASE62_CHARS.charAt((int) (value % 62)));
            value /= 62;

            // 如果value变为0，重新生成
            if (value == 0 && shortCode.length() < SHORT_CODE_LENGTH) {
                value = Math.abs(RANDOM.nextLong());
            }
        }

        return shortCode.substring(0, SHORT_CODE_LENGTH);
    }

    /**
     * 生成指定长度的短码
     */
    public String generate(int length) {
        StringBuilder shortCode = new StringBuilder();
        for (int i = 0; i < length; i++) {
            shortCode.append(BASE62_CHARS.charAt(RANDOM.nextInt(62)));
        }
        return shortCode.toString();
    }
}
