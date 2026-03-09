# ShortURL Pro

短链接管理系统

## 项目介绍

基于 Vue 3 + Spring Boot 的短链接管理平台，支持短链接生成、跳转管理、点击统计等功能。

## 页面预览

### 首页 - 短链接生成器

![首页](screenshots/home.png)

### 首页 - 生成结果

![首页生成结果](screenshots/home-result.png)

### 管理后台登录

![登录页](screenshots/login.png)

### 管理后台 - 短链接列表

![管理后台](screenshots/admin.png)

## 技术栈

### 前端
- Vue 3 + TypeScript
- Vite 5
- Pinia（状态管理）
- Element Plus（UI 组件库）
- Axios（HTTP 客户端）
- Mock.js（开发数据模拟）

### 后端
- Spring Boot 3
- MyBatis
- MySQL 8
- Redis + Spring Cache（缓存）
- JWT（认证）

## 项目结构

```
ShortUrlSystem/
├── backend/          # 后端项目
│   ├── src/
│   └── pom.xml
├── frontend/         # 前端项目
│   ├── src/
│   └── package.json
├── screenshots/                   # 页面截图
│   ├── home.png
│   ├── home-result.png
│   ├── login.png
│   └── admin.png
├── ShortURLPro接口文档v2.md    # 接口文档
└── README.md         # 本文件
```

## 快速开始

### 1. 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Redis 6.0+
- Maven 3.6+

### 2. 后端启动

```bash
cd backend
./mvnw spring-boot:run
```

后端默认运行在 `http://localhost:8080`

### 3. 前端启动

```bash
cd frontend
npm install
npm run dev
```

前端默认运行在 `http://localhost:5173`

### 4. 默认账号

- 用户名：`admin`
- 密码：`admin123456`

## 功能特性

- 用户登录认证（JWT 双令牌）
- 短链接生成与管理
- 短链接跳转（前端页面跳转）
- 点击数统计（异步更新）
- Redis 缓存优化
- Swagger API 文档

## 接口文档

详见 [ShortURLPro接口文档v2.md](ShortURLPro接口文档v2.md)

## Docker 部署

### 环境要求

- Docker 20.10+
- Docker Compose 2.0+

### 快速启动

```bash
# 克隆项目后，进入项目目录
cd ShortUrlSystem

# 一键启动所有服务
docker-compose up -d
```

### 服务访问

| 服务 | 地址 |
|------|------|
| 前端页面 | http://localhost |
| 后端 API | http://localhost:8080 |
| MySQL | localhost:3305 |
| Redis | localhost:6379 |

### 默认账号

- 用户名：`admin`
- 密码：`admin123456`

### 停止服务

```bash
docker-compose down
```

### 重新构建

```bash
docker-compose down
docker-compose build --no-cache
docker-compose up -d
```

### 服务说明

项目使用 Docker Compose 编排以下服务：

| 服务 | 镜像 | 端口 |
|------|------|------|
| MySQL | mysql:8.0 | 3305 |
| Redis | redis:7-alpine | 6379 |
| Backend | shorturl-backend | 8080 |
| Frontend | shorturl-frontend | 80 |

### 环境变量

后端服务可通过环境变量配置：

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| SPRING_DATASOURCE_URL | 数据库连接地址 | jdbc:mysql://mysql:3306/shorturl_db |
| SPRING_DATASOURCE_USERNAME | 数据库用户名 | root |
| SPRING_DATASOURCE_PASSWORD | 数据库密码 | root123456 |
| SPRING_DATA_REDIS_HOST | Redis 主机 | redis |
| SPRING_DATA_REDIS_PORT | Redis 端口 | 6379 |

### 数据持久化

- MySQL 数据：`mysql_data` 卷
- Redis 数据：`redis_data` 卷

### Nginx 配置

前端使用 Nginx 作为反向代理：

- 静态文件服务：`/` → 前端页面
- API 代理：`/api/*` → 后端 API
- 短链接跳转：`/s/*` → 后端跳转服务

详见 [frontend/nginx.conf](frontend/nginx.conf)

## 许可证

本项目为浙江理工大学数字化共享生产实践课程作业。
