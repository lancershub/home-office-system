<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>居家办公通系统 - 主控制台</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css">

    <style>
        body { height: 100vh; overflow: hidden; background-color: #f8f9fa; }
        .sidebar { height: 100vh; background-color: #212529; color: white; padding-top: 20px; box-shadow: 2px 0 5px rgba(0,0,0,0.1); }
        .sidebar .nav-link { color: #adb5bd; margin-bottom: 5px; border-radius: 5px; cursor: pointer; }
        .sidebar .nav-link:hover, .sidebar .nav-link.active { background-color: #0d6efd; color: white; }
        .main-wrapper { height: 100vh; display: flex; flex-direction: column; }
        .top-navbar { background-color: white; box-shadow: 0 2px 4px rgba(0,0,0,.08); z-index: 10; }
        .content-area { flex-grow: 1; overflow-y: hidden; padding: 15px; } /* 设为 hidden，滚动条交给 iframe 内部 */
    </style>
</head>
<body>

<div class="container-fluid p-0">
    <div class="row g-0">
        <div class="col-md-2 sidebar d-flex flex-column px-3">
            <h4 class="text-center mb-4 text-white fw-bold">
                <i class="bi bi-laptop me-2"></i>居家办公通
            </h4>
            <nav class="nav flex-column">
                <a class="nav-link active" onclick="switchMenu(this, '访客记录', 'list')">
                    <i class="bi bi-person-lines-fill me-2"></i> 访客记录
                </a>
                <a class="nav-link" onclick="switchMenu(this, '考勤打卡', 'attendanceList')">
                    <i class="bi bi-calendar-check me-2"></i> 考勤打卡
                </a>
                <a class="nav-link" onclick="switchMenu(this, '机构管理', 'inst/view')">
                    <i class="bi bi-building me-2"></i> 机构管理
                </a>
                <a class="nav-link" onclick="switchMenu(this, '部门管理', 'dept/view')">
                    <i class="bi bi-diagram-3 me-2"></i> 部门管理
                </a>
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/user/assignPage" target="mainFrame">
                        <i class="bi bi-person-gear me-2"></i>人员调配
                    </a>
                </li>
            </nav>
        </div>

        <div class="col-md-10 main-wrapper">
            <header class="top-navbar d-flex justify-content-between align-items-center p-3">
                <h5 class="m-0 text-dark fw-bold" id="page-title">访客记录</h5>
                <div>
                    <span class="me-3"><i class="bi bi-person-circle text-primary"></i> 管理员</span>
                    <a href="logout.action" class="btn btn-sm btn-outline-danger">退出</a>
                </div>
            </header>

            <div class="content-area">
                <div class="card border-0 shadow-sm h-100">
                    <div class="card-body p-0 h-100">
                        <iframe id="content-iframe" src="list" style="width: 100%; height: 100%; border: none;"></iframe>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
    function switchMenu(element, title, targetUrl) {
        document.querySelectorAll('.sidebar .nav-link').forEach(nav => nav.classList.remove('active'));
        element.classList.add('active');
        document.getElementById('page-title').innerText = title;

        if (targetUrl !== '#') {
            document.getElementById('content-iframe').src = targetUrl;
        } else {
            // 如果模块还没开发，清空 iframe 并提示
            document.getElementById('content-iframe').srcdoc = `
                <div style="font-family: sans-serif; text-align: center; margin-top: 20%; color: #6c757d;">
                    <h2>🚧</h2>
                    <h3>${title} 模块开发中...</h3>
                </div>
            `;
        }
    }
</script>
</body>
<a href="${pageContext.request.contextPath}/user/chatPage" target="mainFrame"
   class="btn btn-primary rounded-circle shadow-lg d-flex align-items-center justify-content-center"
   style="position: fixed; bottom: 30px; right: 30px; width: 60px; height: 60px; z-index: 1000;">
    <i class="bi bi-chat-text fs-3 text-white"></i>
</a>
</html>