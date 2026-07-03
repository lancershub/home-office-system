package com.yourcompany.reception.entity;

public class ChatMessage {
    private Integer id;
    private String senderId;
    private String receiverId;
    private String content;
    private String sendTime;
    private Integer isRead;

    public ChatMessage() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getSendTime() { return sendTime; }
    public void setSendTime(String sendTime) { this.sendTime = sendTime; }
    public Integer getIsRead() { return isRead; }
    public void setIsRead(Integer isRead) { this.isRead = isRead; }
}
