# 修复验证记录

日期：2026-09-13。

## 已执行

- JDK 8 + Maven 3.9.9，清理原编译类、测试类和展开的WAR目录后重新执行离线 `verify`，BUILD SUCCESS。
- MySQL 8.0.45 独立实例（127.0.0.1:13307）：23项测试，0失败，0错误，0跳过。
- H2 MySQL兼容模式：23项测试，0失败，0错误，0跳过。
- Tomcat 9.0.117 + 本机无头Chrome：11个JSP路由渲染通过，无页面脚本错误。
- 浏览器验证：存储型XSS输入按文本展示、编辑表单、multipart上传CSRF、角色限制、并发打卡、文件归属/回收站/恢复/彻底删除。
- WebSocket实测：匿名拒绝、路径身份伪造拒绝、跨源拒绝、JSON特殊字符、保存确认、重试去重、退出登录关闭连接。
- 新库schema执行成功；在测试库执行安全迁移脚本成功。没有对原业务库执行SQL。
- 10个应用JavaScript脚本语法检查通过；`git diff --check` 通过。
- 独立测试Tomcat与MySQL已停止。

## 构建产物

`target/home-office.war`

SHA256：
`a31babb904cab6895ef8d6d2b362a9b5c5b7437766e4498a7d64c2b1fe1e9003`

本地详细记录（target是构建目录，重新清理可能删除这些日志）：
- `target/final-verify.log`
- `target/default-tests.log`
- `target/browser-smoke.log`
- `target/surefire-reports/`

消息存储故障测试会故意输出“offline”异常日志；对应断言已通过，表示失败时没有错误发送成功ACK。

## 未执行及部署前事项

- 原业务数据库迁移、真实数据库凭据轮换、旧员工密码重置，需要按 SECURITY_FIXES.md 在实际环境执行。
- Maven Wrapper脚本及发行包SHA256已生成；首次下载验证因自动审批工具用量限制被拒绝，未完成。构建和测试使用已经下载的Maven完成。
- 本次未进行生产负载压测、多实例测试或真实反向代理HTTPS验证。
