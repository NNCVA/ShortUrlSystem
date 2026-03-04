package com.example.shorturl.service.impl;

import com.example.shorturl.dto.request.LoginRequest;
import com.example.shorturl.dto.request.RefreshTokenRequest;
import com.example.shorturl.dto.response.LoginResponse;
import com.example.shorturl.dto.response.RefreshTokenResponse;
import com.example.shorturl.dto.response.UserInfoResponse;
import com.example.shorturl.entity.User;
import com.example.shorturl.exception.BusinessException;
import com.example.shorturl.exception.ErrorCode;
import com.example.shorturl.mapper.UserMapper;
import com.example.shorturl.service.AuthService;
import com.example.shorturl.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 认证服务实现
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public LoginResponse login(LoginRequest request) {
        // 查询用户
        User user = userMapper.selectByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        // 生成 Token
        String token = jwtUtil.generateToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        // 构建用户信息响应
        UserInfoResponse userInfo = new UserInfoResponse();
        BeanUtils.copyProperties(user, userInfo);

        return new LoginResponse(token, refreshToken, userInfo);
    }

    @Override
    public RefreshTokenResponse refresh(RefreshTokenRequest request) {
        // 验证 Refresh Token
        Claims claims = jwtUtil.validateRefreshToken(request.getRefreshToken());
        if (claims == null) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }

        // 从 Refresh Token 获取用户信息
        Long userId = Long.parseLong(claims.getSubject());
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 生成新的 Access Token
        String newToken = jwtUtil.generateToken(user);

        return new RefreshTokenResponse(newToken);
    }

    @Override
    public UserInfoResponse getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        UserInfoResponse userInfo = new UserInfoResponse();
        BeanUtils.copyProperties(user, userInfo);
        return userInfo;
    }
}
