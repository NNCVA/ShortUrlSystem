package com.example.shorturl.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
public class User {
    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名，3-20字符
     */
    private String username;

    /**
     * BCrypt加密密码（不返回给前端）
     */
    @JsonIgnore
    private String password;

    /**
     * 角色：ADMIN / USER
     */
    private String role;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}
