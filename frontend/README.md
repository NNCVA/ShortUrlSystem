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

## 后端依赖

前端项目依赖后端服务，需要以下环境：

- **JDK**: 17+
- **Node.js**: 18+
- **MySQL**: 8.0+（数据库）
- **Redis**: 6.0+（缓存）

启动前端前，请确保后端服务已启动并连接好 MySQL 和 Redis。

## 项目结构

```
frontend/
├── src/
│   ├── api/                 # API 接口封装
│   │   ├── http.ts         # Axios 封装 + 拦截器
│   │   ├── auth.ts         # 认证接口
│   │   └── shortlink.ts    # 短链接接口
│   ├── stores/             # Pinia 状态管理
│   │   └── auth.ts         # 认证状态 + Token 刷新
│   ├── router/             # 路由配置
│   │   └── index.ts        # 路由 + 守卫
│   ├── views/              # 页面组件
│   │   ├── Login.vue       # 登录页
│   │   ├── Demo.vue        # 演示页（公开）
│   │   ├── Admin.vue       # 管理后台
│   │   └── Redirect.vue    # 跳转页
│   ├── components/         # 公共组件（预留）
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
- JWT 双令牌机制：
  - Access Token：短期令牌，用于 API 请求认证
  - Refresh Token：长期令牌，用于刷新 Access Token
- Token 存储（localStorage 持久化）
- 401 自动检测并刷新 Token，刷新失败则跳转登录页

### 2. 演示页（公开）
- 输入长链接生成短码
- 自动从 URL 提取域名作为默认名称
- 一键复制短链接到剪贴板
- 无需登录即可使用

### 3. 管理后台（需登录）
- 短链接列表（分页、搜索、防抖查询）
- 新增短链接（带表单验证）
- 编辑短链接
- 删除短链接（带确认弹窗）
- 启用/禁用短链接
- 查看点击次数
- 用户头像显示（首字母）

### 4. 短链接跳转
- 访问 `/:shortCode` 自动跳转到原始链接（通过前端页面）
- 前端调用后端 API 获取原始 URL 后跳转
- 可隐藏后端服务端口，提升安全性

### 5. 路由守卫
- `/` → 重定向到 `/demo`
- `/login` → 登录页（已登录自动跳转管理页）
- `/demo` → 演示页（公开）
- `/admin` → 管理后台（需登录）
- `/:shortCode` → 跳转页（公开）

## UI 设计特色

采用现代玻璃拟态（Glass Morphism）设计风格：

### 配色方案
- **主色**：珊瑚粉（#FF7E9F）
- **辅色**：薄荷绿（#5AE4A8）
- **强调色**：薰衣草紫（#B794F6）
- **渐变按钮**：珊瑚粉 → 薄荷绿

### 设计风格
- 玻璃拟态背景（毛玻璃效果）
- 半透明模糊效果（backdrop-filter: blur）
- 柔和阴影与边框
- 圆角卡片设计

### 动画效果
- 卡片入场动画（淡入 + 上移）
- 脉冲动画（Logo 图标）
- 浮动圆形背景动画
- 按钮悬停效果

## 接口说明

### 认证接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/auth/login` | POST | 用户登录 |
| `/api/auth/me` | GET | 获取当前用户信息 |
| `/api/auth/logout` | POST | 用户登出 |
| `/api/auth/refresh` | POST | 刷新 Access Token |

### 短链接接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/links` | GET | 获取短链接列表（分页、搜索） |
| `/api/links` | POST | 创建短链接 |
| `/api/links/:id` | PUT | 更新短链接 |
| `/api/links/:id` | DELETE | 删除短链接 |
| `/api/links/:id/toggle` | PATCH | 启用/禁用短链接 |
| `/api/s/:shortCode` | GET | 获取原始URL（跳转接口） |

### 前端路由

| 路由 | 说明 |
|------|------|
| `/` | 重定向到 `/demo` |
| `/login` | 登录页 |
| `/demo` | 演示页（公开） |
| `/admin` | 管理后台（需登录） |
| `/:shortCode` | 短链接跳转页（公开） |

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
4. **Token 刷新**：
   - Access Token：有效期 30 分钟，用于 API 请求认证
   - Refresh Token：有效期 7 天，用于刷新 Access Token
   - 前端会自动检测 401 错误并尝试刷新，刷新失败则跳转登录页
5. **演示页特性**：自动从输入的 URL 提取域名作为短链接默认名称
6. **短链接生成**：演示页使用当前前端域名生成短链接，可隐藏后端服务端口
7. **搜索防抖**：管理后台搜索框有 300ms 防抖，避免频繁请求
8. **Redis 缓存**：后端使用 Redis 缓存短链接（按短码）和用户（按用户名），需确保 Redis 服务正常运行

## Docker 部署

### 构建 Docker 镜像

```bash
cd frontend
docker build -t shorturl-frontend:latest .
```

### 运行容器

```bash
docker run -d \
  --name shorturl-frontend \
  -p 80:80 \
  --network shorturl-network \
  shorturl-frontend:latest
```

### 端口说明

| 端口 | 说明 |
|------|------|
| 80 | Nginx 服务端口 |

### Nginx 配置说明

前端使用 Nginx 作为 Web 服务器，配置文件位于 `nginx.conf`：

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
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # 短链接跳转
    location /s/ {
        proxy_pass http://backend:8080/s/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_redirect off;
    }
}
```

Nginx 功能说明：
- 静态文件服务：托管 Vue 构建产物
- API 代理：将 `/api/*` 请求转发到后端
- 短链接跳转：将 `/s/*` 请求转发到后端

### 生产环境构建

```bash
# 构建生产版本
npm run build

# 构建 Docker 镜像
docker build -t shorturl-frontend:latest .
```

### 使用 Docker Compose 部署

项目根目录已提供 `docker-compose.yml`，可一键启动所有服务：

```bash
docker-compose up -d
```

详见项目根目录 `README.md` 中的 Docker 部署说明。

## 许可证

本项目为浙江理工大学数字化共享生产实践课程作业。
