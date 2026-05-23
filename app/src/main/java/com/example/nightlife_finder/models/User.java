package com.example.nightlife_finder.models;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String uid;
    private String email;
    private List<String> favorites; // Lưu danh sách Place ID mà user yêu thích

    // Profile fields – thêm mới, merge an toàn với Firestore
    private String displayName;
    private String phone;
    private String location;
    private String bio;
    private String avatarUrl;

    // Firebase yêu cầu một constructor rỗng (Empty Constructor) để map dữ liệu
    public User() {
    }

    public User(String uid, String email) {
        this.uid = uid;
        this.email = email;
        this.favorites = new ArrayList<>();
    }

    // --- Getters và Setters ---
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getFavorites() {
        return favorites;
    }

    public void setFavorites(List<String> favorites) {
        this.favorites = favorites;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}