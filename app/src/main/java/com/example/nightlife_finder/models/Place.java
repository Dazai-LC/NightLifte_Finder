package com.example.nightlife_finder.models;

public class Place {
    public String id;
    public String name;
    public String category;
    public String type;
    public String address;
    public String openingHours;
    public String dishTags;
    public double latitude;
    public double longitude;
    public int nightScore;
    public float rating;

    public Place(
            String id,
            String name,
            String category,
            String type,
            String address,
            String openingHours,
            String dishTags,
            double latitude,
            double longitude,
            int nightScore,
            float rating
    ) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.type = type;
        this.address = address;
        this.openingHours = openingHours;
        this.dishTags = dishTags;
        this.latitude = latitude;
        this.longitude = longitude;
        this.nightScore = nightScore;
        this.rating = rating;
    }
}