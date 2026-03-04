package com.example.shorturl.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 短链接响应
 */
@Data
public class ShortLinkResponse {
    /**
     * 短链接ID
     */
    private Long id;

    /**
     * 短链接名称
     */
    private String name;

    /**
     * 原始长链接
     */
    private String originalUrl;

    /**
     * 短码
     */
    private String shortCode;

    /**
     * 状态：ENABLED / DISABLED
     */
    private String status;

    /**
     * 点击次数
     */
    private Integer clickCount;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
