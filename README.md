<p align="center">
  <img src="read/read/public/icons.svg" width="80" alt="Read 社区 Logo" />
</p>

<h1 align="center">Read 社区 — 学生读书笔记分享平台</h1>

<p align="center">
  <strong>阅读 · 思考 · 分享</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-blue?logo=openjdk" alt="Java 17" />
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen?logo=springboot" alt="Spring Boot 3.5" />
  <img src="https://img.shields.io/badge/Vue-3.3-4FC08D?logo=vuedotjs" alt="Vue 3" />
  <img src="https://img.shields.io/badge/MySQL-8.4-4479A1?logo=mysql" alt="MySQL 8.4" />
  <img src="https://img.shields.io/badge/License-MIT-yellow" alt="License MIT" />
</p>

---

## 📖 项目简介

**Read 社区**是一个面向大学生的读书笔记分享平台。用户可以撰写读书笔记、发现好书、与书友交流，构建个人知识体系。平台支持笔记发布与审核流程、点赞收藏、好友聊天、系统通知等完整社交功能。

### 🎯 适用场景

- 大学生读书笔记管理与分享
- 校内读书社群建设
- 个人知识管理（PKM）
- 毕业设计 / 课程项目参考

---

## ✨ 核心功能

| 模块 | 功能 |
|------|------|
| 📝 笔记管理 | 富文本编辑器（wangEditor）、草稿/审核/发布流程、分类与标签、可见性控制 |
| 👥 社交互动 | 点赞、收藏、评论、好友系统、实时聊天（WebSocket） |
| 🔔 通知系统 | 全局公告 + 个人通知（审核结果、互动提醒）、WebSocket 实时推送 |
| 🛡️ 权限控制 | JWT 无状态认证、Spring Security、BCrypt 加密、角色分级（管理员/用户） |
| 📊 管理后台 | 数据仪表盘（ECharts）、笔记审核、用户管理、轮播图配置、分类标签管理 |
| 🎨 现代 UI | Vue 3 Composition API、Element Plus、响应式布局、Taste-Skill 校准配色 |

---

## 🏗️ 技术架构

```
┌─────────────────────────────────────────────┐
│              浏览器 (Vue 3 SPA)              │
│  Vite + Element Plus + Pinia + Vue Router   │
│              ↑ /api proxy                    │
│         后端 (Spring Boot 3.5)               │
│  Spring Security + JWT + MyBatis-Plus       │
│  WebSocket (聊天/通知) + RESTful API         │
│              ↑ JDBC                          │
│         数据库 (MySQL 8.4)                    │
└─────────────────────────────────────────────┘
```

### 技术栈详情

| 层级 | 技术 | 版本 | 说明 |
|------|------|------|------|
| 前端框架 | Vue 3 | 3.3+ | Composition API + `<script setup>` |
| 构建工具 | Vite | 4.4+ | 极速 HMR |
| UI 组件库 | Element Plus | 2.3+ | 企业级 Vue 3 组件库 |
| 富文本编辑器 | wangEditor | 5.1+ | 轻量级，支持 Vue 3 |
| 图表库 | ECharts | 5.4+ | 管理后台数据可视化 |
| 状态管理 | Pinia | 2.1+ | 官方推荐，支持持久化 |
| HTTP 客户端 | Axios | 1.5+ | 请求/响应拦截，JWT 自动注入 |
| 后端框架 | Spring Boot | 3.5.13 | Java 17，自动配置 |
| 安全框架 | Spring Security | 6.x | 无状态 JWT + BCrypt |
| ORM | MyBatis-Plus | 3.5+ | Lambda 查询、分页、自动填充 |
| 数据库 | MySQL | 8.4 | InnoDB，utf8mb4 |
| 实时通信 | WebSocket | - | 聊天 + 通知实时推送 |
| 邮件服务 | Spring Mail | - | QQ SMTP，验证码发送 |

---

## 🚀 快速开始

### 环境要求

| 工具 | 最低版本 |
|------|----------|
| JDK | 17 |
| MySQL | 8.0 |
| Node.js | 18 |
| Maven | 3.9 |

### 1. 克隆项目

```bash
git clone https://github.com/xiaoen2001/myhome.git
cd myhome
```

### 2. 初始化数据库

```sql
CREATE DATABASE share CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. 配置后端

```bash
cp share/src/main/resources/application-example.yml share/src/main/resources/application.yml
```

编辑 `application.yml` 填入数据库密码、邮箱授权码和 JWT 密钥：

```yaml
spring:
  datasource:
    password: your_db_password
  mail:
    username: your_email@qq.com
    password: your_qq_auth_code
jwt:
  secret: "your_random_secret_string"
```

### 4. 启动后端

```bash
cd share
mvn spring-boot:run
```

后端启动于 `http://localhost:8081`

### 5. 启动前端

```bash
cd read/read
npm install
npm run dev
```

前端启动于 `http://localhost:5174`

---

## 📁 项目结构

```
myhome/
├── read/read/                        # 前端 (Vue 3 + Vite)
│   └── src/
│       ├── api/modules/              # API 模块 (auth, note, comment...)
│       ├── components/app/           # 公共组件 (Header, Footer)
│       ├── router/                   # 路由配置
│       ├── stores/                   # Pinia 状态管理
│       ├── views/app/                # 前台页面
│       ├── views/admin/              # 管理后台页面
│       └── views/layout/             # 布局组件
│
├── share/share/                      # 后端 (Spring Boot)
│   └── src/main/java/com/share/
│       ├── common/                   # 通用类 (Result, 异常, 状态码)
│       ├── config/                   # Spring 配置 + WebSocket
│       ├── controller/admin/         # 管理端控制器
│       ├── entity/                   # 数据库实体
│       ├── mapper/                   # MyBatis-Plus Mapper
│       ├── service/impl/             # 服务层实现
│       └── util/                     # 工具类 (JWT, 文件上传, 验证码)
│
└── docs/                             # 项目文档
    ├── ARCHITECTURE.md
    ├── API.md
    ├── DATABASE.md
    └── DEPLOY.md
```

---

## 🔐 安全设计

| 安全措施 | 实现方式 |
|----------|----------|
| 密码加密 | BCrypt（Spring Security） |
| 身份认证 | JWT 无状态 Token，24 小时过期 |
| 权限控制 | admin / user 双角色，管理接口二次校验 |
| CORS | 白名单机制，仅允许开发端口 |
| SQL 注入 | MyBatis-Plus 参数化查询 |
| 敏感配置 | `application.yml` 已 gitignore，提供模板 |
| 文件上传 | 类型校验 + 大小限制 |

---

## 📚 文档

| 文档 | 内容 |
|------|------|
| [架构设计](docs/ARCHITECTURE.md) | 系统架构、技术选型、模块设计 |
| [API 参考](docs/API.md) | 完整 RESTful API 文档 |
| [数据库设计](docs/DATABASE.md) | ER 图、表结构、索引策略 |
| [部署指南](docs/DEPLOY.md) | 生产环境部署 |

---

## 📄 License

MIT License

---

<p align="center">Made with ❤️ for readers everywhere</p>
