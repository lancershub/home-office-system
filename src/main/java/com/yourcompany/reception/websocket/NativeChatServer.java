package com.yourcompany.reception.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yourcompany.reception.service.ChatMessageService;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

// 【核心魔法】：直接使用 {userId} 作为路径变量，Tomcat 会自动提取它！
@ServerEndpoint("/native/chat/{userId}")
public class NativeChatServer {

    // 依然使用线程安全的 Map 存储在线人员的 Session
    private static final ConcurrentHashMap<String, Session> onlineUsers = new ConcurrentHashMap<>();

    // 记录当前这根电话线属于谁
    private String myUserId;

    /**
     * 1. 连接建立时触发
     * @PathParam("userId") 直接就把前端 URL 里的 ID 抓出来了！极其优雅！
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        this.myUserId = userId;
        onlineUsers.put(userId, session);
        System.out.println("【原生 WebSocket】门卫放行，用户上线：" + userId + "，当前总人数：" + onlineUsers.size());
    }

    /**
     * 2. 收到消息时触发
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("【原生 WebSocket】收到 " + myUserId + " 的消息：" + message);

        // 解析前端发来的 JSON
        JSONObject jsonObject = JSON.parseObject(message);
        String toUserId = jsonObject.getString("to");
        String content = jsonObject.getString("content");

        // 尝试寻找接收人
        Session targetSession = onlineUsers.get(toUserId);

        // 持久化消息到数据库
        try {
            ChatMessageService chatService = ChatMessageService.getInstance();
            if (chatService != null) {
                int rows = chatService.save(myUserId, toUserId, content);
                System.out.println("【原生 WebSocket】消息已存库，影响行数：" + rows);
            } else {
                System.out.println("【原生 WebSocket】严重错误：ChatMessageService 静态引用为 null，Spring 可能未初始化！");
            }
        } catch (Exception e) {
            System.out.println("【原生 WebSocket】消息存库失败：");
            e.printStackTrace();
        }

        if (targetSession != null && targetSession.isOpen()) {
            // 对方在线，发送！
            String sendJson = String.format("{\"from\":\"%s\", \"content\":\"%s\"}", myUserId, content);
            try {
                // 原生的发送消息语法
                targetSession.getBasicRemote().sendText(sendJson);
            } catch (IOException e) {
                System.out.println("【原生 WebSocket】发送异常：" + e.getMessage());
            }
        } else {
            System.out.println("【原生 WebSocket】发送失败，目标用户 " + toUserId + " 处于离线状态。");
        }
    }

    /**
     * 3. 连接断开时触发
     */
    /**
     * 3. 连接断开时触发
     * 【修复点】：给方法参数加上 Session session，用来对比身份
     */
    @OnClose
    public void onClose(Session session) {
        if (myUserId != null) {
            // 【核心安全锁】：如果名单里 admin 对应的连接，确实是当前要断开的这个旧连接，才允许删除！
            // 防止 F5 刷新时，旧连接把新连接的记录给误删了！
            if (onlineUsers.get(myUserId) == session) {
                onlineUsers.remove(myUserId);
                System.out.println("【原生 WebSocket】用户彻底下线：" + myUserId + "，当前总人数：" + onlineUsers.size());
            } else {
                System.out.println("【原生 WebSocket】拦截了一次误删动作！用户 " + myUserId + " 已在别处重新连接。");
            }
        }
    }

    /**
     * 4. 发生错误时触发
     */
    @OnError
    public void onError(Session session, Throwable error) {
        System.out.println("【原生 WebSocket】致命错误：" + error.getMessage());
    }
}