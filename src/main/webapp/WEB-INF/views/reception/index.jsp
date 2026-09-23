<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
<%@ include file="security.jspf" %>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>居家办公通系统 - 门户</title>
    <link href="${pageContext.request.contextPath}/lib/bootstrap/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/lib/bootstrap-icons/font/bootstrap-icons.css">
    <style>
        body {
            background-color: #f4f6f9;
            height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
        }
        .login-card {
            width: 100%;
            max-width: 450px;
            border-radius: 15px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.1);
            background: white;
            overflow: hidden;
        }
        .login-header {
            background: linear-gradient(135deg, #0d6efd, #198754); /* 渐变色稍微调整了一下，更有生机 */
            color: white;
            padding: 30px 20px;
            text-align: center;
        }
        .nav-tabs .nav-link { color: #495057; font-weight: 500; }
        .nav-tabs .nav-link.active { font-weight: bold; color: #0d6efd; border-bottom: 3px solid #0d6efd; }
    </style>
</head>
<body>

<div class="login-card">
    <div class="login-header">
        <h3 class="mb-0"><i class="bi bi-laptop me-2"></i>居家办公通系统</h3>
        <p class="mt-2 mb-0 text-white-50">高效、便捷的远程办公管理平台</p>
    </div>

    <ul class="nav nav-tabs nav-justified border-0 mt-3" id="myTab" role="tablist">
        <li class="nav-item" role="presentation">
            <button class="nav-link active border-0 bg-transparent" id="login-tab" data-bs-toggle="tab" data-bs-target="#login-pane" type="button" role="tab">系统登录</button>
        </li>
        <li class="nav-item" role="presentation">
            <button class="nav-link border-0 bg-transparent" id="register-tab" data-bs-toggle="tab" data-bs-target="#register-pane" type="button" role="tab">访客来访登记</button>
        </li>
    </ul>

    <div class="tab-content p-4" id="myTabContent">

        <div class="tab-pane fade show active" id="login-pane" role="tabpanel">

            <c:if test="${param.error eq 'true'}"><div class="alert alert-danger">账号或密码错误</div></c:if>

            <form action="login" method="post">
<input type="hidden" name="${_csrf.parameterName}" value="<c:out value='${_csrf.token}'/>">
                <div class="mb-3">
                    <label class="form-label text-muted small">登录账号 <span class="badge bg-primary ms-1">管理员 / 访客</span></label>
                    <div class="input-group">
                        <span class="input-group-text bg-light"><i class="bi bi-person"></i></span>
                        <input type="text" class="form-control" name="username" placeholder="管理员账号 或 访客分配的ID" required>
                    </div>
                </div>
                <div class="mb-4">
                    <label class="form-label text-muted small">登录密码</label>
                    <div class="input-group">
                        <span class="input-group-text bg-light"><i class="bi bi-lock"></i></span>
                        <input type="password" class="form-control" name="password" minlength="12" placeholder="请输入密码" required>
                    </div>
                </div>
                <button type="submit" class="btn btn-primary w-100 py-2 fw-bold"><i class="bi bi-box-arrow-in-right me-2"></i>登 录 系 统</button>
            </form>
        </div>

        <div class="tab-pane fade" id="register-pane" role="tabpanel">

            <% if("true".equals(request.getParameter("regSuccess"))) { %>
                <div class="alert alert-success py-3 shadow-sm border-0 text-center">
                    <i class="bi bi-check-circle-fill fs-4 text-success d-block mb-2"></i>
                    <h5 class="alert-heading fw-bold">注册成功！</h5>
                    <p class="mb-1">您的专属登录 ID 是：</p>
                    <h2 class="text-danger fw-bold my-2"><c:out value="${param.newId}"/></h2>
                    <hr>
                    <p class="mb-0 small text-muted">请牢记您的 ID，稍后请切换到"系统登录"标签页进行登录。</p>
                </div>
            <% } %>

            <form action="register" method="post">
<input type="hidden" name="${_csrf.parameterName}" value="<c:out value='${_csrf.token}'/>">
                <div class="mb-3">
                    <input type="text" class="form-control" name="visitorName" placeholder="访客姓名 (必填)" required>
                </div>
                <div class="mb-3">
                    <input type="text" class="form-control" name="phone" placeholder="联系电话 (必填)" required>
                </div>
                <div class="mb-3">
                    <input type="password" class="form-control border-primary" name="password" minlength="12" placeholder="请设置登录密码 (必填)" required>
                </div>
                <div class="mb-4">
                    <textarea class="form-control" name="purpose" rows="2" placeholder="来访目的 (选填)"></textarea>
                </div>
                <button type="submit" class="btn btn-success w-100 py-2 fw-bold"><i class="bi bi-person-plus-fill me-2"></i>注 册 账 号</button>
            </form>
        </div>

    </div>
</div>

<script src="${pageContext.request.contextPath}/lib/bootstrap/js/bootstrap.bundle.min.js"></script>
</body>
</html>