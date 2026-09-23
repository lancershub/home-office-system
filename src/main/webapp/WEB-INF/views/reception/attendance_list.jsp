<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
<%@ include file="security.jspf" %>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>考勤数据管理</title>
    <link href="${pageContext.request.contextPath}/lib/bootstrap/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/lib/bootstrap-icons/font/bootstrap-icons.css">
</head>
<body>

<div class="container mt-4">
    <div class="d-flex justify-content-between align-items-center mb-3">
        <h4 class="text-secondary"><i class="bi bi-calendar-check me-2"></i>员工考勤记录</h4>
        <a href="${pageContext.request.contextPath}/exportAttendance" class="btn btn-outline-primary btn-sm">
            <i class="bi bi-download me-1"></i>导出报表
        </a>
    </div>

    <div class="table-responsive shadow-sm rounded">
        <table class="table table-striped table-bordered table-hover align-middle mb-0 text-center">
            <thead class="table-dark">
                <tr>
                    <th scope="col">记录ID</th>
                    <th scope="col">员工姓名</th>
                    <th scope="col">考勤日期</th>
                    <th scope="col">上班打卡</th>
                    <th scope="col">下班打卡</th>
                    <th scope="col">系统诊断</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${records}" var="r">
                    <tr>
                        <th scope="row">${r.id}</th>
                        <td class="fw-bold"><c:out value="${r.user_name}"/></td>
                        <td class="text-primary">${r.work_date}</td>

                        <td>
                            <c:choose>
                                <c:when test="${not empty r.clock_in_time}">
                                    <span class="badge bg-success bg-opacity-10 text-success border border-success"><i class="bi bi-clock me-1"></i>${r.clock_in_time}</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge bg-danger">异常未打卡</span>
                                </c:otherwise>
                            </c:choose>
                        </td>

                        <td>
                            <c:choose>
                                <c:when test="${not empty r.clock_out_time}">
                                    <span class="badge bg-info bg-opacity-10 text-info border border-info"><i class="bi bi-clock-history me-1"></i>${r.clock_out_time}</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge bg-warning text-dark"><i class="bi bi-exclamation-triangle me-1"></i>未打卡 / 办公中</span>
                                </c:otherwise>
                            </c:choose>
                        </td>

                        <td>
                            <c:choose>
                                <c:when test="${not empty r.clock_in_time and not empty r.clock_out_time}">
                                    <span class="badge bg-success">上下班均已打卡</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge bg-secondary">状态待定</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:forEach>

                <c:if test="${empty records}">
                    <tr>
                        <td colspan="6" class="text-center text-muted py-4">
                            <i class="bi bi-inbox fs-4 d-block mb-2"></i> 暂无任何考勤打卡数据
                        </td>
                    </tr>
                </c:if>
            </tbody>
        </table>
    </div>
</div>

<script src="${pageContext.request.contextPath}/lib/bootstrap/js/bootstrap.bundle.min.js"></script>
<div class="m-3"><c:if test="${page > 1}"><a href="?page=${page-1}">上一页</a></c:if> <span>第 <c:out value="${page}"/> 页</span> <c:if test="${records.size() == 100}"><a href="?page=${page+1}">下一页</a></c:if></div></body>
</html>