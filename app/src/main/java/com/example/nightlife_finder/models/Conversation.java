package com.example.nightlife_finder.models;

/**
 * Conversation – ánh xạ document trong Firestore collection "conversations".
 * Fields phải khớp chính xác với schema đã thống nhất.
 */
public class Conversation {

    private String id;
    private String title;
    private String createdBy;
    private String createdByEmail;
    private String lastMessage;
    private long createdAt;
    private long lastMessageAt;

    // Firebase yêu cầu constructor rỗng
    public Conversation() {
    }

    public Conversation(String id, String title, String createdBy, String createdByEmail,
                        String lastMessage, long createdAt, long lastMessageAt) {
        this.id = id;
        this.title = title;
        this.createdBy = createdBy;
        this.createdByEmail = createdByEmail;
        this.lastMessage = lastMessage;
        this.createdAt = createdAt;
        this.lastMessageAt = lastMessageAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getCreatedByEmail() { return createdByEmail; }
    public void setCreatedByEmail(String createdByEmail) { this.createdByEmail = createdByEmail; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getLastMessageAt() { return lastMessageAt; }
    public void setLastMessageAt(long lastMessageAt) { this.lastMessageAt = lastMessageAt; }
}
