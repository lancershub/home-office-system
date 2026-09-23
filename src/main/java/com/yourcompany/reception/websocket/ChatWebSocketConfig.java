package com.yourcompany.reception.websocket;
import org.springframework.context.annotation.*;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.http.server.*;
import javax.servlet.http.HttpSession;
import java.util.Map;

@Configuration
@EnableWebSocket
public class ChatWebSocketConfig implements WebSocketConfigurer {
    private final NativeChatServer handler;
    public ChatWebSocketConfig(NativeChatServer handler) { this.handler = handler; }
    @Override public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Default Spring origin policy is same-origin; never allow arbitrary origins.
        registry.addHandler(handler, "/native/chat/*").addInterceptors(new HandshakeInterceptor() {
            public boolean beforeHandshake(ServerHttpRequest req, ServerHttpResponse res, WebSocketHandler ws, Map<String,Object> attrs) {
                if (!(req instanceof ServletServerHttpRequest) || req.getPrincipal() == null || req.getHeaders().getOrigin() == null) return false;
                String path = req.getURI().getPath();
                if (!path.substring(path.lastIndexOf('/') + 1).equals(req.getPrincipal().getName())) return false;
                HttpSession session = ((ServletServerHttpRequest)req).getServletRequest().getSession(false);
                if (session == null) return false;
                attrs.put("httpSession", session);
                attrs.put("httpSessionId", session.getId());
                return true;
            }
            public void afterHandshake(ServerHttpRequest req, ServerHttpResponse res, WebSocketHandler ws, Exception ex) {}
        });
    }
}
