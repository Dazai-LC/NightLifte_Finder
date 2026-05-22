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

    // Thông tin quán (thêm từ lần 2)
    private String shopName;
    private String shopAvatarText;
    private String shopCategory;
    private String address;
    private String openTime;
    private String closeTime;

    // Trạng thái đọc (false = chưa đọc; old docs không có field → Firebase default false)
    private boolean isRead;

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

    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }

    public String getShopAvatarText() { return shopAvatarText; }
    public void setShopAvatarText(String shopAvatarText) { this.shopAvatarText = shopAvatarText; }

    public String getShopCategory() { return shopCategory; }
    public void setShopCategory(String shopCategory) { this.shopCategory = shopCategory; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getOpenTime() { return openTime; }
    public void setOpenTime(String openTime) { this.openTime = openTime; }

    public String getCloseTime() { return closeTime; }
    public void setCloseTime(String closeTime) { this.closeTime = closeTime; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
}
