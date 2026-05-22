package com.example.nightlife_finder.models;

/**
 * ChatMessage – ánh xạ document trong subcollection
 * conversations/{conversationId}/messages/{messageId}.
 * Fields phải khớp chính xác với schema đã thống nhất.
 */
public class ChatMessage {

    private String id;
    private String conversationId;
    private String senderId;
    private String senderEmail;
    private String text;
    private long createdAt;

    // Firebase yêu cầu constructor rỗng
    public ChatMessage() {
    }

    public ChatMessage(String id, String conversationId, String senderId,
                       String senderEmail, String text, long createdAt) {
        this.id = id;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.senderEmail = senderEmail;
        this.text = text;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getSenderEmail() { return senderEmail; }
    public void setSenderEmail(String senderEmail) { this.senderEmail = senderEmail; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
