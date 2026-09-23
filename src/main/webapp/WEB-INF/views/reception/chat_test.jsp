<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
<%@ include file="security.jspf" %>
<meta name="chat-user" content="${not empty sessionScope.adminUser ? 'admin' : sessionScope.visitorId}">
    <meta charset="UTF-8">
    <title>即时沟通</title>
    <link href="${pageContext.request.contextPath}/lib/bootstrap/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { background-color: #f0f2f5; padding-top: 20px; }
        .chat-box { height: 400px; overflow-y: auto; background-color: #fff; border: 1px solid #ddd; padding: 15px; border-radius: 8px; margin-bottom: 15px; }
        .msg-row { margin-bottom: 15px; display: flex; }
        .msg-left { justify-content: flex-start; }
        .msg-right { justify-content: flex-end; }
        .msg-bubble { max-width: 70%; padding: 10px 15px; border-radius: 15px; font-size: 14px; position: relative; }
        .bubble-left { background-color: #f1f0f0; color: #333; border-top-left-radius: 2px; }
        .bubble-right { background-color: #95ec69; color: #333; border-top-right-radius: 2px; }
        .msg-sender { font-size: 12px; color: #999; margin-bottom: 2px; }
    </style>
</head>
<body>
<div class="container" style="max-width: 600px;">
    <div class="card shadow">
        <div class="card-header bg-dark text-white d-flex justify-content-between align-items-center">
            <span>🟢 在线通信中控台</span>
            <span id="statusLabel" class="badge bg-danger">未连接</span>
        </div>
        <div class="card-body">
            <div class="input-group mb-3">
                <span class="input-group-text">发送给谁(填ID)：</span>
                <input type="text" id="targetUserId" class="form-control" placeholder="例如：admin 或 访客ID" value="admin">
            </div>

            <div class="chat-box" id="chatBox">
                <div class="text-center text-muted small mb-3">—— 系统：已进入实时聊天室 ——</div>
            </div>

            <div class="input-group">
                <input type="text" id="msgInput" class="form-control" placeholder="输入你想说的话..." onkeypress="if(event.keyCode==13) sendMessage()">
                <button class="btn btn-success" onclick="sendMessage()">发送消息</button>
            </div>
        </div>
    </div>
</div>

<script src="${pageContext.request.contextPath}/lib/jquery/jquery.min.js"></script>
<script src="${pageContext.request.contextPath}/js/chat.js"></script>
</body>
</html>