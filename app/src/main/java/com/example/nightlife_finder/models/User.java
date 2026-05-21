package com.example.nightlife_finder.models;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String uid;
    private String email;
    private List<String> favorites; // Lưu danh sách Place ID mà user yêu thích

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
}