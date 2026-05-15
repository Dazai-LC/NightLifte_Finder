package com.example.nightlife_finder.models;

public class Place {

    private String name;
    private String address;
    private String imageUrl;
    private float rating;

    public Place() {
    }

    public Place(String name, String address,
                 String imageUrl, float rating) {

        this.name = name;
        this.address = address;
        this.imageUrl = imageUrl;
        this.rating = rating;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}