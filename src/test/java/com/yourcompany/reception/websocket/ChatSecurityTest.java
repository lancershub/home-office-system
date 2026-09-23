package com.yourcompany.reception.websocket;
import com.yourcompany.reception.service.ChatMessageService;
import org.junit.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.socket.*;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class ChatSecurityTest {
    private WebSocketSession session(String id,String principal,MockHttpSession http){
        WebSocketSession ws=mock(WebSocketSession.class);
        when(ws.getId()).thenReturn(id);when(ws.getPrincipal()).thenReturn(()->principal);when(ws.isOpen()).thenReturn(true);
        Map<String,Object> attrs=new HashMap<>();attrs.put("httpSession",http);attrs.put("httpSessionId",http.getId());
        when(ws.getAttributes()).thenReturn(attrs);return ws;
    }
    private MockHttpSession http(String user){
        MockHttpSession http=new MockHttpSession();
        http.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,new SecurityContextImpl(new UsernamePasswordAuthenticationToken(user,"",Collections.emptyList())));
        return http;
    }
    @Test public void forgedIdentityCannotConnect()throws Exception{
        NativeChatServer server=new NativeChatServer(mock(ChatMessageService.class));
        WebSocketSession ws=session("one","admin",http("1"));
        server.afterConnectionEstablished(ws);
        verify(ws).close(CloseStatus.POLICY_VIOLATION);
    }
    @Test public void invalidatedHttpSessionCannotSend()throws Exception{
        ChatMessageService messages=mock(ChatMessageService.class);NativeChatServer server=new NativeChatServer(messages);
        MockHttpSession http=http("1");WebSocketSession ws=session("one","1",http);
        server.afterConnectionEstablished(ws);http.invalidate();
        server.handleTextMessage(ws,new TextMessage("{}"));
        verifyNoInteractions(messages);verify(ws).close(CloseStatus.POLICY_VIOLATION);
    }
    @Test public void failedPersistenceNeverPushesMessageOrAck()throws Exception{
        ChatMessageService messages=mock(ChatMessageService.class);NativeChatServer server=new NativeChatServer(messages);
        WebSocketSession from=session("one","1",http("1")),to=session("two","2",http("2"));
        server.afterConnectionEstablished(from);server.afterConnectionEstablished(to);
        when(messages.save(anyString(),anyString(),anyString(),anyString())).thenThrow(new org.springframework.dao.DataAccessResourceFailureException("offline"));
        server.handleTextMessage(from,new TextMessage("{\"to\":\"2\",\"content\":\"hello\",\"clientId\":\"1234567890123456\"}"));
        verify(to,never()).sendMessage(any());
        org.mockito.ArgumentCaptor<WebSocketMessage> capture=org.mockito.ArgumentCaptor.forClass(WebSocketMessage.class);
        verify(from).sendMessage(capture.capture());
        assertTrue(capture.getValue().getPayload().toString().contains("\"type\":\"error\""));
    }
    @Test public void quotesAndNewlinesAreSerializedAndMultipleTabsRemainConnected()throws Exception{
        ChatMessageService messages=mock(ChatMessageService.class);NativeChatServer server=new NativeChatServer(messages);
        WebSocketSession from=session("one","1",http("1")),old=session("old","2",http("2")),to=session("new","2",http("2"));
        server.afterConnectionEstablished(from);server.afterConnectionEstablished(old);server.afterConnectionEstablished(to);
        server.afterConnectionClosed(old,CloseStatus.NORMAL);
        Map<String,Object> saved=new LinkedHashMap<>();saved.put("id",1);saved.put("from","1");saved.put("to","2");saved.put("content","\"line\nnext");saved.put("clientId","1234567890123456");
        when(messages.save(anyString(),anyString(),anyString(),anyString())).thenReturn(saved);
        server.handleTextMessage(from,new TextMessage("{\"to\":\"2\",\"content\":\"hello\",\"clientId\":\"1234567890123456\"}"));
        org.mockito.ArgumentCaptor<WebSocketMessage> capture=org.mockito.ArgumentCaptor.forClass(WebSocketMessage.class);
        verify(to).sendMessage(capture.capture());
        assertEquals("\"line\nnext",new com.fasterxml.jackson.databind.ObjectMapper().readTree(capture.getValue().getPayload().toString()).get("content").asText());
        verify(from).sendMessage(any());
    }
}
