<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>机构管理</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css">
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

<script src="https://cdn.jsdelivr.net/npm/jquery@3.6.0/dist/jquery.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script>
    const ctx = "${pageContext.request.contextPath}";
    const instModal = new bootstrap.Modal(document.getElementById('instModal'));

    $(document).ready(loadInstData);

    function loadInstData() {
        $.get(ctx + '/inst/list', function(res) {
            let data = typeof res === 'string' ? JSON.parse(res) : res;
            let html = '';
            data.forEach(item => {
                let dateStr = item.create_time ? new Date(item.create_time).toLocaleDateString() : '-';
                let json = encodeURIComponent(JSON.stringify(item));
                html += `
                    <tr>
                        <td>\${item.id}</td>
                        <td class="fw-bold">\${item.inst_name}</td>
                        <td>\${item.inst_desc || '-'}</td>
                        <td>\${dateStr}</td>
                        <td>
                            <button class="btn btn-sm btn-outline-primary" onclick="showEditModal('\${json}')">编辑</button>
                            <button class="btn btn-sm btn-outline-danger" onclick="deleteInst(\${item.id})">删除</button>
                        </td>
                    </tr>`;
            });
            $("#instTableBody").html(html);
        });
    }

    function showAddModal() {
        $("#instForm")[0].reset();
        $("#inst_id").val('');
        $("#modalTitle").text("新增机构");
        instModal.show();
    }

    function showEditModal(json) {
        let item = JSON.parse(decodeURIComponent(json));
        $("#inst_id").val(item.id);
        $("#inst_name").val(item.inst_name);
        $("#inst_desc").val(item.inst_desc);
        $("#modalTitle").text("编辑机构");
        instModal.show();
    }

    function saveInst() {
        $.post(ctx + '/inst/save', $("#instForm").serialize(), function(res) {
            if(res === 'success') {
                instModal.hide();
                loadInstData();
            }
        });
    }

    function deleteInst(id) {
        if(confirm("确定删除吗？")) {
            $.post(ctx + '/inst/delete', {id: id}, function(res) {
                if(res === 'success') loadInstData();
            });
        }
    }
</script>
</body>
</html>