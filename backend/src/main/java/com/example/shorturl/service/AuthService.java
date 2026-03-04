package com.example.shorturl.service;

import com.example.shorturl.dto.request.LoginRequest;
import com.example.shorturl.dto.response.LoginResponse;
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
     * 获取当前用户信息
     */
    UserInfoResponse getCurrentUser(Long userId);
}
