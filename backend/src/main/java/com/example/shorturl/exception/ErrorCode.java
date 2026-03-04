package com.example.shorturl.exception;

import lombok.Getter;

/**
 * 错误码枚举
 */
@Getter
public enum ErrorCode {
    // 成功
    SUCCESS(200, "success"),

    // 客户端错误 4xx
    BAD_REQUEST(400, "参数错误"),
    UNAUTHORIZED(401, "未认证"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "数据冲突"),

    // 业务错误
    USERNAME_EXISTS(409, "用户名已存在"),
    USER_NOT_FOUND(404, "用户不存在"),
    INVALID_CREDENTIALS(401, "用户名或密码错误"),
    TOKEN_INVALID(401, "Token无效或已过期"),
    SHORT_CODE_EXISTS(409, "短码已存在"),
    SHORT_LINK_NOT_FOUND(404, "短链接不存在"),
    SHORT_LINK_DISABLED(403, "短链接已禁用"),

    // 服务器错误 5xx
    INTERNAL_ERROR(500, "服务器内部错误");

    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
