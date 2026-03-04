package com.example.shorturl.dto.response;

import com.example.shorturl.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    /**
     * JWT Access Token
     */
    private String token;

    /**
     * JWT Refresh Token
     */
    private String refreshToken;

    /**
     * 用户信息
     */
    private UserInfoResponse user;
}
