package com.example.shorturl.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建短链接请求
 */
@Schema(description = "创建短链接请求")
@Data
public class CreateShortLinkRequest {
    /**
     * 短链接名称
     */
    @Schema(description = "短链接名称", example = "官网首页", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "短链接名称不能为空")
    @Size(max = 100, message = "短链接名称不能超过100字符")
    private String name;

    /**
     * 原始长链接
     */
    @Schema(description = "原始长链接", example = "https://www.zstu.edu.cn", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "原始链接不能为空")
    @Size(max = 2048, message = "原始链接不能超过2048字符")
    @Pattern(regexp = "^https?://.*", message = "原始链接必须以http://或https://开头")
    private String originalUrl;
}
