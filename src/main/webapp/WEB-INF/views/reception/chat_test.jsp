<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>实时通信测试中心</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
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

<script src="https://cdn.jsdelivr.net/npm/jquery@3.6.0/dist/jquery.min.js"></script>
<script>
    const ctx = "${pageContext.request.contextPath}";
    let ws = null;

    // 页面一加载，马上连线服务器并加载历史消息
    $(document).ready(function() {
        connectWebSocket();
        loadChatHistory();
    });

   function connectWebSocket() {
       // 1. 分别安全地获取两个身份标识（交由 JS 来判断，而不是在 EL 里直接短路）
       const adminId = "${not empty sessionScope.adminUser ? 'admin' : ''}";
       const visitorId = "${not empty sessionScope.visitorId ? sessionScope.visitorId : ''}";

       // 2. 打印诊断日志，在 F12 控制台里抓鬼
       console.log("【诊断】当前 Session 状态 -> adminId:", adminId, " | visitorId:", visitorId);

       // 3. 决定 myUserId（这里假设普通用户登录后，应该优先使用普通用户身份）
       let myUserId = "";
       if (visitorId !== "") {
           myUserId = visitorId;
       } else if (adminId !== "") {
           myUserId = adminId;
       }

       if (!myUserId) {
           alert("严重错误：未能获取到当前登录身份，请尝试清理浏览器缓存并重新登录！");
           return;
       }

       let protocol = window.location.protocol === 'https:' ? 'wss://' : 'ws://';
       let wsUrl = protocol + window.location.host + ctx + "/native/chat/" + myUserId;

       // ... 下面的 WebSocket 实例创建代码保持不变 ...
       ws = new WebSocket(wsUrl);
       // ...
       // ...前面的 new WebSocket(wsUrl) 保持不变 ...

       ws.onopen = function() {
           console.log("【前端状态】WebSocket 底层连接成功！准备更新 UI...");

           // 放弃使用 jQuery，改用原生 JS 强制修改 DOM，排除 jQuery 冲突的可能
           let label = document.getElementById("statusLabel");
           if (label) {
               label.className = "badge bg-success"; // 变绿
               label.innerText = "已连接 (" + myUserId + ")";
               console.log("【前端状态】UI 更新完毕！");
           } else {
               console.error("【前端状态】找不到 id 为 statusLabel 的标签！");
           }
       };

       ws.onmessage = function(event) {
           console.log("【前端状态】收到服务器消息：", event.data);
           let msgObj = JSON.parse(event.data);
           renderMessage(msgObj.from, msgObj.content, 'left');
           $("#targetUserId").val(msgObj.from);
       };

       ws.onclose = function(event) {
           console.warn("【前端状态】连接已断开，状态码：", event.code, " 原因：", event.reason);
           let label = document.getElementById("statusLabel");
           if (label) {
               label.className = "badge bg-danger";
               label.innerText = "连接断开";
           }
       };

       ws.onerror = function(err) {
           console.error("【前端状态】WebSocket 发生致命错误：", err);
       };
   }

    // 2. 发送消息
    function sendMessage() {
        let targetId = $("#targetUserId").val();
        let content = $("#msgInput").val();

        if (!targetId || !content) {
            alert("接收人ID和消息内容不能为空！"); return;
        }

        let msgJson = JSON.stringify({
            "to": targetId,
            "content": content
        });

        ws.send(msgJson);
        renderMessage("我", content, 'right');
        $("#msgInput").val('');
    }

    // 3. 渲染聊天气泡
    function renderMessage(sender, text, position) {
        let alignClass = position === 'left' ? 'msg-left' : 'msg-right';
        let bubbleClass = position === 'left' ? 'bubble-left' : 'bubble-right';
        let nameHtml = position === 'left' ? `<div class="msg-sender">\${sender}</div>` : '';

        let html = `
            <div class="msg-row \${alignClass}">
                <div style="max-width: 80%;">
                    \${nameHtml}
                    <div class="msg-bubble \${bubbleClass}">\${text}</div>
                </div>
            </div>
        `;
        $("#chatBox").append(html);
        $('#chatBox').scrollTop($('#chatBox')[0].scrollHeight);
    }

    // 4. 从服务器加载历史聊天记录
    function loadChatHistory() {
        let targetId = $("#targetUserId").val();
        if (!targetId) return;

        $.ajax({
            url: ctx + "/user/chatHistory",
            data: { withUserId: targetId },
            type: "GET",
            dataType: "json",
            success: function(list) {
                $("#chatBox").html('<div class="text-center text-muted small mb-3">—— 系统：已加载历史消息 ——</div>');
                if (list && list.length > 0) {
                    list.forEach(function(msg) {
                        // 判断消息方向：当前用户发送的在右边，接收的在左边
                        let myId = getMyUserId();
                        if (msg.senderId === myId) {
                            renderMessage("我", msg.content, 'right');
                        } else {
                            renderMessage(msg.senderId, msg.content, 'left');
                        }
                    });
                }
            },
            error: function() {
                console.log("加载历史消息失败");
            }
        });
    }

    function getMyUserId() {
        const adminId = "${not empty sessionScope.adminUser ? 'admin' : ''}";
        const visitorId = "${not empty sessionScope.visitorId ? sessionScope.visitorId : ''}";
        if (visitorId !== "") return visitorId;
        if (adminId !== "") return adminId;
        return "";
    }

    // 当切换聊天对象时自动加载历史记录
    $("#targetUserId").on('change', function() {
        loadChatHistory();
    });
</script>
</body>
</html>