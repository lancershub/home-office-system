package com.yourcompany.reception.websocket;
import com.fasterxml.jackson.databind.*;
import com.yourcompany.reception.service.ChatMessageService;
import org.springframework.stereotype.Component;
import org.springframework.context.event.EventListener;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.session.HttpSessionDestroyedEvent;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.*;
import org.springframework.scheduling.annotation.Scheduled;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.io.IOException;
import java.util.logging.*;

public class NativeChatServer extends TextWebSocketHandler {
    private static final Logger LOG = Logger.getLogger(NativeChatServer.class.getName());
    private final ChatMessageService messages;
    private final ObjectMapper json = new ObjectMapper();
    // Key by connection ID so old closes cannot remove a new connection, and multiple tabs work.
    private final Map<String, WebSocketSession> connections = new ConcurrentHashMap<>();
    public NativeChatServer(ChatMessageService messages) { this.messages = messages; }
    @Override public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        if (!valid(session)) { session.close(CloseStatus.POLICY_VIOLATION); return; }
        session.setTextMessageSizeLimit(16384);
        connections.put(session.getId(), new ConcurrentWebSocketSessionDecorator(session, 5000, 65536));
    }
    private boolean valid(WebSocketSession ws) {
        if (ws.getPrincipal() == null) return false;
        try {
            HttpSession http = (HttpSession) ws.getAttributes().get("httpSession");
            if (http == null) return false;
            SecurityContext context = (SecurityContext) http.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
            return context != null && context.getAuthentication() != null && context.getAuthentication().isAuthenticated()
                && ws.getPrincipal().getName().equals(context.getAuthentication().getName());
        } catch (IllegalStateException ex) { return false; }
    }
    @Override public void handleTextMessage(WebSocketSession session, TextMessage payload) throws Exception {
        if (!valid(session)) { session.close(CloseStatus.POLICY_VIOLATION); return; }
        String clientId = null;
        try {
            if (payload.getPayloadLength() > 16384) throw new IllegalArgumentException("消息过大");
            synchronized (session.getAttributes()) {
                long now = System.currentTimeMillis();
                long[] rate = (long[])session.getAttributes().get("rate");
                if (rate == null || now - rate[0] >= 60000) { rate = new long[]{now,0}; session.getAttributes().put("rate",rate); }
                if (++rate[1] > 60) throw new IllegalArgumentException("发送过于频繁，请稍后重试");
            }
            JsonNode node = json.readTree(payload.getPayload());
            if (node == null || !node.isObject()) throw new IllegalArgumentException("消息格式不正确");
            clientId = node.path("clientId").asText();
            String to = node.path("to").asText(), content = node.path("content").asText();
            Map<String,Object> saved = messages.save(session.getPrincipal().getName(), to, content, clientId);
            Map<String,Object> event = new LinkedHashMap<>(saved);
            event.put("type", "message");
            // All recipient connections are checked against their current HTTP login session.
            for (WebSocketSession target : connections.values()) {
                if (target.getPrincipal().getName().equals(to) && valid(target)) send(target, event);
            }
            Map<String,Object> ack = new LinkedHashMap<>(saved); ack.put("type", "ack");
            send(connections.get(session.getId()), ack); // save returned only after the DB commit
        } catch (IllegalArgumentException | com.fasterxml.jackson.core.JsonProcessingException ex) {
            error(session, clientId, "INVALID_MESSAGE", "消息无效或发送过于频繁");
        } catch (Exception ex) {
            LOG.log(Level.WARNING, "Message persistence failed", ex);
            error(session, clientId, "SAVE_FAILED", "消息未确认保存，请重试");
        }
    }
    private void error(WebSocketSession session, String clientId, String code, String message) {
        Map<String,Object> e = new LinkedHashMap<>(); e.put("type","error"); e.put("clientId",clientId); e.put("code",code); e.put("message",message);
        send(connections.get(session.getId()), e);
    }
    private void send(WebSocketSession session, Object value) {
        if (session == null || !session.isOpen()) return;
        try { session.sendMessage(new TextMessage(json.writeValueAsString(value))); }
        catch (Exception ex) { close(session); }
    }
    private void close(WebSocketSession session) {
        connections.remove(session.getId(), session);
        try { session.close(CloseStatus.POLICY_VIOLATION); } catch (IOException ignored) {}
    }
    @Override public void afterConnectionClosed(WebSocketSession session, CloseStatus status) { connections.remove(session.getId()); }
    @Override public void handleTransportError(WebSocketSession session, Throwable error) { WebSocketSession wrapped=connections.get(session.getId()); if(wrapped!=null) close(wrapped); }
    @EventListener public void sessionDestroyed(HttpSessionDestroyedEvent event) {
        for (WebSocketSession ws : connections.values()) if (event.getId().equals(ws.getAttributes().get("httpSessionId"))) close(ws);
    }
    @Scheduled(fixedDelay=30000) public void expireConnections() {
        for (WebSocketSession ws : connections.values()) if (!valid(ws)) close(ws);
    }
}
