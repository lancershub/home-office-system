<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
<%@ include file="security.jspf" %>
    <meta charset="UTF-8">
    <title>文件柜橱</title>
    <link href="${pageContext.request.contextPath}/lib/bootstrap/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/lib/bootstrap-icons/font/bootstrap-icons.css">
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
<input type="hidden" name="${_csrf.parameterName}" value="<c:out value='${_csrf.token}'/>">
                        <div class="upload-area mb-3" onclick="document.getElementById('fileInput').click()">
                            <i class="bi bi-file-earmark-arrow-up fs-1 text-primary mb-2 d-block"></i>
                            <p class="mb-1 fw-bold">点击选择文件</p>
                            <p class="text-muted small mb-2" id="fileNameDisplay">支持办公文档、图片和 ZIP，最大 20MB</p>
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

<script src="${pageContext.request.contextPath}/lib/jquery/jquery.min.js"></script>
<script src="${pageContext.request.contextPath}/lib/bootstrap/js/bootstrap.bundle.min.js"></script>

<script src="${pageContext.request.contextPath}/js/files.js"></script>
</body>
</html>