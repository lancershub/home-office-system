<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>部门管理</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css">

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

<script src="https://cdn.jsdelivr.net/npm/jquery@3.6.0/dist/jquery.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

<script>
    const ctx = "${pageContext.request.contextPath}";
    const deptModal = new bootstrap.Modal(document.getElementById('deptModal'));

    // 机构数据：由服务端注入
    const instData = [
        <c:forEach items="${institutions}" var="inst" varStatus="s">
            {id: ${inst.id}, inst_name: '${inst.inst_name}'}${s.last ? '' : ','}
        </c:forEach>
    ];

    // 填充机构下拉框
    function buildInstOptions(selectedId) {
        var html = '<option value="">-- 暂不归属任何机构 --</option>';
        instData.forEach(function(inst) {
            var sel = (inst.id === selectedId) ? ' selected' : '';
            html += '<option value="' + inst.id + '"' + sel + '>' + inst.inst_name + '</option>';
        });
        $("#inst_id").html(html);
    }

    // 根据 inst_id 获取机构名
    function getInstName(instId) {
        var found = instData.find(function(inst) { return inst.id === instId; });
        return found ? found.inst_name : '<span class="text-muted">未归属</span>';
    }

    $(document).ready(function() {
        buildInstOptions();
        loadDeptData();
    });

    function loadDeptData() {
        $.ajax({
            url: ctx + '/dept/list',
            type: 'GET',
            dataType: 'json',
            success: function(res) {
                var html = '';
                if(res.length === 0) {
                    html = '<tr><td colspan="6" class="text-muted py-4">暂无部门数据，请点击右上角添加</td></tr>';
                } else {
                    res.forEach(function(dept) {
                        var dateStr = dept.create_time ? new Date(dept.create_time).toLocaleString() : '-';
                        var deptJson = encodeURIComponent(JSON.stringify(dept));

                        html += '<tr>' +
                            '<td><span class="badge bg-secondary"># ' + dept.id + '</span></td>' +
                            '<td class="fw-bold text-dark">' + dept.dept_name + '</td>' +
                            '<td>' + getInstName(dept.inst_id) + '</td>' +
                            '<td class="text-start text-muted">' + (dept.dept_desc || '<span class="text-light">暂无描述</span>') + '</td>' +
                            '<td>' + dateStr + '</td>' +
                            '<td>' +
                                '<button class="btn btn-outline-primary action-btn me-1" onclick="showEditModal(\'' + deptJson + '\')"><i class="bi bi-pencil-square"></i></button>' +
                                '<button class="btn btn-outline-danger action-btn" onclick="deleteDept(' + dept.id + ', \'' + dept.dept_name + '\')"><i class="bi bi-trash"></i></button>' +
                            '</td>' +
                            '</tr>';
                    });
                }
                $("#deptTableBody").html(html);
            },
            error: function() {
                $("#deptTableBody").html('<tr><td colspan="6" class="text-danger py-4">数据加载失败，请检查服务器连接</td></tr>');
            }
        });
    }

    function showAddModal() {
        $("#deptForm")[0].reset();
        $("#dept_id").val('');
        buildInstOptions();
        $("#modalTitle").text("新增部门");
        deptModal.show();
    }

    function showEditModal(deptJsonStr) {
        var dept = JSON.parse(decodeURIComponent(deptJsonStr));
        $("#dept_id").val(dept.id);
        $("#dept_name").val(dept.dept_name);
        $("#dept_desc").val(dept.dept_desc);
        $("#manager_id").val(dept.manager_id);
        buildInstOptions(dept.inst_id);
        $("#modalTitle").text("编辑部门 - " + dept.dept_name);
        deptModal.show();
    }

    function saveDept() {
        var name = $("#dept_name").val().trim();
        if(!name) return alert("部门名称不能为空！");

        $.ajax({
            url: ctx + '/dept/save',
            type: 'POST',
            data: $("#deptForm").serialize(),
            success: function(res) {
                if(res === 'success') {
                    deptModal.hide();
                    loadDeptData();
                } else {
                    alert("保存失败，请重试！");
                }
            }
        });
    }

    function deleteDept(id, name) {
        if(confirm('确定要删除【' + name + '】吗？此操作不可恢复！')) {
            $.ajax({
                url: ctx + '/dept/delete',
                type: 'POST',
                data: { id: id },
                success: function(res) {
                    if(res === 'success') {
                        loadDeptData();
                    } else {
                        alert("删除失败！");
                    }
                }
            });
        }
    }
</script>
</body>
</html>