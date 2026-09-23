<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
<%@ include file="security.jspf" %>
    <meta charset="UTF-8">
    <title>机构管理</title>
    <link href="${pageContext.request.contextPath}/lib/bootstrap/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/lib/bootstrap-icons/font/bootstrap-icons.css">
    <style>
        body { background-color: #f8f9fa; padding: 20px; }
        .card { border-radius: 12px; border: none; box-shadow: 0 4px 15px rgba(0,0,0,0.05); }
    </style>
</head>
<body>

<div class="container-fluid">
    <div class="card">
        <div class="card-header bg-white d-flex justify-content-between align-items-center py-3">
            <h5 class="mb-0 fw-bold text-dark"><i class="bi bi-building me-2"></i>机构信息管理</h5>
            <button class="btn btn-dark shadow-sm" onclick="showAddModal()">
                <i class="bi bi-plus-lg"></i> 新增机构
            </button>
        </div>
        <div class="card-body p-0">
            <table class="table table-hover mb-0 text-center">
                <thead>
                    <tr>
                        <th width="10%">ID</th>
                        <th width="25%">机构名称</th>
                        <th width="35%">机构描述</th>
                        <th width="15%">创建时间</th>
                        <th width="15%">操作</th>
                    </tr>
                </thead>
                <tbody id="instTableBody">
                    </tbody>
            </table>
        </div>
    </div>
</div>

<div class="modal fade" id="instModal" tabindex="-1">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="modalTitle">新增机构</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <form id="instForm">
<input type="hidden" name="${_csrf.parameterName}" value="<c:out value='${_csrf.token}'/>">
                    <input type="hidden" id="inst_id" name="id">
                    <div class="mb-3">
                        <label class="form-label">机构名称</label>
                        <input type="text" class="form-control" id="inst_name" name="inst_name" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">机构描述</label>
                        <textarea class="form-control" id="inst_desc" name="inst_desc" rows="3"></textarea>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-primary" onclick="saveInst()">保存</button>
            </div>
        </div>
    </div>
</div>

<script src="${pageContext.request.contextPath}/lib/jquery/jquery.min.js"></script>
<script src="${pageContext.request.contextPath}/lib/bootstrap/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/js/institution.js"></script>
</body>
</html>