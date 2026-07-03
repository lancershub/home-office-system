<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>访客数据记录</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css">
</head>
<body>

<div class="container mt-4">
    <div class="d-flex justify-content-between align-items-center mb-3">
        <h4 class="text-secondary"><i class="bi bi-card-list me-2"></i>访客数据记录</h4>
        <button class="btn btn-success" data-bs-toggle="modal" data-bs-target="#addModal">
            <i class="bi bi-plus-lg me-1"></i>新增访客
        </button>
    </div>

    <div class="table-responsive shadow-sm rounded">
        <table class="table table-striped table-bordered table-hover align-middle mb-0">
            <thead class="table-dark">
                <tr>
                    <th scope="col"># ID</th>
                    <th scope="col">访客姓名</th>
                    <th scope="col">联系电话</th>
                    <th scope="col">来访目的</th>
                    <th scope="col">登录密码</th>
                    <th scope="col" class="text-center">操作</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${visitors}" var="v">
                    <tr>
                        <th scope="row">${v.id}</th>

                        <td>${v.visitor_name}</td>
                        <td>${v.phone}</td>
                        <td>${v.purpose}</td>
                        <td>
                            <span class="text-muted">******</span>
                        </td>

                        <td class="text-center">
                            <button class="btn btn-primary btn-sm"
                                    onclick="openEditModal('${v.id}', '${v.visitor_name}', '${v.phone}', '${v.purpose}')">
                                <i class="bi bi-pencil-square"></i> 编辑
                            </button>
                            <a href="deleteVisitor?id=${v.id}" class="btn btn-danger btn-sm"
                               onclick="return confirm('确定要删除访客 [${v.visitor_name}] 吗？');">
                                <i class="bi bi-trash"></i> 删除
                            </a>
                        </td>
                    </tr>
                </c:forEach>

                <c:if test="${empty visitors}">
                    <tr>
                        <td colspan="6" class="text-center text-muted py-4">
                            <i class="bi bi-inbox fs-4 d-block mb-2"></i> 暂无访客记录数据
                        </td>
                    </tr>
                </c:if>
            </tbody>
        </table>
    </div>
</div>

<div class="modal fade" id="addModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header bg-success text-white">
                <h5 class="modal-title">新增访客登记</h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
            </div>
            <form action="addVisitor" method="post">
                <div class="modal-body">
                    <div class="mb-3">
                        <label class="form-label">访客姓名 <span class="text-danger">*</span></label>
                        <input type="text" class="form-control" name="visitorName" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">联系电话 <span class="text-danger">*</span></label>
                        <input type="text" class="form-control" name="phone" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">来访目的</label>
                        <textarea class="form-control" name="purpose" rows="2"></textarea>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
                    <button type="submit" class="btn btn-success">保存记录</button>
                </div>
            </form>
        </div>
    </div>
</div>

<div class="modal fade" id="editModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header bg-primary text-white">
                <h5 class="modal-title">修改访客信息</h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
            </div>
            <form action="updateVisitor" method="post">
                <div class="modal-body">
                    <input type="hidden" name="id" id="edit-id">
                    <div class="mb-3">
                        <label class="form-label">访客姓名 <span class="text-danger">*</span></label>
                        <input type="text" class="form-control" name="visitorName" id="edit-name" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">联系电话 <span class="text-danger">*</span></label>
                        <input type="text" class="form-control" name="phone" id="edit-phone" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">来访目的</label>
                        <textarea class="form-control" name="purpose" id="edit-purpose" rows="2"></textarea>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
                    <button type="submit" class="btn btn-primary">确认修改</button>
                </div>
            </form>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
<script>
    // JS 逻辑：点击“编辑”按钮时，将当前行的数据填充到模态框中
    function openEditModal(id, name, phone, purpose) {
        document.getElementById('edit-id').value = id;
        document.getElementById('edit-name').value = name;
        document.getElementById('edit-phone').value = phone;
        document.getElementById('edit-purpose').value = purpose;

        var editModal = new bootstrap.Modal(document.getElementById('editModal'));
        editModal.show();
    }
</script>
</body>
</html>