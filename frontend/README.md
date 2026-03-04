# ShortURL Pro 前端项目

基于 Vue 3 + TypeScript + Element Plus 的短链接管理系统前端。

## 技术栈

- **框架**: Vue 3.4
- **语言**: TypeScript
- **构建工具**: Vite 5
- **状态管理**: Pinia
- **路由**: Vue Router 4
- **HTTP 客户端**: Axios
- **UI 组件库**: Element Plus
- **Mock 数据**: Mock.js

## 项目结构

```
frontend/
├── src/
│   ├── api/                 # API 接口封装
│   │   ├── http.ts         # Axios 封装
│   │   ├── auth.ts         # 认证接口
│   │   └── shortlink.ts    # 短链接接口
│   ├── stores/             # Pinia 状态管理
│   │   └── auth.ts         # 认证状态
│   ├── router/             # 路由配置
│   │   └── index.ts        # 路由 + 守卫
│   ├── views/              # 页面组件
│   │   ├── Login.vue       # 登录页
│   │   ├── Demo.vue        # 演示页
│   │   ├── Admin.vue       # 管理后台
│   │   └── Redirect.vue    # 跳转页
│   ├── mocks/              # Mock 接口
│   │   ├── index.ts
│   │   ├── auth.ts
│   │   └── shortlink.ts
│   ├── main.ts             # 入口文件
│   ├── App.vue             # 根组件
│   └── env.d.ts            # 环境变量类型
├── index.html
├── vite.config.ts
├── tsconfig.json
├── package.json
└── .env.development
```

## 快速开始

### 安装依赖

```bash
cd frontend
npm install
```

### 启动开发服务器

```bash
npm run dev
```

访问 http://localhost:5173

### 构建生产版本

```bash
npm run build
```

## 功能说明

### 1. 登录认证
- 用户名/密码登录
- JWT Token 存储（localStorage 持久化）
- 401 自动跳转登录页

### 2. 演示页（公开）
- 输入长链接生成短码
- 复制短链接
- 无需登录

### 3. 管理后台（需登录）
- 短链接列表（分页、搜索）
- 新增短链接
- 编辑短链接
- 删除短链接
- 启用/禁用短链接
- 查看点击次数

### 4. 短链接跳转
- 访问 `/shortCode` 自动跳转到原始链接（通过前端页面）
- 前端调用后端 API `/api/s/{shortCode}` 获取原始 URL 后跳转
- 可隐藏后端服务端口，提升安全性

## 接口说明

### 认证接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/auth/login` | POST | 用户登录 |
| `/api/auth/me` | GET | 获取当前用户 |
| `/api/auth/logout` | POST | 用户登出 |

### 短链接接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/shortlinks` | GET | 获取短链接列表 |
| `/api/shortlinks` | POST | 创建短链接 |
| `/api/shortlinks/:id` | PUT | 更新短链接 |
| `/api/shortlinks/:id` | DELETE | 删除短链接 |
| `/api/shortlinks/:id/enable` | PATCH | 启用短链接 |
| `/api/shortlinks/:id/disable` | PATCH | 禁用短链接 |
| `/api/shortlinks/generate` | POST | 生成短码（公开） |
| `/:shortCode` | GET | 短链接跳转 |

## 配置说明

### 环境变量 (.env.development)

```bash
# API 基础路径
VITE_API_BASE=http://localhost:8080

# 是否启用 Mock（开发环境使用）
VITE_USE_MOCK=true
```

### 代理配置 (vite.config.ts)

开发环境下，Vite 代理将 `/api` 请求转发到后端：

```typescript
proxy: {
  '/api': {
    target: 'http://localhost:8080',
    changeOrigin: true
  }
}
```

## 注意事项

1. **Mock 模式**：开发环境默认启用 Mock，可通过修改 `.env.development` 中的 `VITE_USE_MOCK=false` 切换到真实后端
2. **默认账号**：admin / admin123456
3. **CORS**：后端需配置允许前端域名的跨域访问
4. **Token 过期**：Token 有效期 2 小时，过期后需重新登录

## 许可证

本项目为浙江理工大学数字化共享生产实践课程作业。
