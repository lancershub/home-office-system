<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
<%@ include file="security.jspf" %>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>访客个人中心 - 居家办公通</title>
    <link href="${pageContext.request.contextPath}/lib/bootstrap/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/lib/bootstrap-icons/font/bootstrap-icons.css">

    <style>
        /* 布局样式控制 */
        body { background-color: #f4f6f9; overflow-x: hidden; }

        .wrapper { display: flex; width: 100%; align-items: stretch; }

        /* 侧边栏样式 */
        #sidebar {
            min-width: 250px;
            max-width: 250px;
            background: #2c3e50; /* 深蓝色调 */
            color: #fff;
            transition: all 0.3s;
            min-height: 100vh;
        }

        #sidebar .sidebar-header { padding: 20px; background: #1a252f; text-align: center; }
        #sidebar ul.components { padding: 20px 0; border-bottom: 1px solid #47748b; }
        #sidebar ul li a {
            padding: 15px 25px;
            display: block;
            color: #bdc3c7;
            text-decoration: none;
            font-size: 1.1em;
        }
        #sidebar ul li a:hover { color: #fff; background: #34495e; }
        #sidebar ul li.active > a { color: #fff; background: #198754; } /* 激活状态为绿色 */

        /* 内容区样式 */
        #content { width: 100%; padding: 0; }
        .top-nav { background: #fff; padding: 15px 30px; box-shadow: 0 2px 5px rgba(0,0,0,0.05); }
        .page-body { padding: 40px; }
        .clock-card { transition: transform 0.2s; border-radius: 20px !important; }
        .clock-card:hover { transform: translateY(-5px); }
    </style>
</head>
<body>

<div class="wrapper">
    <nav id="sidebar" class="shadow">
        <div class="sidebar-header">
            <h3><i class="bi bi-laptop me-2"></i>办公系统</h3>
        </div>

        <ul class="list-unstyled components">
            <li class="active">
                <a href="#"><i class="bi bi-clock-history me-2"></i>考勤打卡</a>
            </li>
            <li>
                <a href="${pageContext.request.contextPath}/schedule/view"><i class="bi bi-calendar3 me-2"></i>日程管理</a>
            </li>
            <li>
                <a href="${pageContext.request.contextPath}/file/view"><i class="bi bi-folder2 me-2"></i>文件柜橱</a>
            </li>
        </ul>

        <div class="px-4 mt-5">
            <form action="logout.action" method="post">
<input type="hidden" name="${_csrf.parameterName}" value="<c:out value='${_csrf.token}'/>"><button type="submit" class="btn btn-outline-light w-100 btn-sm">
                <i class="bi bi-box-arrow-right me-1"></i>退出系统
            </button></form>
        </div>
    </nav>

    <div id="content">
        <div class="top-nav d-flex justify-content-between align-items-center">
            <h5 class="mb-0 text-secondary">个人中心 / 考勤打卡</h5>
            <div class="user-info">
                <span class="badge bg-light text-dark border p-2">
                    <i class="bi bi-person-circle me-1"></i> 登录ID: ${sessionScope.visitorId}
                </span>
            </div>
        </div>

        <div class="page-body">
            <div class="row justify-content-center">
                <div class="col-lg-10">

                    <div class="card border-0 shadow-sm rounded-4 mb-4">
                        <div class="card-body p-4">
                            <h4 class="fw-bold"><c:out value="${msg}"/></h4>
                            <p class="text-muted mb-0">今天是全新的一天，祝您办公愉快！</p>
                        </div>
                    </div>

                    <div class="card clock-card border-0 shadow rounded-4 text-center p-5">
                        <h3 class="mb-4"><i class="bi bi-clock-history text-primary me-2"></i>今日考勤打卡</h3>

                        <c:choose>
                            <%-- 状态1：还没打上班卡 --%>
                            <c:when test="${empty todayRecord}">
                                <p class="text-muted fs-5 mb-4">您今天还没有打卡哦</p>
                                <form action="clockIn" method="post">
<input type="hidden" name="${_csrf.parameterName}" value="<c:out value='${_csrf.token}'/>">
                                    <button type="submit" class="btn btn-success btn-lg rounded-pill px-5 shadow py-3 fs-5">
                                        <i class="bi bi-box-arrow-in-right me-2"></i>上 班 打 卡
                                    </button>
                                </form>
                            </c:when>

                            <%-- 状态2：打过上班卡，该打下班卡了 --%>
                            <c:when test="${empty todayRecord.clock_out_time}">
                                <div class="alert alert-success d-inline-block px-4 py-2 rounded-pill mb-4 fs-5">
                                    <i class="bi bi-check-circle-fill me-2"></i>上班时间：<strong>${todayRecord.clock_in_time}</strong>
                                </div>
                                <p class="text-warning fw-bold fs-5 mb-4">辛苦了！记得下班前打卡哦</p>
                                <form action="clockOut" method="post">
<input type="hidden" name="${_csrf.parameterName}" value="<c:out value='${_csrf.token}'/>">
                                    <button type="submit" class="btn btn-warning btn-lg rounded-pill px-5 shadow py-3 fs-5 text-dark">
                                        <i class="bi bi-box-arrow-right me-2"></i>下 班 打 卡
                                    </button>
                                </form>
                            </c:when>

                            <%-- 状态3：今日已完成 --%>
                            <c:otherwise>
                                <div class="d-flex justify-content-center gap-3 mb-4 fs-5">
                                    <span class="badge bg-success bg-opacity-10 text-success border border-success px-4 py-2 rounded-pill">
                                        上班：${todayRecord.clock_in_time}
                                    </span>
                                    <span class="badge bg-warning bg-opacity-10 text-warning border border-warning px-4 py-2 rounded-pill">
                                        下班：${todayRecord.clock_out_time}
                                    </span>
                                </div>
                                <button class="btn btn-secondary btn-lg rounded-pill px-5 py-3 fs-5" disabled>
                                    <i class="bi bi-check2-all me-2"></i>今日已完成打卡
                                </button>
                                <p class="text-muted mt-3">打卡已锁定，请明天再来。</p>
                            </c:otherwise>
                        </c:choose>
                    </div>

                </div>
            </div>
        </div>
    </div>
</div>
$(document).ready(function() {
    // 页面加载后，立即检查今日日程
    checkReminders();
});

<script src="${pageContext.request.contextPath}/lib/bootstrap/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/lib/jquery/jquery.min.js"></script>

<script>
    $(document).ready(function() {
        // 页面加载延迟 500 毫秒执行，确保其他资源加载完毕
        setTimeout(checkReminders, 500);
    });

    function checkReminders() {
        $.ajax({
            url: "${pageContext.request.contextPath}/schedule/data",
            type: 'GET',
            success: function(res) {
                // 1. 获取准确的本地时间 (避免 toISOString 的时区坑)
                const now = new Date();
                const year = now.getFullYear();
                const month = String(now.getMonth() + 1).padStart(2, '0');
                const day = String(now.getDate()).padStart(2, '0');
                const today = year + "-" + month + "-" + day; // 拼接成 YYYY-MM-DD

                // 打印到 F12 控制台，方便你直接看到到底错在哪
                console.log("【提醒测试】前端认为的今天是: ", today);
                console.log("【提醒测试】后端发来的所有日程: ", res);

                // 2. 更加宽容的匹配逻辑
                let todayTasks = res.filter(item => {
                    if (!item.schedule_date) return false;

                    // 将后端的时间转为字符串，并检查是否包含今天的日期
                    // 这样无论是 "2026-04-14" 还是 "2026-04-14 10:00:00" 都能匹配上
                    let backendDateStr = String(item.schedule_date);
                    return backendDateStr.includes(today);
                });

                console.log("【提醒测试】找到的今日待办数量: ", todayTasks.length);

                // 3. 触发弹窗
                if (todayTasks.length > 0) {
                    alert("📢 叮咚！您今天有 " + todayTasks.length + " 个日程需要处理！");
                }
            },
            error: function() {
                console.error("【提醒测试】无法获取日程数据，请检查网络或登录状态。");
            }
        });
    }
</script>
</body>
<a href="${pageContext.request.contextPath}/user/chatPage" target="mainFrame"
   class="btn btn-primary rounded-circle shadow-lg d-flex align-items-center justify-content-center"
   style="position: fixed; bottom: 30px; right: 30px; width: 60px; height: 60px; z-index: 1000;">
    <i class="bi bi-chat-text fs-3 text-white"></i>
</a>
</html>