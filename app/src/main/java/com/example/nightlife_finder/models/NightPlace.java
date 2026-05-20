package com.example.nightlife_finder.models;

public class NightPlace {

    private String osmId;
    private String name;
    private String type;
    private String category;
    private String openingHours;
    private double latitude;
    private double longitude;
    private double distanceMeters;
    private int nightScore;
    private long updatedAt;

    public NightPlace() {
    }

    public NightPlace(
            String osmId,
            String name,
            String type,
            String category,
            String openingHours,
            double latitude,
            double longitude,
            double distanceMeters,
            int nightScore,
            long updatedAt
    ) {
        this.osmId = osmId;
        this.name = name;
        this.type = type;
        this.category = category;
        this.openingHours = openingHours;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distanceMeters = distanceMeters;
        this.nightScore = nightScore;
        this.updatedAt = updatedAt;
    }

    public String getOsmId() {
        return osmId;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public String getOpeningHours() {
        return openingHours;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getDistanceMeters() {
        return distanceMeters;
    }

    public int getNightScore() {
        return nightScore;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }
}