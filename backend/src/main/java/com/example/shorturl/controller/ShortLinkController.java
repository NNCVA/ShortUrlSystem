package com.example.shorturl.controller;

import com.example.shorturl.dto.request.CreateShortLinkRequest;
import com.example.shorturl.dto.request.UpdateShortLinkRequest;
import com.example.shorturl.dto.response.ApiResponse;
import com.example.shorturl.entity.ShortLink;
import com.example.shorturl.service.ShortLinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

/**
 * 短链接控制器
 */
@Tag(name = "短链接管理", description = "短链接的增删改查、状态管理、跳转")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ShortLinkController {

    private final ShortLinkService shortLinkService;

    /**
     * 创建短链接
     */
    @Operation(summary = "创建短链接", description = "创建新的短链接，自动生成6位短码")
    @PostMapping("/links")
    public ApiResponse<ShortLink> create(@Valid @RequestBody CreateShortLinkRequest request) {
        ShortLink shortLink = shortLinkService.create(request);
        return ApiResponse.success(shortLink);
    }

    /**
     * 获取短链接列表（分页、搜索、状态筛选）
     */
    @Operation(summary = "获取短链接列表", description = "分页查询短链接列表，支持关键词搜索和状态筛选")
    @GetMapping("/links")
    public ApiResponse<Map<String, Object>> getList(
            @Parameter(description = "搜索关键词（匹配名称或URL）") @RequestParam(required = false) String keyword,
            @Parameter(description = "状态筛选（ENABLED/DISABLED）") @RequestParam(required = false) String status,
            @Parameter(description = "页码，从1开始") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size) {
        Map<String, Object> result = shortLinkService.getList(keyword, status, page, size);
        return ApiResponse.success(result);
    }

    /**
     * 获取单个短链接
     */
    @Operation(summary = "获取短链接详情", description = "根据ID获取单个短链接的详细信息")
    @GetMapping("/links/{id}")
    public ApiResponse<ShortLink> getById(
            @Parameter(description = "短链接ID") @PathVariable Long id) {
        ShortLink shortLink = shortLinkService.getById(id);
        return ApiResponse.success(shortLink);
    }

    /**
     * 更新短链接
     */
    @Operation(summary = "更新短链接", description = "更新短链接的名称、原始URL或状态")
    @PutMapping("/links/{id}")
    public ApiResponse<ShortLink> update(
            @Parameter(description = "短链接ID") @PathVariable Long id,
            @Valid @RequestBody UpdateShortLinkRequest request) {
        ShortLink shortLink = shortLinkService.update(id, request);
        return ApiResponse.success(shortLink);
    }

    /**
     * 删除短链接
     */
    @Operation(summary = "删除短链接", description = "根据ID删除短链接（物理删除）")
    @DeleteMapping("/links/{id}")
    public ApiResponse<Void> delete(
            @Parameter(description = "短链接ID") @PathVariable Long id) {
        shortLinkService.delete(id);
        return ApiResponse.success();
    }

    /**
     * 切换短链接状态
     */
    @Operation(summary = "切换短链接状态", description = "在 ENABLED 和 DISABLED 之间切换")
    @PatchMapping("/links/{id}/toggle")
    public ApiResponse<ShortLink> toggleStatus(
            @Parameter(description = "短链接ID") @PathVariable Long id) {
        ShortLink shortLink = shortLinkService.toggleStatus(id);
        return ApiResponse.success(shortLink);
    }

    /**
     * 短链接跳转（302重定向）
     */
    @Operation(
        summary = "短链接跳转",
        description = "根据短码跳转到原始URL，自动增加点击次数（302重定向）"
    )
    @SecurityRequirement(name = "")
    @GetMapping("/s/{shortCode}")
    public void redirect(
            @Parameter(description = "6位短码") @PathVariable String shortCode,
            HttpServletResponse response) throws IOException {
        String originalUrl = shortLinkService.getOriginalUrlByShortCode(shortCode);
        response.sendRedirect(originalUrl);
    }
}
