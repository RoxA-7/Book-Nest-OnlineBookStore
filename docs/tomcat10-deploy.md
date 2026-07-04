# Tomcat 10.1.54 + MySQL 部署说明

本项目使用 Tomcat 10.1.x 对应的 Jakarta EE 包名：

- `jakarta.servlet.*`
- Servlet API 6.0
- JSP API 3.1
- Jakarta JSTL 3.0

数据库使用 MySQL 8.x，通过 JDBC 连接。

## 1. 准备 MySQL 数据库

先登录 MySQL：

```bash
mysql -u root -p
```

创建数据库：

```sql
CREATE DATABASE IF NOT EXISTS online_bookstore
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;
```

也可以直接执行完整脚本：

```bash
mysql -u root -p < docs/database.sql
```

## 2. 构建 WAR

```bash
mvn clean package
```

如果本机没有全局 Maven，可以使用当前机器缓存的 Maven：

```powershell
C:\Users\29919\.m2\wrapper\dists\apache-maven-3.9.14-bin\1cb7fhup6b5n3bed6kckbrnspv\apache-maven-3.9.14\bin\mvn.cmd clean package
```

构建产物：

```text
target/online-bookstore.war
```

## 3. 配置 MySQL 账号密码

默认连接：

```text
jdbc:mysql://localhost:3306/online_bookstore?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true
```

默认账号：

```text
root
```

默认密码：

```text
123456
```

如果你的 MySQL 账号密码不同，推荐在 Tomcat 启动参数里加入：

```text
-Dbookstore.jdbc.user=root
-Dbookstore.jdbc.password=你的密码
```

Windows 下可以在 Tomcat 的 `bin/setenv.bat` 中写入：

```bat
set "CATALINA_OPTS=%CATALINA_OPTS% -Dbookstore.jdbc.user=root -Dbookstore.jdbc.password=你的密码"
```

如果 `setenv.bat` 不存在，可以新建。

## 4. 部署到 Tomcat 10.1.54

假设 Tomcat 解压目录为 `%CATALINA_HOME%`：

```powershell
Copy-Item target\online-bookstore.war "$env:CATALINA_HOME\webapps\online-bookstore.war" -Force
& "$env:CATALINA_HOME\bin\startup.bat"
```

访问地址：

```text
http://localhost:8080/online-bookstore/
```

管理员账号：

```text
admin / admin123
```

## 注意

Tomcat 10 使用 `jakarta.*` 包名，不能运行 `javax.servlet.*` 的旧版 WAR。Tomcat 9 才使用 `javax.servlet.*`。
