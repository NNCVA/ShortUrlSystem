package com.example.shorturl.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 刷新 Token 响应
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenResponse {
    /**
     * 新的 Access Token
     */
    private String token;
}
