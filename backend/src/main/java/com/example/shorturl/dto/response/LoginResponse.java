package com.example.shorturl.dto.response;

import com.example.shorturl.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录响应
 */
@Data
@AllArgsConstructor
public class LoginResponse {
    /**
     * JWT Token
     */
    private String token;

    /**
     * 用户信息
     */
    private UserInfoResponse user;
}
