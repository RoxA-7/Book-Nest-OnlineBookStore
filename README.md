# Online Bookstore

基于 JSP、Servlet、JavaBean、JDBC、MySQL 的在线书店实验项目。

## 功能

- 用户注册、登录、退出
- 图书分类浏览、搜索、精选图书展示
- 书单选书、数量统计、总价统计
- 管理员图书增删改查
- MySQL 数据库建表和演示数据初始化

## 技术栈

- JDK 11+
- Maven
- Tomcat 10.1.x
- Jakarta Servlet 6.0
- Jakarta JSP 3.1
- Jakarta JSTL 3.0
- JavaBean
- JDBC
- MySQL 8.x

## 数据库准备

先在 MySQL 中创建数据库：

```sql
CREATE DATABASE IF NOT EXISTS online_bookstore
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;
```

项目启动后会自动创建数据表并写入演示数据。完整 SQL 也可以直接执行：

```text
docs/database.sql
```

默认连接配置：

```text
url=jdbc:mysql://localhost:3306/online_bookstore?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true
user=root
password=123456
```

如果你的 MySQL 账号密码不同，需要通过 JVM 参数覆盖：

```bash
-Dbookstore.jdbc.user=root -Dbookstore.jdbc.password=你的密码
```

## 构建和部署

```bash
mvn clean package
```

把生成的 WAR 部署到 Tomcat 10.1.55：

```text
target/online-bookstore.war
```

访问地址：

```text
http://localhost:8080/online-bookstore/
```

默认管理员账号：

```text
admin / admin123
```
