<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>文件柜橱</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css">
    <style>
        body { background-color: #f8f9fa; padding: 20px; }
        .card { border-radius: 12px; border: none; box-shadow: 0 4px 15px rgba(0,0,0,0.05); }
        .table th { background-color: #f1f3f5; color: #495057; font-weight: 600; border-bottom: 2px solid #dee2e6; }
        .table td { vertical-align: middle; }
        .upload-area {
            border: 2px dashed #dee2e6;
            border-radius: 10px;
            padding: 30px;
            text-align: center;
            cursor: pointer;
            transition: border-color 0.3s;
        }
        .upload-area:hover { border-color: #0d6efd; background: #f0f7ff; }
        .size-display { font-size: 0.85rem; color: #6c757d; }
    </style>
</head>
<body>

<div class="container-fluid">
    <div class="row">
        <div class="col-lg-3">
            <div class="card mb-3">
                <div class="card-header bg-white py-3">
                    <h6 class="mb-0 fw-bold"><i class="bi bi-cloud-upload me-2"></i>上传文件</h6>
                </div>
                <div class="card-body">
                    <form id="uploadForm" action="${pageContext.request.contextPath}/file/upload" method="post" enctype="multipart/form-data">
                        <div class="upload-area mb-3" onclick="document.getElementById('fileInput').click()">
                            <i class="bi bi-file-earmark-arrow-up fs-1 text-primary mb-2 d-block"></i>
                            <p class="mb-1 fw-bold">点击选择文件</p>
                            <p class="text-muted small mb-2" id="fileNameDisplay">支持任意格式，最大 100MB</p>
                            <input type="file" id="fileInput" name="file" style="display:none" onchange="onFileSelected(this)" required>
                        </div>
                        <button type="submit" id="uploadBtn" class="btn btn-primary w-100" disabled>
                            <i class="bi bi-upload me-1"></i>开始上传
                        </button>
                    </form>
                </div>
            </div>
            <div class="card">
                <div class="card-body text-center py-4">
                    <button class="btn btn-outline-secondary w-100 mb-2" id="toggleTrashBtn" onclick="toggleTrash()">
                        <i class="bi bi-trash me-1"></i><span id="trashBtnText">查看回收站</span>
                    </button>
                    <small class="text-muted">上传的文件仅供本人查看</small>
                </div>
            </div>
        </div>

        <div class="col-lg-9">
            <div class="card">
                <div class="card-header bg-white d-flex justify-content-between align-items-center py-3">
                    <h5 class="mb-0 fw-bold text-primary" id="tableTitle">
                        <i class="bi bi-folder2-open me-2"></i>我的文件
                    </h5>
                    <button class="btn btn-sm btn-outline-secondary" onclick="loadFiles()">
                        <i class="bi bi-arrow-clockwise"></i> 刷新
                    </button>
                </div>
                <div class="card-body p-0">
                    <table class="table table-hover mb-0 text-center">
                        <thead>
                            <tr>
                                <th width="30%">文件名</th>
                                <th width="10%">大小</th>
                                <th width="15%">类型</th>
                                <th width="20%">上传时间</th>
                                <th width="25%">操作</th>
                            </tr>
                        </thead>
                        <tbody id="fileTableBody">
                            <tr><td colspan="5" class="text-muted py-4">正在加载...</td></tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/jquery@3.6.0/dist/jquery.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

<script>
    var ctx = "${pageContext.request.contextPath}";
    var isTrashMode = false;

    $(document).ready(function() { loadFiles(); });

    function onFileSelected(input) {
        if (input.files.length > 0) {
            var f = input.files[0];
            var size = f.size > 1024 * 1024 ? (f.size / 1024 / 1024).toFixed(1) + ' MB'
                     : f.size > 1024 ? (f.size / 1024).toFixed(1) + ' KB'
                     : f.size + ' B';
            $("#fileNameDisplay").html('<strong>' + f.name + '</strong> <span class="size-display">(' + size + ')</span>');
            $("#uploadBtn").prop("disabled", false);
        }
    }

    function loadFiles() {
        var url = isTrashMode ? ctx + '/file/trash' : ctx + '/file/list';
        $.get(url, function(res) {
            var data = typeof res === 'string' ? JSON.parse(res) : res;
            var html = '';
            if (data.length === 0) {
                html = '<tr><td colspan="5" class="text-muted py-4">' + (isTrashMode ? '回收站为空' : '暂无文件，请上传') + '</td></tr>';
            } else {
                data.forEach(function(f) {
                    var dateStr = f.upload_time ? new Date(f.upload_time).toLocaleString() : '-';
                    var sizeStr = f.file_size > 1024 * 1024 ? (f.file_size / 1024 / 1024).toFixed(1) + ' MB'
                                : (f.file_size / 1024).toFixed(1) + ' KB';
                    var typeStr = f.file_type || '-';
                    if (typeStr.length > 25) typeStr = typeStr.substring(0, 25) + '...';

                    html += '<tr>' +
                        '<td class="text-start fw-bold">' + f.original_name + '</td>' +
                        '<td>' + sizeStr + '</td>' +
                        '<td class="text-muted small">' + typeStr + '</td>' +
                        '<td>' + dateStr + '</td>' +
                        '<td>' + getActions(f) + '</td>' +
                        '</tr>';
                });
            }
            $("#fileTableBody").html(html);
        });
    }

    function getActions(f) {
        if (isTrashMode) {
            return '<button class="btn btn-sm btn-outline-success me-1" onclick="restoreFile(' + f.id + ')"><i class="bi bi-arrow-counterclockwise"></i> 恢复</button>' +
                   '<button class="btn btn-sm btn-outline-danger" onclick="permanentDelete(' + f.id + ', \'' + f.original_name + '\')"><i class="bi bi-x-circle"></i> 彻底删除</button>';
        } else {
            return '<a href="' + ctx + '/file/download/' + f.id + '" class="btn btn-sm btn-outline-primary me-1"><i class="bi bi-download"></i> 下载</a>' +
                   '<button class="btn btn-sm btn-outline-danger" onclick="deleteFile(' + f.id + ', \'' + f.original_name + '\')"><i class="bi bi-trash"></i> 删除</button>';
        }
    }

    function toggleTrash() {
        isTrashMode = !isTrashMode;
        if (isTrashMode) {
            $("#trashBtnText").text("返回文件列表");
            $("#tableTitle").html('<i class="bi bi-trash me-2"></i>回收站');
            $("#toggleTrashBtn").removeClass("btn-outline-secondary").addClass("btn-outline-warning");
        } else {
            $("#trashBtnText").text("查看回收站");
            $("#tableTitle").html('<i class="bi bi-folder2-open me-2"></i>我的文件');
            $("#toggleTrashBtn").removeClass("btn-outline-warning").addClass("btn-outline-secondary");
        }
        loadFiles();
    }

    function deleteFile(id, name) {
        if (!confirm('确定将【' + name + '】移入回收站吗？')) return;
        $.post(ctx + '/file/delete/' + id, function(res) {
            if (res === 'success') loadFiles();
        });
    }

    function restoreFile(id) {
        $.post(ctx + '/file/restore/' + id, function(res) {
            if (res === 'success') loadFiles();
        });
    }

    function permanentDelete(id, name) {
        if (!confirm('彻底删除【' + name + '】后无法恢复，确定吗？')) return;
        $.post(ctx + '/file/permanentDelete/' + id, function(res) {
            if (res === 'success') loadFiles();
            else alert("删除失败");
        });
    }
</script>
</body>
</html>