# 🚀 部署指南

## 环境准备

### 服务器最低配置

| 资源 | 最低要求 | 推荐配置 |
|------|----------|----------|
| CPU | 1 核 | 2 核 |
| 内存 | 2 GB | 4 GB |
| 磁盘 | 20 GB | 50 GB SSD |
| 操作系统 | Ubuntu 20.04 / CentOS 7 | Ubuntu 22.04 |

### 软件依赖

```bash
# Ubuntu/Debian
sudo apt update
sudo apt install -y openjdk-17-jdk mysql-server nginx nodejs npm maven

# CentOS/RHEL
sudo yum install -y java-17-openjdk mysql-server nginx nodejs npm maven
```

---

## 方案一：传统部署（Ubuntu）

### 1. 安装 MySQL 并创建数据库

```bash
sudo mysql -u root -p

CREATE DATABASE share CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'share'@'localhost' IDENTIFIED BY 'your_secure_password';
GRANT ALL PRIVILEGES ON share.* TO 'share'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

### 2. 克隆项目

```bash
git clone https://github.com/xiaoen2001/myhome.git /opt/myhome
cd /opt/myhome
```

### 3. 配置后端

```bash
cp share/src/main/resources/application-example.yml share/src/main/resources/application.yml
vim share/src/main/resources/application.yml
```

关键配置项：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/share?...   # 数据库连接
    username: share                                # 生产环境用专用账户
    password: your_secure_password
  mail:
    host: smtp.qq.com
    username: your_email@qq.com
    password: your_auth_code

jwt:
  secret: "生成一个长随机字符串"                     # openssl rand -base64 64

server:
  base-url: https://your-domain.com                # 改为实际域名

file:
  upload-dir: /opt/myhome/uploads                   # 上传文件路径
```

### 4. 构建后端

```bash
cd share
mvn clean package -DskipTests
```

构建产物：`target/share-0.0.1-SNAPSHOT.jar`

### 5. 构建前端

```bash
cd read/read
npm install
npm run build
```

构建产物：`dist/` 目录，复制到后端静态资源目录：

```bash
cp -r dist/* ../share/src/main/resources/static/
```

### 6. 创建 systemd 服务

```bash
sudo vim /etc/systemd/system/read-community.service
```

```ini
[Unit]
Description=Read Community Backend
After=network.target mysql.service

[Service]
User=www-data
WorkingDirectory=/opt/myhome
ExecStart=/usr/bin/java -jar /opt/myhome/share/target/share-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=10
Environment="JAVA_OPTS=-Xmx512m -Xms256m"

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl daemon-reload
sudo systemctl enable read-community
sudo systemctl start read-community
```

### 7. 配置 Nginx 反向代理

```bash
sudo vim /etc/nginx/sites-available/read-community
```

```nginx
server {
    listen 80;
    server_name your-domain.com;

    client_max_body_size 50M;

    # 前端静态文件
    location / {
        root /opt/myhome/share/src/main/resources/static;
        try_files $uri $uri/ /index.html;
    }

    # API 代理到后端
    location /api/ {
        proxy_pass http://localhost:8081/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # 上传文件
    location /uploads/ {
        proxy_pass http://localhost:8081/uploads/;
    }

    # WebSocket
    location /ws/ {
        proxy_pass http://localhost:8081/ws/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
```

```bash
sudo ln -s /etc/nginx/sites-available/read-community /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

### 8. 配置 HTTPS（可选，推荐）

```bash
sudo apt install certbot python3-certbot-nginx
sudo certbot --nginx -d your-domain.com
```

---

## 方案二：Docker 部署

### docker-compose.yml

```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.4
    environment:
      MYSQL_ROOT_PASSWORD: root_password
      MYSQL_DATABASE: share
      MYSQL_USER: share
      MYSQL_PASSWORD: share_password
    volumes:
      - mysql_data:/var/lib/mysql
    ports:
      - "3306:3306"

  backend:
    build: ./share
    ports:
      - "8081:8081"
    depends_on:
      - mysql
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/share?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
      SPRING_DATASOURCE_USERNAME: share
      SPRING_DATASOURCE_PASSWORD: share_password
      JWT_SECRET: your_jwt_secret
      FILE_UPLOAD_DIR: /app/uploads
    volumes:
      - uploads:/app/uploads

  frontend:
    build: ./read/read
    ports:
      - "80:80"
    depends_on:
      - backend

volumes:
  mysql_data:
  uploads:
```

---

## 生产环境 Checklist

- [ ] 数据库密码已修改为非默认值
- [ ] JWT secret 已生成为随机字符串（`openssl rand -base64 64`）
- [ ] 邮件授权码已配置
- [ ] `application.yml` 已从版本控制排除
- [ ] MySQL 用户权限已最小化
- [ ] Nginx 已配置 HTTPS
- [ ] 防火墙仅开放 80/443 端口
- [ ] 上传目录权限正确（`chmod 755 uploads/`）
- [ ] 后端日志已配置轮转（logback）
- [ ] 数据库已配置自动备份（crontab）

### 数据库备份脚本

```bash
#!/bin/bash
# /opt/scripts/backup-db.sh
mysqldump -u share -p'password' share | gzip > /opt/backups/share_$(date +%Y%m%d).sql.gz
find /opt/backups -name "*.sql.gz" -mtime +30 -delete
```

```bash
# crontab -e
0 3 * * * /opt/scripts/backup-db.sh
```

---

## 常见问题

### 端口被占用
```bash
lsof -i :8081
kill -9 <PID>
```

### MySQL 连接被拒
检查 `bind-address` 是否为 `127.0.0.1`，MySQL 8.4 默认认证插件改为 `caching_sha2_password`：
```sql
ALTER USER 'share'@'localhost' IDENTIFIED WITH mysql_native_password BY 'password';
```

### 前端 404
检查 Nginx `try_files` 配置，SPA 需要回退到 `index.html`。

### 文件上传失败
检查 `upload-dir` 目录是否存在且有写入权限。
