-- ShortURL Pro 数据库建表脚本
-- 数据库：shorturl_db
-- 字符集：utf8mb4

create database IF NOT EXISTS shorturl_db;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(20) NOT NULL UNIQUE COMMENT '用户名，3-20字符',
    password VARCHAR(100) NOT NULL COMMENT 'BCrypt加密密码',
    role VARCHAR(10) NOT NULL DEFAULT 'USER' COMMENT '角色：ADMIN/USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 短链接表
CREATE TABLE IF NOT EXISTS short_links (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '短链接ID',
    name VARCHAR(100) NOT NULL COMMENT '短链接名称',
    original_url VARCHAR(2048) NOT NULL COMMENT '原始长链接',
    short_code VARCHAR(6) NOT NULL UNIQUE COMMENT '短码，6位Base62',
    status VARCHAR(10) NOT NULL DEFAULT 'ENABLED' COMMENT '状态：ENABLED/DISABLED',
    click_count INT DEFAULT 0 COMMENT '点击次数',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_short_code (short_code),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='短链接表';

-- 插入默认管理员账号
-- 用户名：admin
-- 密码：admin123456（BCrypt加密后）
INSERT INTO users (username, password, role)
VALUES ('admin', '$2a$10$OSTv6rCIyDYqsn.2HBnGMu04fb7odT7/gI4wMimeMaA7A27Kb6Eju', 'ADMIN')
ON DUPLICATE KEY UPDATE username=username;
