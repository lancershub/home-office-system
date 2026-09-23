<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
<%@ include file="security.jspf" %>
    <meta charset="UTF-8">
    <title>部门管理</title>
    <link href="${pageContext.request.contextPath}/lib/bootstrap/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/lib/bootstrap-icons/font/bootstrap-icons.css">

    <style>
        body { background-color: #f8f9fa; padding: 20px; }
        .card { border-radius: 12px; border: none; box-shadow: 0 4px 15px rgba(0,0,0,0.05); }
        .table th { background-color: #f1f3f5; color: #495057; font-weight: 600; border-bottom: 2px solid #dee2e6; }
        .table td { vertical-align: middle; }
        .action-btn { padding: 4px 10px; font-size: 14px; }
    </style>
</head>
<body>

<div class="container-fluid">
    <div class="card">
        <div class="card-header bg-white d-flex justify-content-between align-items-center py-3">
            <h5 class="mb-0 fw-bold text-primary"><i class="bi bi-diagram-3-fill me-2"></i>部门组织架构管理</h5>
            <button class="btn btn-primary shadow-sm" onclick="showAddModal()">
                <i class="bi bi-plus-lg"></i> 新增部门
            </button>
        </div>
        <div class="card-body p-0">
            <table class="table table-hover mb-0 text-center">
                <thead>
                    <tr>
                        <th width="8%">编号</th>
                        <th width="15%">部门名称</th>
                        <th width="15%">所属机构</th>
                        <th width="25%">部门职责描述</th>
                        <th width="15%">创建时间</th>
                        <th width="12%">操作</th>
                    </tr>
                </thead>
                <tbody id="deptTableBody">
                    <tr><td colspan="6" class="text-muted py-4">正在加载数据...</td></tr>
                </tbody>
            </table>
        </div>
    </div>
</div>

<div class="modal fade" id="deptModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow">
            <div class="modal-header bg-primary text-white">
                <h5 class="modal-title" id="modalTitle">新增部门</h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body p-4">
                <form id="deptForm">
<input type="hidden" name="${_csrf.parameterName}" value="<c:out value='${_csrf.token}'/>">
                    <input type="hidden" id="dept_id" name="id">

                    <div class="mb-3">
                        <label class="form-label fw-bold">部门名称 <span class="text-danger">*</span></label>
                        <input type="text" class="form-control" id="dept_name" name="dept_name" required placeholder="例如：技术研发部">
                    </div>
                    <div class="mb-3">
                        <label class="form-label fw-bold">所属机构</label>
                        <select class="form-select" id="inst_id" name="inst_id">
                            <option value="">-- 暂不归属任何机构 --</option>
                        </select>
                    </div>
                    <div class="mb-3">
                        <label class="form-label fw-bold">部门描述</label>
                        <textarea class="form-control" id="dept_desc" name="dept_desc" rows="4" placeholder="请简要描述该部门的主要职责..."></textarea>
                    </div>
                    <div class="mb-3">
                        <label class="form-label fw-bold">负责人 ID (选填)</label>
                        <input type="number" class="form-control" id="manager_id" name="manager_id" placeholder="输入负责人编号">
                    </div>
                </form>
            </div>
            <div class="modal-footer border-0">
                <button type="button" class="btn btn-light" data-bs-dismiss="modal">取消</button>
                <button type="button" class="btn btn-primary px-4" onclick="saveDept()">确认保存</button>
            </div>
        </div>
    </div>
</div>

<script src="${pageContext.request.contextPath}/lib/jquery/jquery.min.js"></script>
<script src="${pageContext.request.contextPath}/lib/bootstrap/js/bootstrap.bundle.min.js"></script>

<script src="${pageContext.request.contextPath}/js/dept.js"></script>
</body>
</html>