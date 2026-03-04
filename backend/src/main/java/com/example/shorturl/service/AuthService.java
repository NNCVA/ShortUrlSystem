package com.example.shorturl.service;

import com.example.shorturl.dto.request.LoginRequest;
import com.example.shorturl.dto.request.RefreshTokenRequest;
import com.example.shorturl.dto.response.LoginResponse;
import com.example.shorturl.dto.response.RefreshTokenResponse;
import com.example.shorturl.dto.response.UserInfoResponse;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest request);

    /**
     * 刷新 Access Token
     */
    RefreshTokenResponse refresh(RefreshTokenRequest request);

    /**
     * 获取当前用户信息
     */
    UserInfoResponse getCurrentUser(Long userId);
}
