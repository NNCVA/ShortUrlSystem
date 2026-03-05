# ShortURL Pro 后端项目

基于 Spring Boot 3 + MyBatis + MySQL 8 + JWT 的短链接管理系统后端。

## 技术栈

- **框架**: Spring Boot 3.2.5
- **数据库**: MySQL 8
- **ORM**: MyBatis 3.0.3
- **缓存**: Redis + Spring Cache
- **认证**: JWT (jjwt 0.12.5)
- **密码加密**: BCrypt (Spring Security Crypto)
- **参数校验**: Jakarta Validation
- **API文档**: SpringDoc OpenAPI (Swagger UI)
- **构建工具**: Maven
- **Java 版本**: 17

## 项目结构

```
backend/
├── src/main/java/com/example/shorturl/
│   ├── BackendApplication.java          # 主启动类
│   ├── entity/                          # 实体类
│   │   ├── User.java
│   │   └── ShortLink.java
│   ├── dto/                             # 数据传输对象
│   │   ├── request/
│   │   │   ├── LoginRequest.java
│   │   │   ├── RefreshTokenRequest.java
│   │   │   ├── CreateShortLinkRequest.java
│   │   │   └── UpdateShortLinkRequest.java
│   │   └── response/
│   │       ├── ApiResponse.java
│   │       ├── LoginResponse.java
│   │       ├── RefreshTokenReponse.java
│   │       ├── UserInfoResponse.java
│   │       └── ShortLinkResponse.java
│   ├── controller/                      # 控制器
│   │   ├── AuthController.java
│   │   └── ShortLinkController.java
│   ├── service/                         # 业务逻辑
│   │   ├── AuthService.java
│   │   ├── ShortLinkService.java
│   │   └── impl/
│   │       ├── AuthServiceImpl.java
│   │       └── ShortLinkServiceImpl.java
│   ├── mapper/                          # MyBatis Mapper
│   │   ├── UserMapper.java
│   │   └── ShortLinkMapper.java
│   ├── config/                          # 配置类
│   │   ├── CorsConfig.java
│   │   ├── WebConfig.java
│   │   ├── MyBatisConfig.java           # MyBatis 配置
│   │   ├── PasswordEncoderConfig.java
│   │   ├── OpenApiConfig.java           # Swagger/OpenAPI 配置
│   │   └── RedisConfig.java             # Redis 缓存配置
│   ├── filter/                          # 过滤器
│   │   └── JwtAuthenticationFilter.java
│   ├── util/                            # 工具类
│   │   ├── JwtUtil.java
│   │   └── ShortCodeGenerator.java
│   └── exception/                       # 异常处理
│       ├── GlobalExceptionHandler.java
│       ├── BusinessException.java
│       └── ErrorCode.java
├── src/main/resources/
│   ├── application.yml                  # 应用配置
│   ├── schema.sql                       # 数据库建表脚本
│   └── mapper/                          # MyBatis XML
│       ├── UserMapper.xml
│       └── ShortLinkMapper.xml
└── pom.xml                              # Maven 配置
```

## 快速开始

### 1. 环境要求

- JDK 17+
- MySQL 8.0+
- Redis 6.0+（用于缓存）
- Maven 3.6+

### 2. 数据库配置

创建数据库：

```sql
CREATE DATABASE shorturl_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

修改 `src/main/resources/application.yml` 中的数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/shorturl_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password  # 修改为你的密码
```

### 3. 启动项目

使用 Maven 启动：

```bash
cd backend
./mvnw spring-boot:run
```

或者使用 IDE 直接运行 `BackendApplication.java`。

项目启动后会自动执行 `schema.sql` 创建表并插入默认管理员账号。

### 4. 默认账号

- **用户名**: `admin`
- **密码**: `admin123456`
- **角色**: `ADMIN`

## API 接口

### 基础信息

- **Base URL**: `http://localhost:8080/api`
- **认证方式**: JWT Bearer Token
- **请求头**: `Authorization: Bearer <token>`

### 认证接口

#### 1. 登录
```
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123456"
}

响应:
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGc...",
    "refreshToken": "eyJhbGc...",
    "user": {
      "id": 1,
      "username": "admin",
      "role": "ADMIN",
      "createdAt": "2025-06-01T10:00:00"
    }
  },
  "timestamp": "2025-06-01T10:30:00"
}
```

#### 2. 获取当前用户
```
GET /api/auth/me
Authorization: Bearer <token>

响应:
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "admin",
    "role": "ADMIN",
    "createdAt": "2025-06-01T10:00:00"
  },
  "timestamp": "2025-06-01T10:30:00"
}
```

#### 3. 登出
```
POST /api/auth/logout
Authorization: Bearer <token>

响应:
{
  "code": 200,
  "message": "success",
  "data": null,
  "timestamp": "2025-06-01T10:30:00"
}
```

#### 4. 刷新 Access Token
```
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGc..."
}

响应:
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGc..."
  },
  "timestamp": "2025-06-01T10:30:00"
}
```

### 短链接接口

> **特性说明**：短链接跳转时使用 `@Async` 异步更新点击数，不阻塞主流程，提升访问性能。



#### 1. 创建短链接（可选认证）

```
POST /api/links
Authorization: Bearer <token>  # 可选，不传也可创建公开短链接
Content-Type: application/json

{
  "name": "官网首页",
  "originalUrl": "https://www.zstu.edu.cn"
}

响应:
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "官网首页",
    "originalUrl": "https://www.zstu.edu.cn",
    "shortCode": "rU8Vq1",
    "status": "ENABLED",
    "clickCount": 0,
    "createdAt": "2025-06-01T10:30:00",
    "updatedAt": "2025-06-01T10:30:00"
  },
  "timestamp": "2025-06-01T10:30:00"
}
```

#### 2. 获取短链接列表
```
GET /api/links?page=1&pageSize=10&keyword=官网&status=ENABLED
Authorization: Bearer <token>

响应:
{
  "code": 200,
  "message": "success",
  "data": {
    "items": [...],
    "total": 1,
    "page": 1,
    "size": 10
  },
  "timestamp": "2025-06-01T10:30:00"
}
```

#### 3. 获取单个短链接
```
GET /api/links/1
Authorization: Bearer <token>

响应: 同创建短链接响应
```

#### 4. 更新短链接
```
PUT /api/links/1
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "新名称",
  "originalUrl": "https://new-url.com",
  "status": "DISABLED"
}

响应: 同创建短链接响应
```

#### 5. 删除短链接
```
DELETE /api/links/1
Authorization: Bearer <token>

响应:
{
  "code": 200,
  "message": "success",
  "data": null,
  "timestamp": "2025-06-01T10:30:00"
}
```

#### 6. 切换短链接状态
```
PATCH /api/links/1/toggle
Authorization: Bearer <token>

响应: 同创建短链接响应
```

#### 7. 短链接跳转（无需认证）

**方式一：通过前端页面跳转（推荐）**

访问前端地址，前端会调用后端 API 获取原始 URL 并跳转：

```
GET /{shortCode}

示例: http://localhost:5173/rU8Vq1
```

这种方式生成的短链接格式为前端地址（如 `http://localhost:5173/rU8Vq1`），可以隐藏后端服务端口。

**方式二：直接调用后端 API**

```
GET /api/s/{shortCode}

响应（200）:
{
  "code": 200,
  "message": "success",
  "data": "https://www.zstu.edu.cn",
  "timestamp": "2025-06-01T10:30:00"
}
```

## 配置说明

### JWT 配置

在 `application.yml` 中配置：

```yaml
jwt:
  secret: your-256-bit-secret-key-change-this-in-production-environment-32chars
  expiration: 1800000        # 30分钟（毫秒）Access Token 有效期
  refresh-expiration: 604800000  # 7天（毫秒）Refresh Token 有效期
```

**说明：**
- Access Token：短期令牌，用于 API 认证，有效期 30 分钟
- Refresh Token：长期令牌，用于刷新 Access Token，有效期 7 天
- 前端会自动使用 Refresh Token 刷新过期的 Access Token

### Redis 配置

在 `application.yml` 中配置：

```yaml
spring.data.redis:
  host: localhost
  port: 6379
  database: 0
  lettuce.pool:
    max-active: 8
    max-idle: 8
    min-idle: 2
```

**缓存策略：**

| 缓存名称 | TTL | 说明 |
|---------|-----|------|
| shortLinkByCode | 10分钟 | 短链接按短码查询（高频访问） |
| userByUsername | 30分钟 | 用户按用户名查询（登录时使用） |

**注意**：首次启动前需确保 Redis 服务已启动。

**注意**: 生产环境请使用环境变量或配置中心管理 JWT Secret。

### CORS 配置

默认允许以下前端端口跨域访问，可在 `WebConfig.java` 中修改：

- `http://localhost:5173`
- `http://localhost:5174`
- `http://localhost:5175`

## 错误码说明

| Code | 含义 | 说明 |
|------|------|------|
| 200 | 成功 | 请求成功 |
| 400 | 参数错误 | 请求参数校验失败 |
| 401 | 未认证 | Token 缺失、无效或过期 |
| 403 | 无权限 | 没有访问权限 |
| 404 | 不存在 | 资源未找到 |
| 409 | 数据冲突 | 数据已存在 |
| 500 | 服务器错误 | 服务器内部错误 |

## 开发说明

### 添加新接口

1. 在 `controller` 包中创建控制器
2. 在 `service` 包中创建服务接口和实现
3. 在 `mapper` 包中创建 Mapper 接口
4. 在 `resources/mapper` 中创建 XML 映射文件
5. 如需排除 JWT 认证，在 `JwtAuthenticationFilter` 中添加排除路径

### 数据库迁移

修改 `schema.sql` 后，重启应用会自动执行（开发环境）。生产环境建议使用 Flyway 或 Liquibase。

## 测试

### 使用 Swagger UI 测试（推荐）

启动项目后，打开浏览器访问 Swagger UI：

```
http://localhost:8080/swagger-ui.html
```

#### 测试流程

1. **打开 Swagger UI**
   - 访问 `http://localhost:8080/swagger-ui.html`
   - 看到两个 API 分组：认证管理、短链接管理

2. **登录获取 Token**
   - 点击 `POST /api/auth/login` 接口
   - 点击 "Try it out" 按钮
   - 输入用户名 `admin`，密码 `admin123456`
   - 点击 "Execute"
   - 复制响应中的 `token` 值

3. **配置全局认证**
   - 点击页面右上角的 "Authorize" 按钮
   - 在弹出框中输入：`Bearer <token>`（注意 Bearer 后有空格）
   - 点击 "Authorize"
   - 点击 "Close"

4. **测试需要认证的接口**
   - 点击任意接口（如 `GET /api/auth/me`）
   - 点击 "Try it out"
   - 点击 "Execute"
   - 查看响应结果

5. **测试短链接接口**
   - 点击 `POST /api/links` 创建短链接
   - 输入：
     ```json
     {
       "name": "官网首页",
       "originalUrl": "https://www.zstu.edu.cn"
     }
     ```
   - 点击 "Execute"
   - 返回创建成功的短链接（包含 shortCode）

6. **测试跳转接口**
   - 复制上一步返回的 `shortCode`
   - 在浏览器新标签页访问：`http://localhost:8080/api/s/<shortCode>`
   - 应该自动跳转到原始 URL

### 使用 curl 测试

```bash
# 登录获取 Token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123456"}'

# 获取当前用户（替换 <token>）
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer <token>"

# 创建短链接
curl -X POST http://localhost:8080/api/links \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"name":"测试","originalUrl":"https://www.zstu.edu.cn"}'
```

### OpenAPI 文档

访问 OpenAPI JSON 文档：

```
http://localhost:8080/api-docs
```

## 许可证

本项目为浙江理工大学数字化共享生产实践课程作业。
