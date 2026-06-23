# 🏗️ 系统架构设计

## 架构概览

```
┌──────────────────────────────────────────────────────────┐
│                      客户端层                             │
│  ┌─────────────────────┐  ┌──────────────────────────┐   │
│  │   Vue 3 SPA (前台)   │  │  管理后台 (Admin SPA)      │   │
│  │   Element Plus UI    │  │   ECharts + Element Plus │   │
│  └─────────┬───────────┘  └────────────┬─────────────┘   │
│            │          HTTP/REST         │                 │
├────────────┼───────────────────────────┼─────────────────┤
│            ▼                           ▼                  │
│  ┌──────────────────────────────────────────────────┐    │
│  │              Vite Dev Proxy (:5174)               │    │
│  │         /api → http://localhost:8081              │    │
│  └──────────────────┬───────────────────────────────┘    │
├─────────────────────┼────────────────────────────────────┤
│                     ▼                       应用层        │
│  ┌──────────────────────────────────────────────────┐    │
│  │           Spring Boot 3.5 (Tomcat :8081)         │    │
│  │                                                  │    │
│  │  ┌──────────┐ ┌──────────┐ ┌─────────────────┐  │    │
│  │  │Security  │ │JWT       │ │WebSocket        │  │    │
│  │  │Filter    │ │Interceptor│ │Handler          │  │    │
│  │  └──────────┘ └──────────┘ └─────────────────┘  │    │
│  │                                                  │    │
│  │  ┌──────────────────────────────────────────┐   │    │
│  │  │         Controller Layer (REST)           │   │    │
│  │  │  Auth / Note / Comment / Friend / Admin  │   │    │
│  │  └────────────────┬─────────────────────────┘   │    │
│  │                   ▼                              │    │
│  │  ┌──────────────────────────────────────────┐   │    │
│  │  │         Service Layer (业务逻辑)           │   │    │
│  │  │  INoteService / IUserService / ...       │   │    │
│  │  └────────────────┬─────────────────────────┘   │    │
│  │                   ▼                              │    │
│  │  ┌──────────────────────────────────────────┐   │    │
│  │  │         MyBatis-Plus (ORM)                │   │    │
│  │  │  LambdaQueryWrapper / Page / AutoFill     │   │    │
│  │  └────────────────┬─────────────────────────┘   │    │
│  └───────────────────┼─────────────────────────────┘    │
├───────────────────────┼──────────────────────────────────┤
│                       ▼                      数据层       │
│  ┌──────────────────────────────────────────────────┐    │
│  │              MySQL 8.4 (InnoDB)                   │    │
│  │  14 tables / utf8mb4 / auto_increment             │    │
│  └──────────────────────────────────────────────────┘    │
└──────────────────────────────────────────────────────────┘
```

## 层级设计

### 前端架构（MVVM）

```
View (Template) ←→ ViewModel (script setup) ←→ Model (Pinia Store)
     │                     │                          │
     │  Element Plus       │  Reactive State           │  API Modules
     │  Vue Router         │  Computed / Watch         │  Axios Instance
     └─────────────────────┴──────────────────────────┘
```

- **Vue 3 Composition API** + `<script setup>` 语法
- **Pinia** 管理全局用户状态，`pinia-plugin-persistedstate` 持久化到 localStorage
- **Vue Router** 懒加载路由，`beforeEach` 守卫校验登录状态
- **Axios** 拦截器自动注入 JWT Token，统一处理 401 过期跳转

### 后端架构（分层）

```
Controller → Service → Mapper → DB
     ↓           ↓         ↓
  参数校验    业务逻辑    SQL 映射
  JWT 解析   事务管理    Lambda 查询
```

- **Controller**：接收请求、校验权限、调用 Service
- **Service**：业务逻辑、事务管理、通知发送
- **Mapper**：MyBatis-Plus BaseMapper，Lambda 条件构造器

## 请求处理流程

### 认证流程

```
Client                    Server
  │                         │
  │  POST /api/auth/login   │
  │  {username, password}   │
  │────────────────────────►│
  │                         │ BCryptPasswordEncoder.matches()
  │                         │ JwtUtil.generateToken(userId, username)
  │  { code:200,            │
  │    data: { token } }    │
  │◄────────────────────────│
  │                         │
  │  GET /api/notes         │
  │  Authorization: Bearer  │
  │────────────────────────►│
  │                         │ JwtInterceptor.preHandle()
  │                         │ → 验证 Token 有效性
  │                         │ → 解析 userId → request.setAttribute
  │                         │
  │  { code:200, data }     │
  │◄────────────────────────│
```

### 审核流程

```
User 发布笔记 (status: reviewing)
  → Admin 列表看到待审核笔记
    → approve (status: published) → 通知作者"审核通过"
    → reject (status: rejected)   → 通知作者"审核未通过"
```

### WebSocket 实时通信

```
ChatWebSocketHandler           NotificationWebSocketHandler
       │                                │
       │  ws://host:8081/ws/chat        │  ws://host:8081/ws/notification
       │  ?userId={id}                  │  ?userId={id}
       │  &token={jwt}                  │  &token={jwt}
       │                                │
       ▼                                ▼
  Map<Long, Session>              Map<Long, Session>
  sendToUser / broadcast          sendToUser / broadcast
```

## 关键技术决策

| 决策 | 选择 | 理由 |
|------|------|------|
| 前端框架 | Vue 3 | 学习曲线平缓，适合学生项目 |
| UI 库 | Element Plus | 国内生态成熟，中文文档齐全 |
| 后端框架 | Spring Boot 3.5 | Java 生态最主流，企业级特性 |
| ORM | MyBatis-Plus | 比 JPA 灵活，Lambda 查询直观 |
| 认证方案 | JWT | 无状态，易扩展，适合前后端分离 |
| 实时通信 | WebSocket | Spring 原生支持，无需额外中间件 |
| 密码加密 | BCrypt | 行业标准，抗彩虹表 |

## 扩展性考量

- **前后端分离**：可独立部署、独立扩容
- **无状态 JWT**：支持多实例水平扩展
- **模块化 Service**：接口/实现分离，易于 Mock 测试
- **Vite 代理**：开发环境无缝切换后端地址
