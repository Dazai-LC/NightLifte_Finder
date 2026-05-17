package com.example.nightlife_finder.models;

import com.google.firebase.firestore.DocumentId;

public class Place {
    // Dùng @DocumentId để lấy ID của document trên Firestore mà không cần tạo field "id" trong database
    @DocumentId
    private String id;

    // Các field chuẩn 100% theo Mục VIII, đặt tên camelCase theo Mục IX
    private String name;
    private String address;
    private String category;
    private String openTime;
    private String imageUrl;
    private Double lat; // Dùng Double theo đúng đặc tả
    private Double lng; // Dùng Double theo đúng đặc tả

    // Firebase yêu cầu constructor rỗng
    public Place() {
    }

    public Place(String name, String address, String category, String openTime, String imageUrl, Double lat, Double lng) {
        this.name = name;
        this.address = address;
        this.category = category;
        this.openTime = openTime;
        this.imageUrl = imageUrl;
        this.lat = lat;
        this.lng = lng;
    }

    // --- Getters và Setters ---
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getOpenTime() {
        return openTime;
    }

    public void setOpenTime(String openTime) {
        this.openTime = openTime;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }
}