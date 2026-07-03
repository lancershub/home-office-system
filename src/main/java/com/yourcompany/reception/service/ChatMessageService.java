package com.yourcompany.reception.service;

import com.yourcompany.reception.entity.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Service
public class ChatMessageService {

    private static ChatMessageService self;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init() {
        self = this;
        System.out.println("【ChatMessageService】初始化完成，静态引用已设置");
    }

    public static ChatMessageService getInstance() {
        return self;
    }

    public int save(String senderId, String receiverId, String content) {
        String sql = "INSERT INTO message_record (sender_id, receiver_id, content, send_time, is_read) VALUES (?, ?, ?, NOW(), 0)";
        return jdbcTemplate.update(sql, toDbId(senderId), toDbId(receiverId), content);
    }

    public List<ChatMessage> getHistory(String userA, String userB) {
        String sql = "SELECT * FROM message_record WHERE (sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?) ORDER BY send_time ASC";
        int idA = toDbId(userA);
        int idB = toDbId(userB);
        return jdbcTemplate.query(sql, new Object[]{idA, idB, idB, idA}, this::mapRow);
    }

    public int markRead(String senderId, String receiverId) {
        String sql = "UPDATE message_record SET is_read = 1 WHERE sender_id = ? AND receiver_id = ? AND is_read = 0";
        return jdbcTemplate.update(sql, toDbId(senderId), toDbId(receiverId));
    }

    private int toDbId(String userId) {
        if ("admin".equals(userId)) return 0;
        return Integer.parseInt(userId);
    }

    private String fromDbId(int dbId) {
        if (dbId == 0) return "admin";
        return String.valueOf(dbId);
    }

    private ChatMessage mapRow(ResultSet rs, int rowNum) throws SQLException {
        ChatMessage msg = new ChatMessage();
        msg.setId(rs.getInt("id"));
        msg.setSenderId(fromDbId(rs.getInt("sender_id")));
        msg.setReceiverId(fromDbId(rs.getInt("receiver_id")));
        msg.setContent(rs.getString("content"));
        msg.setSendTime(rs.getString("send_time"));
        msg.setIsRead(rs.getInt("is_read"));
        return msg;
    }
}
