package com.yourcompany.reception.service;
import com.yourcompany.reception.entity.ChatMessage;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import com.yourcompany.reception.util.Input;
import java.util.*;

@Service
public class ChatMessageService {
    private final JdbcTemplate jdbc;
    public ChatMessageService(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    public Map<String,Object> save(String sender, String receiver, String content, String clientId) {
        int from = toDbId(sender), to = toDbId(receiver);
        Input.text(content, "消息", 4000, true);
        if (clientId == null || !clientId.matches("[A-Za-z0-9-]{16,64}")) throw new IllegalArgumentException("消息ID无效");
        if ((to == 0 ? jdbc.queryForObject("SELECT COUNT(*) FROM admin_account WHERE username = 'admin'", Integer.class)
                : jdbc.queryForObject("SELECT COUNT(*) FROM visitor_record WHERE id = ?", Integer.class, to)) == 0)
            throw new IllegalArgumentException("接收人不存在");
        try {
            jdbc.update("INSERT INTO message_record (sender_id, receiver_id, content, send_time, is_read, client_message_id) VALUES (?, ?, ?, NOW(), 0, ?)", from, to, content, clientId);
        } catch (DuplicateKeyException ex) {
            // An ACK may have been lost: the same sender/client ID identifies the original message.
        }
        Map<String,Object> row = jdbc.queryForMap("SELECT id, receiver_id, content FROM message_record WHERE sender_id = ? AND client_message_id = ?", from, clientId);
        if (((Number)row.get("receiver_id")).intValue() != to || !content.equals(row.get("content")))
            throw new IllegalArgumentException("重复消息ID对应不同内容");
        Map<String,Object> out = new LinkedHashMap<>();
        out.put("id", row.get("id")); out.put("clientId", clientId); out.put("from", sender); out.put("to",receiver); out.put("content",content);
        return out;
    }
    public List<ChatMessage> getHistory(String userA, String userB, Integer beforeId) {
        int a=toDbId(userA), b=toDbId(userB);
        int before = beforeId == null ? Integer.MAX_VALUE : Input.id(beforeId);
        List<ChatMessage> result = jdbc.query("SELECT id, sender_id, receiver_id, content, send_time, is_read FROM message_record WHERE ((sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)) AND id < ? ORDER BY id DESC LIMIT 100",
            new Object[]{a,b,b,a,before}, (rs,n) -> {
                ChatMessage m=new ChatMessage(); m.setId(rs.getInt("id")); m.setSenderId(fromDbId(rs.getInt("sender_id"))); m.setReceiverId(fromDbId(rs.getInt("receiver_id")));
                m.setContent(rs.getString("content")); m.setSendTime(rs.getString("send_time")); m.setIsRead(rs.getInt("is_read")); return m;
            });
        Collections.reverse(result); return result;
    }
    private int toDbId(String id) {
        if ("admin".equals(id)) return 0;
        if (id == null || !id.matches("[1-9][0-9]{0,8}")) throw new IllegalArgumentException("用户ID无效");
        return Integer.parseInt(id);
    }
    private String fromDbId(int id) { return id == 0 ? "admin" : String.valueOf(id); }
}
