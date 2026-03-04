package com.example.shorturl.filter;

import com.example.shorturl.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 获取请求路径
        String path = request.getRequestURI();

        // 排除不需要认证的路径
        if (isExcludedPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 从请求头获取 Token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendUnauthorizedError(response, "Token缺失");
            return;
        }

        String token = authHeader.substring(7);

        // 验证 Token
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            sendUnauthorizedError(response, "Token无效或已过期");
            return;
        }

        // 将用户信息写入 request attribute
        request.setAttribute("userId", userId);
        request.setAttribute("username", jwtUtil.getUsernameFromToken(token));
        request.setAttribute("role", jwtUtil.getRoleFromToken(token));

        filterChain.doFilter(request, response);
    }

    /**
     * 判断是否为排除路径
     */
    private boolean isExcludedPath(String path) {
        return path.equals("/api/auth/login")
            || path.startsWith("/api/s/")
            || path.startsWith("/swagger-ui")
            || path.startsWith("/v3/api-docs")
            || path.equals("/api-docs")
            || path.startsWith("/swagger-resources")
            || path.equals("/favicon.ico");
    }

    /**
     * 发送 401 错误响应
     */
    private void sendUnauthorizedError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(String.format(
                "{\"code\":401,\"message\":\"%s\",\"data\":null,\"timestamp\":\"%s\"}",
                message,
                java.time.LocalDateTime.now().toString()
        ));
    }
}
