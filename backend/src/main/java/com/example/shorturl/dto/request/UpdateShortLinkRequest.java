package com.example.shorturl.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新短链接请求
 */
@Schema(description = "更新短链接请求")
@Data
public class UpdateShortLinkRequest {
    /**
     * 短链接名称
     */
    @Schema(description = "短链接名称", example = "新名称")
    @Size(max = 100, message = "短链接名称不能超过100字符")
    private String name;

    /**
     * 原始长链接
     */
    @Schema(description = "原始长链接", example = "https://new-url.com")
    @Size(max = 2048, message = "原始链接不能超过2048字符")
    @Pattern(regexp = "^https?://.*", message = "原始链接必须以http://或https://开头")
    private String originalUrl;

    /**
     * 状态：ENABLED / DISABLED
     */
    @Schema(description = "状态", example = "DISABLED", allowableValues = {"ENABLED", "DISABLED"})
    @Pattern(regexp = "^(ENABLED|DISABLED)$", message = "状态只能是ENABLED或DISABLED")
    private String status;
}
