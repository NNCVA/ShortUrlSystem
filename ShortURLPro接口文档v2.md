# ShortURL Pro — 前后端接口文档

> **版本：** v2.0（含登录鉴权）　**技术栈：** Vue 3 + TypeScript · Spring Boot 3 · MyBatis · MySQL 8 · Redis · JWT
>
> 浙江理工大学 · 数字化共享生产实践课程

---

## 目录

1. [文档概述](#一文档概述)
2. [登录认证流程](#二登录认证流程)
3. [核心数据模型](#三核心数据模型)
4. [认证接口详细说明](#四认证接口详细说明)
5. [短链接接口详细说明](#五短链接接口详细说明)
6. [前端实现指南（Vue 3）](#六前端实现指南vue-3)
7. [后端实现指南（Spring Boot + MyBatis）](#七后端实现指南spring-boot--mybatis)
8. [接口速查总表](#八接口速查总表)
9. [docker部署](#九docker-部署指南)

---

## 一、文档概述

本文档定义 ShortURL Pro 的前后端接口规范，涵盖**登录认证**与**短链接管理**两大模块，供前后端同学各自独立开发并最终联调使用。

### 1.1 通信约定

| 项目 | 规范 |
|------|------|
| 请求基础路径 | `http://localhost:8080/api` |
| 数据格式 | Request / Response 均为 JSON，UTF-8 编码 |
| 认证方式 | JWT Bearer Token（登录后获取，写入请求头） |
| 请求头字段 | `Authorization: Bearer <token>` |
| Token 有效期 | Access Token 30 分钟，Refresh Token 7 天，支持自动刷新 |
| 时间格式 | ISO 8601：`2025-06-01T10:30:00` |
| 跨域 | 后端配置 CORS 允许 `http://localhost:5173` |

### 1.2 统一响应格式

所有接口（除 302 跳转外）均返回以下统一 JSON 结构：

```json
{
  "code":      200,
  "message":   "success",
  "data":      { ... },
  "timestamp": "2025-06-01T10:30:00"
}
```

| code | 含义 | 常见场景 |
|------|------|----------|
| `200` | 成功 | 正常返回 |
| `400` | 参数错误 | 字段为空、格式错误 |
| `401` | 未认证 | Token 缺失、过期、签名错误 |
| `403` | 无权限 | 普通用户访问管理接口 |
| `404` | 不存在 | 短码 / 记录未找到 |
| `409` | 数据冲突 | 用户名已存在 |
| `500` | 服务器异常 | 未知错误 |

---

## 二、登录认证流程

系统采用 **JWT（JSON Web Token）无状态认证**方案，整体流程如下：

```
① 用户在登录页输入用户名 + 密码
② 前端 POST /api/auth/login → 后端用 BCrypt 校验密码
③ 校验通过 → 后端签发 JWT Token（含 userId、username、role、过期时间）
④ 前端将 Token 存入 Pinia（+ localStorage），后续每次请求自动携带
⑤ 后端 JwtFilter 拦截请求 → 解析 Token → 写入 request attribute
⑥ Token 过期（401）→ 前端跳转登录页，清除本地 Token
```

### 2.1 JWT Token 结构

Token 由三部分组成（Base64 编码，以 `.` 分隔）：

```json
// Header
{ "alg": "HS256", "typ": "JWT" }

// Payload（自定义字段）
{
  "sub":      "1",         // userId
  "username": "admin",
  "role":     "ADMIN",     // ADMIN / USER
  "iat":      1717228800,  // 签发时间（Unix 秒）
  "exp":      1717236000   // 过期时间（iat + 7200s）
}

// Signature = HMACSHA256(base64(header) + '.' + base64(payload), SECRET_KEY)
```

> ⚠️ **`SECRET_KEY` 须保存在后端 `application.yml` 中，不可泄露到前端或 Git 仓库。建议长度 ≥ 32 字符。**

---

## 三、核心数据模型

### 3.1 User（用户表）

| 字段名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| `id` | number | 是 | 主键自增 | `1` |
| `username` | string | 是 | 用户名，唯一，3~20 字符 | `"admin"` |
| `password` | string | 是 | BCrypt 加密后的密码，**不返回给前端** | `"$2a$10$..."` |
| `role` | string | 是 | 角色枚举：`ADMIN` / `USER` | `"ADMIN"` |
| `createdAt` | string | 否 | 创建时间，后端自动赋值 | `"2025-06-01T10:00:00"` |

### 3.2 ShortLink（短链接记录）

| 字段名 | 类型 | 必填 | 说明                      | 示例值                             |
|--------|------|------|-------------------------|---------------------------------|
| `id` | number | 是 | 主键自增                    | `1`                             |
| `name` | string | 是 | 短链接名称，用于搜索              | `"官网首页"`                        |
| `originalUrl` | string | 是 | 原始长链接                   | `"https://www.zstu.edu.cn/..."` |
| `shortCode` | string | 是 | 短码，唯一，后端自动生成（6位 Base62） | `"rU8Vq1"`                    |
| `status` | string | 是 | `ENABLED` / `DISABLED`  | `"ENABLED"`                     |
| `clickCount` | number | 否 | 点击次数，后端累加，前端只读          | `128`                           |
| `createdAt` | string | 否 | 创建时间                    | `"2025-06-01T10:00:00"`         |
| `updatedAt` | string | 否 | 最近更新时间                  | `"2025-06-02T08:30:00"`         |

---

## 四、认证接口详细说明

### 4.1 `POST /api/auth/login` — 用户登录

> 🔓 **公开接口，无需携带 Token**

**请求体（JSON）**

| 字段名 | 类型 | 必填 | 校验规则 |
|--------|------|------|----------|
| `username` | string | 是 | 3~20 字符，只允许字母、数字、下划线 |
| `password` | string | 是 | 6~30 字符 |

**请求示例**

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123456"
}
```

**成功响应（200）**

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token":        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIi...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIi...",
    "user": {
      "id":       1,
      "username": "admin",
      "role":     "ADMIN",
      "createdAt": "2025-06-01T10:00:00"
    }
  }
}
```

**失败响应（401）**

```json
{
  "code": 401,
  "message": "用户名或密码错误",
  "data": null
}
```

---

### 4.2 `GET /api/auth/me` — 获取当前登录用户信息

> 🔒 **需要携带 Token**

**请求示例**

```http
GET /api/auth/me
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

**成功响应（200）**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id":        1,
    "username":  "admin",
    "role":      "ADMIN",
    "createdAt": "2025-06-01T10:00:00"
  }
}
```

**Token 无效 / 过期响应（401）**

```json
{
  "code": 401,
  "message": "Token 已过期，请重新登录",
  "data": null
}
```

---

### 4.3 `POST /api/auth/logout` — 登出

> 🔒 **需要携带 Token**

JWT 为无状态认证，后端登出接口仅做形式响应。**真正的登出由前端删除 Pinia / localStorage 中的 Token 完成。**

**成功响应（200）**

```json
{
  "code": 200,
  "message": "已退出登录",
  "data": null
}
```

---

### 4.4 `POST /api/auth/refresh` — 刷新 Access Token

> 🔓 **公开接口，无需携带 Token，使用 Refresh Token**

**请求体（JSON）**

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| `refreshToken` | string | 是 | Refresh Token（登录时获取） |

**请求示例**

```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIi..."
}
```

**成功响应（200）**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIi..."
  }
}
```

**失败响应（401）**

```json
{
  "code": 401,
  "message": "Token 无效或已过期",
  "data": null
}
```

---

## 五、短链接接口详细说明

> 🔒 **以下所有 `/api/links` 接口均需携带：`Authorization: Bearer <token>`，否则返回 401。**
>
> 仅 `POST /api/links`（演示页）和 `GET /api/s/:shortCode` 为公开接口。

---

### 5.1 `GET /api/links` — 查询列表

**Query 参数**

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| `keyword` | string | 否 | — | 按名称或 URL 模糊搜索 |
| `status` | string | 否 | — | 按状态筛选：`ENABLED` / `DISABLED` |
| `page` | number | 否 | `1` | 页码，从 1 开始 |
| `pageSize` | number | 否 | `10` | 每页条数 |

**请求示例**

```http
GET /api/links?keyword=官网&page=1&pageSize=10
Authorization: Bearer <token>
```

**成功响应（200）**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 25,
    "page": 1,
    "size": 10,
    "items": [
      {
        "id":          1,
        "name":        "官网首页",
        "originalUrl": "https://www.zstu.edu.cn/index.html",
        "shortCode":   "rU8Vq1",
        "status":      "ENABLED",
        "clickCount":  128,
        "createdAt":   "2025-06-01T10:00:00",
        "updatedAt":   "2025-06-01T10:00:00"
      }
    ]
  }
}
```

---

### 5.2 `POST /api/links` — 新增

**请求体（JSON）**

| 字段名 | 类型 | 必填 | 校验规则 |
|--------|------|------|----------|
| `name` | string | 是 | 1~50 字符 |
| `originalUrl` | string | 是 | 合法 URL（`http` / `https` 开头） |

**请求示例**

```http
POST /api/links
Authorization: Bearer <token>
Content-Type: application/json

{
  "name":        "官网首页",
  "originalUrl": "https://www.zstu.edu.cn/index.html"
}
```

**成功响应（200）**

```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id":          10,
    "name":        "官网首页",
    "originalUrl": "https://www.zstu.edu.cn/index.html",
    "shortCode":   "Kx3mZpAB",
    "status":      "ENABLED",
    "clickCount":  0,
    "createdAt":   "2025-06-01T14:00:00",
    "updatedAt":   "2025-06-01T14:00:00"
  }
}
```

**失败响应（400）**

```json
{
  "code": 400,
  "message": "originalUrl 格式不正确，请输入完整的 http/https 链接",
  "data": null
}
```

---

### 5.3 `PUT /api/links/:id` — 编辑

**请求示例**

```http
PUT /api/links/10
Authorization: Bearer <token>
Content-Type: application/json

{
  "name":        "官网首页（修改后）",
  "originalUrl": "https://www.zstu.edu.cn/new-page"
}
```

**成功响应（200）**

```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id":          10,
    "name":        "官网首页（修改后）",
    "originalUrl": "https://www.zstu.edu.cn/new-page",
    "shortCode":   "Kx3mZpAB",
    "status":      "ENABLED",
    "updatedAt":   "2025-06-02T08:30:00"
  }
}
```

---

### 5.4 `DELETE /api/links/:id` — 删除

**请求示例**

```http
DELETE /api/links/10
Authorization: Bearer <token>
```

**成功响应（200）**

```json
{ "code": 200, "message": "删除成功", "data": null }
```

**失败响应（404）**

```json
{ "code": 404, "message": "短链接不存在或已被删除", "data": null }
```

---

### 5.5 `PATCH /api/links/:id/toggle` — 切换状态

> 无需请求体。切换短链接的启用/禁用状态。

```http
PATCH /api/links/10/toggle
Authorization: Bearer <token>
```

```json
{ "code": 200, "message": "success", "data": { "id": 10, "status": "ENABLED" } }
```

---

### 5.6 `POST /api/links` — 演示页生成短码

> 🔓 **公开接口，无需 Token**

**请求示例**

```http
POST /api/links
Content-Type: application/json

{
  "name":        "官网首页",
  "originalUrl": "https://www.zstu.edu.cn/very/long/path"
}
```

**成功响应（200）**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id":          1,
    "name":        "官网首页",
    "originalUrl": "https://www.zstu.edu.cn/very/long/path",
    "shortCode":   "Kx3mZpAB",
    "status":      "ENABLED",
    "clickCount":  0,
    "createdAt":   "2025-06-01T14:00:00",
    "updatedAt":   "2025-06-01T14:00:00"
  }
}
```

---

### 5.7 `GET /api/s/:shortCode` — 获取原始URL

> 🔓 **公开接口，无需 Token**

直接调用后端 API 获取原始 URL：

```http
GET /api/s/Kx3mZpAB
```

**成功响应（200）**

```json
{
  "code": 200,
  "message": "success",
  "data": "https://www.zstu.edu.cn",
  "timestamp": "2025-06-01T10:30:00"
}
```

| 情况 | HTTP 状态 | 行为 |
|------|-----------|------|
| 短码存在且 `ENABLED` | `200` | 返回 JSON，前端获取原始 URL 后跳转 |
| 短码存在但 `DISABLED` | `403` | 返回 JSON：链接已被禁用 |
| 短码不存在 | `404` | 返回 JSON：链接不存在 |

> **推荐跳转方式**：使用前端页面跳转（访问 `http://localhost:5173/Kx3mZpAB`），可隐藏后端端口。

---

## 六、前端实现指南（Vue 3）

### 6.1 推荐目录结构

```
src/
├── api/                 # API 接口封装
│   ├── http.ts          # Axios 封装 + 拦截器
│   ├── auth.ts          # 登录 / 登出 / me 接口
│   └── shortlink.ts     # 短链接 CRUD 接口
├── stores/              # Pinia 状态管理
│   └── auth.ts          # 认证状态 + Token 刷新
├── router/              # 路由配置
│   └── index.ts         # 路由 + 守卫
├── views/               # 页面组件
│   ├── Login.vue        # 登录页
│   ├── Demo.vue         # 演示页（公开）
│   ├── Admin.vue        # 管理后台
│   └── Redirect.vue     # 跳转页
├── mocks/               # Mock 接口
│   ├── index.ts
│   ├── auth.ts
│   └── shortlink.ts
├── main.ts              # 入口文件
├── App.vue              # 根组件
└── env.d.ts             # 环境变量类型
```

### 6.2 Pinia 认证 Store

```typescript
// src/stores/auth.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { AuthApi } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const token    = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<any>(JSON.parse(localStorage.getItem('userInfo') || 'null'))

  const isLogin = computed(() => !!token.value)

  async function login(username: string, password: string) {
    const res = await AuthApi.login({ username, password })
    token.value    = res.data.token
    userInfo.value = res.data.userInfo
    localStorage.setItem('token',    token.value)
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
  }

  function logout() {
    token.value    = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  return { token, userInfo, isLogin, login, logout }
})
```

### 6.3 Axios 封装（Token 注入 + 401 自动跳转）

```typescript
// src/api/http.ts
import axios from 'axios'
import router from '@/router'
import { useAuthStore } from '@/stores/auth'

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || 'http://localhost:8080',
  timeout: 10000,
})

// 请求拦截：自动注入 Token
http.interceptors.request.use(config => {
  const auth = useAuthStore()
  if (auth.token) {
    config.headers.Authorization = `Bearer ${auth.token}`
  }
  return config
})

// 响应拦截：统一处理 401
http.interceptors.response.use(
  res => {
    if (res.data.code !== 200) return Promise.reject(new Error(res.data.message))
    return res.data
  },
  err => {
    if (err.response?.status === 401) {
      useAuthStore().logout()
      router.push('/login')
    }
    return Promise.reject(err)
  }
)

export default http
```

### 6.4 路由守卫

```typescript
// src/router/index.ts
import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: () => import('@/pages/login/LoginPage.vue') },
    { path: '/demo',  component: () => import('@/pages/demo/DemoPage.vue') },
    {
      path: '/admin',
      component: () => import('@/pages/admin/ListPage.vue'),
      meta: { requiresAuth: true }
    },
  ]
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta.requiresAuth && !auth.isLogin) {
    return '/login'
  }
})

export default router
```

### 6.5 业务接口封装

```typescript
// src/api/auth.ts
import http from './http'

export const AuthApi = {
  login:  (data: { username: string; password: string }) => http.post('/api/auth/login', data),
  me:     () => http.get('/api/auth/me'),
  logout: () => http.post('/api/auth/logout'),
}
```

```typescript
// src/api/shorturl.ts
import http from './http'

export const ShortUrlApi = {
  list:     (params?: { name?: string; page?: number; pageSize?: number }) =>
              http.get('/api/shortlinks', { params }),
  create:   (data: { name: string; originalUrl: string }) =>
              http.post('/api/shortlinks', data),
  update:   (id: number, data: { name: string; originalUrl: string }) =>
              http.put(`/api/shortlinks/${id}`, data),
  remove:   (id: number) => http.delete(`/api/shortlinks/${id}`),
  enable:   (id: number) => http.patch(`/api/shortlinks/${id}/enable`),
  disable:  (id: number) => http.patch(`/api/shortlinks/${id}/disable`),
  generate: (originalUrl: string) => http.post('/api/shortlinks/generate', { originalUrl }),
}
```

### 6.6 Mock 接口（含登录）

```typescript
// src/mocks/auth.ts
import Mock from 'mockjs'

Mock.mock('/api/auth/login', 'post', (req: any) => {
  const { username, password } = JSON.parse(req.body)
  if (username === 'admin' && password === 'admin123456') {
    return {
      code: 200, message: '登录成功',
      data: {
        token:     'mock-jwt-token-' + Date.now(),
        tokenType: 'Bearer',
        expiresIn: 7200,
        userInfo:  { id: 1, username: 'admin', role: 'ADMIN' }
      }
    }
  }
  return { code: 401, message: '用户名或密码错误', data: null }
})

Mock.mock('/api/auth/me', 'get', () => ({
  code: 200, message: 'success',
  data: { id: 1, username: 'admin', role: 'ADMIN', createdAt: '2025-06-01T10:00:00' }
}))
```

```typescript
// src/mocks/index.ts
import Mock from 'mockjs'
import './auth'
import './shorturl'

Mock.setup({ timeout: '100-300' })
```

```typescript
// .env.development
VITE_USE_MOCK=true

// main.ts
if (import.meta.env.VITE_USE_MOCK === 'true') {
  import('./mocks/index')
}
```

---

## 七、后端实现指南（Spring Boot + MyBatis）

### 7.1 数据库建表 SQL

```sql
CREATE DATABASE IF NOT EXISTS shorturl_db CHARACTER SET utf8mb4;
USE shorturl_db;

-- 用户表
CREATE TABLE users (
  id         BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  username   VARCHAR(50)  NOT NULL UNIQUE COMMENT '用户名',
  password   VARCHAR(100) NOT NULL COMMENT 'BCrypt 加密密码',
  role       VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT 'ADMIN/USER',
  created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 初始管理员（密码 admin123456，已 BCrypt 加密）
INSERT INTO users(username, password, role)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'ADMIN');

-- 短链接表
CREATE TABLE short_links (
  id           BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name         VARCHAR(50)   NOT NULL COMMENT '名称',
  original_url VARCHAR(2048) NOT NULL COMMENT '原始长链接',
  short_code   VARCHAR(16)   NOT NULL UNIQUE COMMENT '短码',
  status       VARCHAR(10)   NOT NULL DEFAULT 'ENABLED',
  click_count  BIGINT        NOT NULL DEFAULT 0,
  created_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### 7.2 Maven 依赖（pom.xml 新增部分）

```xml
<!-- MyBatis Spring Boot Starter -->
<dependency>
  <groupId>org.mybatis.spring.boot</groupId>
  <artifactId>mybatis-spring-boot-starter</artifactId>
  <version>3.0.3</version>
</dependency>

<!-- MySQL 驱动 -->
<dependency>
  <groupId>com.mysql</groupId>
  <artifactId>mysql-connector-j</artifactId>
  <scope>runtime</scope>
</dependency>

<!-- JWT（jjwt 0.12.5） -->
<dependency>
  <groupId>io.jsonwebtoken</groupId>
  <artifactId>jjwt-api</artifactId>
  <version>0.12.5</version>
</dependency>
<dependency>
  <groupId>io.jsonwebtoken</groupId>
  <artifactId>jjwt-impl</artifactId>
  <version>0.12.5</version>
  <scope>runtime</scope>
</dependency>
<dependency>
  <groupId>io.jsonwebtoken</groupId>
  <artifactId>jjwt-jackson</artifactId>
  <version>0.12.5</version>
  <scope>runtime</scope>
</dependency>

<!-- BCrypt（仅引入 spring-security-crypto，无需完整 Spring Security） -->
<dependency>
  <groupId>org.springframework.security</groupId>
  <artifactId>spring-security-crypto</artifactId>
</dependency>

<!-- Redis Support -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<!-- Spring Cache抽象 -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-cache</artifactId>
</dependency>

<!-- Redis连接池 -->
<dependency>
  <groupId>org.apache.commons</groupId>
  <artifactId>commons-pool2</artifactId>
</dependency>
```

### 7.3 application.yml 配置

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/shorturl_db?useUnicode=true&characterEncoding=utf8
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver

mybatis:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: com.example.shorturl.entity
  configuration:
    map-underscore-to-camel-case: true   # 下划线自动转驼峰（short_code → shortCode）

jwt:
  secret: your-256-bit-secret-key-change-this-in-production-environment-32chars
  expiration: 1800000      # 30分钟（毫秒）Access Token 有效期
  refresh-expiration: 604800000  # 7天（毫秒）Refresh Token 有效期                     # 单位：秒（2 小时）

server:
  port: 8080

# Redis 配置
spring.data.redis:
  host: localhost
  port: 6379
  database: 1
  lettuce.pool:
    max-active: 8
    max-idle: 8
    min-idle: 2
```

### 7.4 项目结构

```
src/main/java/com/example/shorturl/
├── controller/           # 控制器
│   ├── AuthController.java
│   └── ShortLinkController.java
├── service/              # 业务逻辑
│   ├── AuthService.java
│   ├── ShortLinkService.java
│   └── impl/            # 实现类
├── mapper/               # MyBatis Mapper
│   ├── UserMapper.java
│   └── ShortLinkMapper.java
├── entity/               # 实体类
│   ├── User.java
│   └── ShortLink.java
├── dto/                  # 数据传输对象
│   ├── request/
│   └── response/
├── config/                # 配置类
│   ├── CorsConfig.java
│   ├── WebConfig.java
│   ├── MyBatisConfig.java
│   ├── PasswordEncoderConfig.java
│   ├── OpenApiConfig.java
│   └── RedisConfig.java  # Redis 缓存配置
├── filter/                # 过滤器
│   └── JwtAuthenticationFilter.java
├── util/                  # 工具类
│   ├── JwtUtil.java
│   └── ShortCodeGenerator.java
└── exception/             # 异常处理
    ├── GlobalExceptionHandler.java
    ├── BusinessException.java
    └── ErrorCode.java

src/main/resources/
├── mapper/                # MyBatis XML
├── schema.sql             # 数据库建表脚本
└── application.yml
```

### 7.5 JWT 工具类

```java
// JwtUtil.java
@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    /**
     * 生成密钥
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

}
```

### 7.6 JWT 过滤器

```java
// JwtFilter.java
@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired JwtUtil jwtUtil;

    // 白名单路径（不需要 Token）
    private static final List<String> WHITE_LIST = List.of(
        "/api/auth/login",
        "/api/shortlinks/generate"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return WHITE_LIST.contains(path) || request.getMethod().equals("OPTIONS");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
            HttpServletResponse res, FilterChain chain) throws ... {
        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                String token  = header.substring(7);
                Claims claims = jwtUtil.parseToken(token);
                // 将用户信息写入 request attribute，供 Controller 使用
                req.setAttribute("userId",   Long.valueOf(claims.getSubject()));
                req.setAttribute("username", claims.get("username"));
                req.setAttribute("role",     claims.get("role"));
            } catch (JwtException e) {
                res.setStatus(401);
                res.setContentType("application/json;charset=UTF-8");
                res.getWriter().write("{\"code\":401,\"message\":\"Token无效或已过期\"}");
                return;
            }
        } else {
            // 未携带 Token 且不在白名单 → 401
            res.setStatus(401);
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("{\"code\":401,\"message\":\"请先登录\"}");
            return;
        }
        chain.doFilter(req, res);
    }
}
```

### 7.7 AuthService — 登录逻辑

```java
// AuthService.java
@Service
public class AuthService {
    @Autowired UserMapper userMapper;
    @Autowired JwtUtil    jwtUtil;

    private final BCryptPasswordEncoder bCrypt = new BCryptPasswordEncoder();

    public LoginResponse login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user == null || !bCrypt.matches(password, user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        String token = jwtUtil.generateToken(
            user.getId(), user.getUsername(), user.getRole()
        );
        return new LoginResponse(token, "Bearer", 7200,
                   new UserInfo(user.getId(), user.getUsername(), user.getRole()));
    }
}
```

### 7.8 MyBatis Mapper 示例

```java
// UserMapper.java
@Mapper
public interface UserMapper {
    User findByUsername(@Param("username") String username);
    User findById(@Param("id") Long id);
    void insert(User user);
}
```

```xml
<!-- resources/mapper/UserMapper.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
  "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.example.shorturl.mapper.UserMapper">

  <select id="findByUsername" resultType="User">
    SELECT id, username, password, role, created_at
    FROM users WHERE username = #{username}
  </select>

  <select id="findById" resultType="User">
    SELECT id, username, role, created_at
    FROM users WHERE id = #{id}
  </select>

</mapper>
```

### 7.9 CORS + 过滤器注册配置

```java
// WebConfig.java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired JwtFilter jwtFilter;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins("http://localhost:5173")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
            .allowedHeaders("*")
            .exposedHeaders("Authorization");
    }

    @Bean
    public FilterRegistrationBean<JwtFilter> jwtFilterBean() {
        FilterRegistrationBean<JwtFilter> bean = new FilterRegistrationBean<>(jwtFilter);
        bean.addUrlPatterns("/api/*");   // 只拦截 /api/ 路径
        return bean;
    }
}
```

### 7.10 短码生成算法

```java
private static final String BASE62 =
    "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

private String generateShortCode() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 8; i++) {
        int idx = (int)(Math.random() * BASE62.length());
        sb.append(BASE62.charAt(idx));
    }
    return sb.toString();
}
// 生成后在数据库查重，冲突则重新生成
```

### 7.11 鉴权白名单总结

| 路径 | 方法 | 说明 |
|------|------|------|
| `/api/auth/login` | `POST` | 登录接口，公开 |
| `/api/shortlinks/generate` | `POST` | 演示页生成，公开 |
| `/:shortCode` | `GET` | 302 跳转，公开，挂在根路径 |
| `/api/**` | `OPTIONS` | CORS 预检请求，直接放行 |

---

## 八、接口速查总表

| 方法 | 路径 | 需 Token | 功能 | 前端对应 |
|------|------|:--------:|------|----------|
| `POST` | `/api/auth/login` | ❌ | 用户登录，返回 JWT | 登录页提交 |
| `GET` | `/api/auth/me` | ✅ | 获取当前用户信息 | 页面初始化 |
| `POST` | `/api/auth/logout` | ✅ | 登出（前端清 Token） | 导航栏退出按钮 |
| `POST` | `/api/auth/refresh` | ❌ | 刷新 Access Token | 401 自动刷新 |
| `GET` | `/api/links?keyword=&page=&pageSize=` | ✅ | 查询列表（可搜索） | 列表页加载 / 搜索 |
| `POST` | `/api/links` | ✅ | 新增短链接 | 新增弹窗提交 |
| `PUT` | `/api/links/:id` | ✅ | 编辑短链接 | 编辑弹窗提交 |
| `DELETE` | `/api/links/:id` | ✅ | 删除短链接 | 删除确认 |
| `PATCH` | `/api/links/:id/toggle` | ✅ | 切换状态 | 行内切换按钮 |
| `POST` | `/api/links` | ❌ | 演示页生成短码 | 演示页「生成」按钮 |
| `GET` | `/:shortCode` | ❌ | 前端页面跳转 | 浏览器直接访问（推荐） |
| `GET` | `/api/s/:shortCode` | ❌ | API 跳转 | 返回 JSON |

---

### 7.12 Redis 缓存策略

系统使用 Redis 缓存热点数据，提升访问性能：

| 缓存名称 | TTL | 说明 |
|---------|-----|------|
| `shortLinkByCode` | 10分钟 | 短链接按短码查询（高频访问） |
| `userByUsername` | 30分钟 | 用户按用户名查询（登录时使用） |

**缓存失效时机：**
- 更新短链接 → 清除 `shortLinkByCode` 缓存
- 删除短链接 → 清除 `shortLinkByCode` 缓存
- 切换状态 → 清除 `shortLinkByCode` 缓存

**注意：** 首次启动前需确保 Redis 服务已运行。

---

## 九、Docker 部署指南

### 9.1 环境要求

- Docker 20.10+
- Docker Compose 2.0+

### 9.2 快速启动

```bash
# 进入项目目录
cd ShortUrlSystem

# 一键启动所有服务
docker-compose up -d
```

### 9.3 服务访问

| 服务 | 地址 |
|------|------|
| 前端页面 | http://localhost |
| 后端 API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| MySQL | localhost:3305 |
| Redis | localhost:6379 |

### 9.4 默认账号

- 用户名：`admin`
- 密码：`admin123456`

### 9.5 Docker Compose 配置说明

项目使用 `docker-compose.yml` 编排以下服务：

```yaml
services:
  mysql:
    image: mysql:8.0
    # ...

  redis:
    image: redis:7-alpine
    # ...

  backend:
    build: ./backend
    # ...

  frontend:
    build: ./frontend
    # ...
```

### 9.6 后端环境变量

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| SPRING_DATASOURCE_URL | 数据库连接地址 | jdbc:mysql://mysql:3306/shorturl_db |
| SPRING_DATASOURCE_USERNAME | 数据库用户名 | root |
| SPRING_DATASOURCE_PASSWORD | 数据库密码 | root123456 |
| SPRING_DATA_REDIS_HOST | Redis 主机 | redis |
| SPRING_DATA_REDIS_PORT | Redis 端口 | 6379 |

### 9.7 Nginx 配置说明

前端使用 Nginx 作为反向代理（`frontend/nginx.conf`）：

```nginx
server {
    listen 80;
    server_name localhost;

    # 前端静态文件
    location / {
        root /usr/share/nginx/html;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    # API 代理 - 后端服务
    location /api/ {
        proxy_pass http://backend:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # 短链接跳转
    location /s/ {
        proxy_pass http://backend:8080/s/;
        proxy_set_header Host $host;
        proxy_redirect off;
    }
}
```

### 9.8 常用命令

```bash
# 启动服务
docker-compose up -d

# 停止服务
docker-compose down

# 查看日志
docker-compose logs -f

# 重新构建镜像
docker-compose build --no-cache

# 查看服务状态
docker-compose ps
```

### 9.9 数据持久化

- MySQL 数据卷：`mysql_data`
- Redis 数据卷：`redis_data`

如需重置数据，可删除对应卷：

```bash
docker-compose down -v
```

---


