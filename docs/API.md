# 📡 API 接口文档

## 基础信息

- **Base URL**: `http://localhost:8081/api`
- **认证方式**: JWT Bearer Token（Header: `Authorization: Bearer <token>`）
- **Content-Type**: `application/json;charset=UTF-8`
- **统一响应格式**:

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

| code | 含义 |
|------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未登录或 Token 过期 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 1. 认证模块 `/api/auth`

### 1.1 登录

```
POST /api/auth/login
```

**Request Body**:
```json
{
  "username": "alice",
  "password": "123456"
}
```

**Response**:
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "username": "alice",
    "nickname": "爱丽丝",
    "avatar": "http://...",
    "role": "admin",
    "token": "eyJhbGciOiJIUzM4NCJ9..."
  }
}
```

### 1.2 注册

```
POST /api/auth/register
```

**Request Body**:
```json
{
  "username": "newuser",
  "password": "123456",
  "email": "newuser@share.com",
  "nickname": "新用户"
}
```

### 1.3 修改密码

```
PUT /api/auth/password
```

**Request Body**:
```json
{
  "oldPassword": "123456",
  "newPassword": "newpassword"
}
```

### 1.4 发送验证码

```
POST /api/auth/reset-code
```

**Request Body**:
```json
{
  "email": "user@example.com"
}
```

### 1.5 验证验证码

```
POST /api/auth/confirmCode
```

**Request Body**:
```json
{
  "email": "user@example.com",
  "code": "123456"
}
```

### 1.6 重置密码

```
POST /api/auth/reset-password
```

**Request Body**:
```json
{
  "email": "user@example.com",
  "code": "123456",
  "newPassword": "newpassword"
}
```

---

## 2. 笔记模块 `/api/notes`

### 2.1 公开笔记列表

```
GET /api/notes?page=1&pageSize=10&keyword=人类简史&categoryId=3&sort=hot
```

**Response**:
```json
{
  "code": 200,
  "data": {
    "current": 1,
    "size": 10,
    "total": 36,
    "records": [
      {
        "id": 1,
        "title": "《活着》读后感",
        "bookName": "活着",
        "author": "余华",
        "cover": "http://...",
        "viewCount": 1256,
        "commentCount": 10,
        "likeCount": 11,
        "collectCount": 6,
        "tags": ["读书笔记"],
        "status": "published",
        "createdAt": "2025-01-02T00:00:00"
      }
    ]
  }
}
```

### 2.2 笔记详情

```
GET /api/notes/{id}
```

返回完整笔记内容、作者信息、当前用户的点赞/收藏状态。

### 2.3 推荐笔记

```
GET /api/notes/recommend?type=hot
```

| type 参数 | 含义 |
|-----------|------|
| `hot` | 按点赞数排序 |
| `mostCommented` | 按评论数排序 |
| `latest` | 按发布时间排序 |

### 2.4 创建笔记

```
POST /api/notes
```

**Request Body**:
```json
{
  "title": "Spring Boot从入门到精通",
  "bookName": "Spring Boot实战",
  "author": "Craig Walls",
  "content": "<h2>Spring Boot 核心概念</h2>...",
  "categoryId": 1,
  "tags": ["Java", "Spring Boot"],
  "visibility": "public",
  "cover": "data:image/jpeg;base64,..."
}
```

### 2.5 我的笔记

```
GET /api/notes/my?page=1&pageSize=10&status=published
```

### 2.6 修改笔记

```
PUT /api/notes/{id}
```

### 2.7 删除笔记

```
DELETE /api/notes/{id}
```

### 2.8 修改可见性

```
PUT /api/notes/{id}/visibility
```

```json
{ "visibility": "private" }
```

### 2.9 提交审核

```
PUT /api/notes/{id}/submit
```

---

## 3. 评论模块 `/api/comments`

### 3.1 获取笔记评论

```
GET /api/comments?noteId=1&page=1&pageSize=20
```

### 3.2 添加评论

```
POST /api/comments
```

**Request Body**:
```json
{
  "noteId": 1,
  "content": "写得很好，受益匪浅！"
}
```

---

## 4. 好友模块 `/api/friends`

### 4.1 好友列表

```
GET /api/friends
```

### 4.2 发送好友请求

```
POST /api/friends/request
```

```json
{ "toUserId": 5 }
```

### 4.3 收到的好友请求

```
GET /api/friends/requests/received
```

### 4.4 处理好友请求

```
PUT /api/friends/request/{id}
```

```json
{ "status": "accepted" }
```

### 4.5 删除好友

```
DELETE /api/friends/{friendId}
```

---

## 5. 聊天模块 `/api/messages`

### 5.1 聊天记录

```
GET /api/messages/{friendId}
```

### 5.2 发送消息

```
POST /api/messages/send
```

```json
{
  "receiverId": 4,
  "content": "你好，你的笔记写得真好！",
  "type": "text"
}
```

### 5.3 聊天列表

```
GET /api/messages/conversations
```

---

## 6. 通知模块 `/api/notifications`

### 6.1 通知列表

```
GET /api/notifications?page=1&pageSize=10
```

返回当前用户的个人通知 + 全局系统通知。

### 6.2 通知详情

```
GET /api/notifications/detail/{id}
```

---

## 7. 用户模块 `/api/users`

### 7.1 用户信息

```
GET /api/users/{id}
```

### 7.2 更新个人信息

```
PUT /api/users/profile
```

```json
{
  "nickname": "新昵称",
  "bio": "个人简介",
  "avatar": "http://..."
}
```

---

## 8. 轮播图模块 `/api/carousels`

### 8.1 轮播图列表

```
GET /api/carousels
```

---

## 9. 管理后台 `/api/admin/`

> 以下接口需要 `admin` 角色

### 9.1 统计仪表盘

```
GET /api/admin/stats
```

返回用户数、笔记数、评论数等统计数据。

### 9.2 趋势数据

```
GET /api/admin/trend/user
GET /api/admin/trend/note
GET /api/admin/trend/comment
GET /api/admin/trend/view
```

### 9.3 笔记管理

```
GET    /api/admin/notes?status=reviewing       # 列表
GET    /api/admin/notes/{id}                   # 详情
PUT    /api/admin/notes/{id}/approve            # 审核通过
PUT    /api/admin/notes/{id}/reject             # 驳回（发送个人通知）
PUT    /api/admin/notes/{id}/offline            # 下架
PUT    /api/admin/notes/{id}/ban                # 封禁
DELETE /api/admin/notes/{id}                   # 删除
```

### 9.4 评论管理

```
GET    /api/admin/comments                     # 列表
DELETE /api/admin/comments/{id}                # 删除（发送通知给评论者）
```

### 9.5 用户管理

```
GET    /api/admin/users                        # 列表
PUT    /api/admin/users/{id}                   # 更新信息
PUT    /api/admin/users/{id}/reset-password    # 重置密码
PUT    /api/admin/users/{id}/status            # 启用/禁用
```

### 9.6 轮播图管理

```
GET    /api/admin/carousels                    # 列表
POST   /api/admin/carousels                    # 新增
PUT    /api/admin/carousels/{id}               # 更新
DELETE /api/admin/carousels/{id}               # 删除
POST   /api/admin/carousels/upload             # 上传图片 (multipart/form-data)
DELETE /api/admin/carousels/new?url=...        # 删除临时图片
```

### 9.7 分类管理

```
GET    /api/admin/categories
POST   /api/admin/categories
PUT    /api/admin/categories/{id}
DELETE /api/admin/categories/{id}
```

### 9.8 标签管理

```
GET    /api/admin/tags
POST   /api/admin/tags
PUT    /api/admin/tags/{id}
DELETE /api/admin/tags/{id}
```

### 9.9 通知公告

```
GET    /api/admin/notifications
POST   /api/admin/notifications
DELETE /api/admin/notifications/{id}
```

---

## WebSocket 接口

### 聊天

```
ws://localhost:8081/ws/chat?userId={userId}&token={jwt}
```

服务器推送格式：
```json
{
  "type": "chat",
  "senderId": 1,
  "senderName": "alice",
  "content": "你好！",
  "timestamp": "2025-01-02T12:00:00"
}
```

### 通知

```
ws://localhost:8081/ws/notification?userId={userId}&token={jwt}
```

服务器推送格式：
```json
{
  "type": "system",
  "title": "审核通知",
  "message": "你的笔记已通过审核"
}
```
