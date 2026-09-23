# 居家办公系统（SSM）

基于 Spring MVC、MyBatis/JdbcTemplate、MySQL、JSP 的单体应用，包含组织管理、考勤、日程、文件柜和即时聊天。

## 环境与构建

- JDK 8（必须是 JDK，JAVA_HOME 不能指向 JRE）
- MySQL 8
- Tomcat 9（javax Servlet/WebSocket；不要直接使用 Jakarta 版 Tomcat 10+）
- 项目附带 Maven Wrapper，不依赖系统安装 Maven；首次构建需要访问 Maven Central
- 浏览器依赖已包含在 WAR 中，不需要访问外部 CDN

```powershell
# 在项目目录执行
.\mvnw.cmd verify
```

Linux/macOS：`sh mvnw verify`。产物为 `target/home-office.war`。
默认测试使用内存数据库，不访问业务数据库。

## 初始化与配置

**已有数据库请阅读 [安全修复与迁移说明](docs/SECURITY_FIXES.md)，不要直接执行新建库脚本。**

新环境创建空数据库后，执行 `src/main/resources/db/schema.sql`。
为运行 Tomcat 的进程配置以下环境变量：

```text
OFFICE_DB_URL=jdbc:mysql://localhost:3306/reception_db?serverTimezone=Asia/Shanghai&characterEncoding=utf8
OFFICE_DB_USER=<应用数据库账号>
OFFICE_DB_PASSWORD=<数据库密码>
OFFICE_UPLOAD_DIR=<存储文件的绝对路径，例如 D:/office-data/files>
```

配置缺失或关键唯一约束缺失时应用拒绝启动。应用账号只需业务表的 DML 权限，迁移使用独立的 DDL 账号。
本地开发也可将 `office-local.properties.example` 复制为项目根目录的 `office-local.properties` 并填写配置；该文件不会进入 Git 或 WAR。
本地 MySQL 使用 `caching_sha2_password` 且未启用 TLS 时，如果出现 `Public Key Retrieval is not allowed`，可按本地模板在 JDBC URL 中加入 `allowPublicKeyRetrieval=true`。模板中的 `useSSL=false` 和公钥获取选项仅用于本机开发；远程或生产数据库应配置可信证书并使用 `sslMode=VERIFY_IDENTITY`，不要照搬本地连接参数。
IDEA Smart Tomcat 可在 VM options 设置 `-Doffice.config=本地配置文件的绝对路径`，避免启动工作目录不同导致找不到配置。环境变量和 JVM 系统属性优先于此文件。
生产环境通过 HTTPS 访问，并在容器/反向代理中正确配置安全 Cookie 与代理协议。

完成构建后，使用控制台工具初始化管理员密码：

```powershell
java -cp "target/classes;target/home-office/WEB-INF/lib/*" com.yourcompany.reception.util.AccountPasswordTool admin
```

工具从控制台隐藏读取密码，至少 12 个字符，UTF-8 最多 72 字节。非交互环境可以临时提供 `OFFICE_ACCOUNT_PASSWORD`，使用后删除该变量。不再提供固定管理员密码。

将 WAR 部署到 Tomcat 9，访问 `http://localhost:8080/home-office/hello`。
如果使用项目现有的 Smart Tomcat 配置，Context path 是 `/demo-1`，访问 `http://localhost:8080/demo-1/hello`；根路径 `/demo-1/` 也会跳转到登录页。
若两个地址均返回404，先查看 Smart Tomcat 的 localhost 日志：Tomcat 进程运行不代表应用初始化成功。本机 MySQL80 服务需要处于运行状态。
管理员登录名为 `admin`，员工登录名为注册得到的数字 ID。
首次启动前请确认 `OFFICE_UPLOAD_DIR` 可读写且不位于 Web 根目录。

## 验证

- `mvnw.cmd verify`：权限、CSRF、登录会话、密码哈希、并发考勤、文件补偿、消息可靠性测试。
- `node scripts/verify-js.cjs`：检查应用 JavaScript 语法。
- 真实 MySQL 和 Chrome/Tomcat 回归说明见 [SECURITY_FIXES.md](docs/SECURITY_FIXES.md)。

当前为单实例部署。提醒任务仍是原有占位实现，不能作为“后台通知已实现”展示。
