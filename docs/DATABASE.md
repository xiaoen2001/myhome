# 🗄️ 数据库设计

## ER 图

```
┌──────────┐     ┌─────────────┐     ┌──────────┐
│   user   │────→│    note     │←────│ category │
└────┬─────┘     └──────┬──────┘     └──────────┘
     │                  │
     │            ┌─────┼─────┐
     │            ▼     ▼     ▼
     │     ┌─────────┐ ┌──────────┐ ┌─────────────┐
     │     │ comment │ │ like_rec │ │collect_record│
     │     └─────────┘ └──────────┘ └─────────────┘
     │
     ├──────→ friend
     ├──────→ friend_request
     ├──────→ message
     ├──────→ history
     └──────→ notification (per-user)
```

## 表结构

### 1. user — 用户表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 用户 ID |
| username | VARCHAR(50) | UNIQUE, NOT NULL | 用户名 |
| nickname | VARCHAR(50) | | 昵称 |
| password | VARCHAR(255) | NOT NULL | BCrypt 加密密码 |
| email | VARCHAR(100) | UNIQUE, NOT NULL | 邮箱 |
| avatar | VARCHAR(500) | | 头像 URL |
| role | ENUM('user','admin') | DEFAULT 'user' | 角色 |
| status | ENUM('active','disabled') | DEFAULT 'active' | 状态 |
| bio | VARCHAR(200) | | 个人简介 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 注册时间 |
| updated_at | DATETIME | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

### 2. note — 笔记表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK | 笔记 ID |
| user_id | BIGINT | FK → user.id | 作者 |
| category_id | INT | FK → category.id | 分类 |
| title | VARCHAR(200) | NOT NULL | 笔记标题 |
| book_name | VARCHAR(200) | NOT NULL | 书名 |
| author | VARCHAR(100) | | 图书作者 |
| cover | TEXT | | 封面图片 URL |
| content | LONGTEXT | NOT NULL | HTML 富文本内容 |
| view_count | INT | DEFAULT 0 | 浏览量 |
| comment_count | INT | DEFAULT 0 | 评论数（冗余） |
| like_count | INT | DEFAULT 0 | 点赞数（冗余） |
| collect_count | INT | DEFAULT 0 | 收藏数（冗余） |
| tags | JSON | | 标签数组 |
| status | ENUM('draft','reviewing','published','rejected','offline','banned') | DEFAULT 'draft' | 发布状态 |
| visibility | ENUM('public','private') | DEFAULT 'public' | 可见性 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**状态流转**:
```
draft → reviewing → published → offline
                    ↘ rejected ↗
published → banned
```

### 3. comment — 评论表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 评论 ID |
| note_id | BIGINT FK | 所属笔记 |
| user_id | BIGINT FK | 评论者 |
| username | VARCHAR(50) | 冗余：用户名 |
| user_nickname | VARCHAR(50) | 冗余：昵称 |
| user_avatar | VARCHAR(500) | 冗余：头像 |
| content | TEXT | 评论内容 |
| created_at | DATETIME | 创建时间 |

### 4. category — 分类表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INT PK | 分类 ID |
| name | VARCHAR(50) | 分类名 |
| sort | INT | 排序 |
| enabled | TINYINT(1) | 是否启用 |

### 5. tag — 标签表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INT PK | 标签 ID |
| name | VARCHAR(50) UNIQUE | 标签名 |
| sort_order | INT | 排序 |
| enabled | TINYINT(1) | 是否启用 |

### 6. like_record — 点赞记录

| 字段 | 类型 | 约束 |
|------|------|------|
| id | BIGINT PK | |
| user_id | BIGINT FK | |
| note_id | BIGINT FK | |
| created_at | DATETIME | |
| | UNIQUE(user_id, note_id) | 一人只能点赞一次 |

### 7. collect_record — 收藏记录

| 字段 | 类型 | 约束 |
|------|------|------|
| id | BIGINT PK | |
| user_id | BIGINT FK | |
| note_id | BIGINT FK | |
| created_at | DATETIME | |
| | UNIQUE(user_id, note_id) | 一人只能收藏一次 |

### 8. friend — 好友关系

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | |
| user_id | BIGINT FK | 用户 |
| friend_id | BIGINT FK | 好友 |
| friend_nickname | VARCHAR(50) | 冗余：好友昵称 |
| friend_avatar | VARCHAR(500) | 冗余：好友头像 |
| | UNIQUE(user_id, friend_id) | 双向存储 |

### 9. friend_request — 好友请求

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | |
| from_user_id | BIGINT FK | 发起者 |
| to_user_id | BIGINT FK | 接收者 |
| from_user_nickname | VARCHAR(50) | 冗余 |
| from_user_avatar | VARCHAR(500) | 冗余 |
| status | ENUM('pending','accepted','rejected') | |
| created_at | DATETIME | |

### 10. message — 聊天消息

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | |
| sender_id | BIGINT FK | 发送者 |
| receiver_id | BIGINT FK | 接收者 |
| type | ENUM('text','image') | 消息类型 |
| content | TEXT | 消息内容 |
| created_at | DATETIME | 发送时间 |

### 11. history — 浏览历史

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | |
| user_id | BIGINT FK | |
| note_id | BIGINT FK | |
| created_at | DATETIME | |
| | UNIQUE(user_id, note_id) | 去重 |

### 12. carousel — 轮播图

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | |
| image_url | VARCHAR(500) | 图片 URL |
| title | VARCHAR(100) | 标题 |
| sort_order | INT | 排序 |
| enabled | TINYINT(1) | 状态 |
| created_at | DATETIME | |
| updated_at | DATETIME | |

### 13. notification — 通知表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | |
| type | VARCHAR(20) | system / admin / like / comment |
| title | VARCHAR(200) | 通知标题 |
| content | VARCHAR(500) | 通知内容 |
| detail_id | BIGINT | FK → notification_detail.id |
| user_id | BIGINT NULL | NULL=全局通知，有值=个人通知 |
| create_time | DATETIME | |

### 14. notification_detail — 通知详情

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | |
| title | VARCHAR(200) | |
| content | TEXT | HTML 内容 |
| author | VARCHAR(100) | |
| status | VARCHAR(20) | published / draft |
| create_time | DATETIME | |

## 索引策略

| 表 | 索引 | 类型 | 用途 |
|------|------|------|------|
| user | uk_username | UNIQUE | 登录查询 |
| user | uk_email | UNIQUE | 邮箱查重 |
| note | idx_user | INDEX | "我的笔记"查询 |
| note | idx_status | INDEX | 审核列表过滤 |
| note | idx_created_at | INDEX | 时间排序 |
| comment | idx_note | INDEX | 笔记评论列表 |
| like_record | uk_user_note | UNIQUE | 去重 + 快速查询 |
| collect_record | uk_user_note | UNIQUE | 去重 + 快速查询 |
| friend | uk_user_friend | UNIQUE | 去重 |
| friend_request | idx_to_user | INDEX | 收到的请求 |
| carousel | idx_enabled_order | INDEX | 首页轮播排序 |
| message | idx_sender_receiver | INDEX | 聊天记录查询 |

## 关键设计决策

### 冗余字段

`comment` 和 `friend` 表冗余存储了用户名、昵称、头像等字段。

**理由**：避免多表 JOIN 查询，提升列表加载速度。用户名/头像变更时通过 SQL 批量更新冗余字段。

### 计数冗余

`note` 表冗余存储 `comment_count`、`like_count`、`collect_count`。

**理由**：避免 `COUNT(*)` 查询，通过触发器（Service 层更新）同步保证一致性。

### 通知分层

`notification` 表支持全局通知（`user_id IS NULL`）和个人通知（`user_id = 具体用户`），实现广播和定向推送的复用。
