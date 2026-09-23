<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
<%@ include file="security.jspf" %>
    <meta charset="UTF-8">
    <title>员工部门分配</title>
    <link href="${pageContext.request.contextPath}/lib/bootstrap/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/lib/bootstrap-icons/font/bootstrap-icons.css">
    <style>
        body { background-color: #f8f9fa; padding: 20px; }
        .card { border-radius: 12px; border: none; box-shadow: 0 4px 15px rgba(0,0,0,0.05); }
        .dept-select { border-radius: 20px; font-size: 14px; border-color: #dee2e6; }
        .dept-select:focus { box-shadow: none; border-color: #0d6efd; }
    </style>
</head>
<body>

<div class="container-fluid">
    <div class="card">
        <div class="card-header bg-white py-3">
            <h5 class="mb-0 fw-bold text-success"><i class="bi bi-people-fill me-2"></i>人员组织调配</h5>
            <small class="text-muted">在此页面为注册员工分配所属部门，更改后立即生效。</small>
        </div>
        <div class="card-body p-0">
            <table class="table table-hover align-middle mb-0 text-center">
                <thead class="table-light">
                    <tr>
                        <th>员工编号</th>
                        <th>员工姓名</th>
                        <th>注册时间</th>
                        <th width="30%">所属部门 (直接选择)</th>
                    </tr>
                </thead>
                <tbody id="employeeTableBody">
                    </tbody>
            </table>
        </div>
    </div>
</div>

<script src="${pageContext.request.contextPath}/lib/jquery/jquery.min.js"></script>
<script src="${pageContext.request.contextPath}/js/employee.js"></script>
</body>
</html>