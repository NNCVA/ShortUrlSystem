package com.example.shorturl.controller;

import com.example.shorturl.dto.request.LoginRequest;
import com.example.shorturl.dto.response.ApiResponse;
import com.example.shorturl.dto.response.LoginResponse;
import com.example.shorturl.dto.response.UserInfoResponse;
import com.example.shorturl.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Tag(name = "认证管理", description = "用户登录、登出、获取当前用户信息")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     */
    @Operation(
        summary = "用户登录",
        description = "使用用户名和密码登录，返回 JWT Token"
    )
    @SecurityRequirement(name = "")
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ApiResponse.success(response);
    }

    /**
     * 获取当前用户信息
     */
    @Operation(
        summary = "获取当前用户信息",
        description = "根据 Token 获取当前登录用户的详细信息"
    )
    @GetMapping("/me")
    public ApiResponse<UserInfoResponse> getCurrentUser(
        @Parameter(hidden = true) @RequestAttribute("userId") Long userId
    ) {
        UserInfoResponse response = authService.getCurrentUser(userId);
        return ApiResponse.success(response);
    }

    /**
     * 登出（前端清除Token，后端返回成功）
     */
    @Operation(
        summary = "用户登出",
        description = "登出当前用户（前端清除 Token）"
    )
    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        return ApiResponse.success();
    }
}
