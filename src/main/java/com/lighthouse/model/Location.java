package com.lighthouse.model;

public class Location {
    private int locationId;
    private String locationName;
    private int rating;
    private int risk;

    public Location() {}

    public Location(int locationId, String locationName, int rating, int risk) {
        this.locationId = locationId;
        this.locationName = locationName;
        this.rating = rating;
        this.risk = risk;
    }

    public int getLocationId() { return locationId; }
    public void setLocationId(int locationId) { this.locationId = locationId; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public int getRisk() { return risk; }
    public void setRisk(int risk) { this.risk = risk; }

    @Override
    public String toString() {
        return locationName + " (Rating: " + rating + ", Risk: " + risk + ")";
    }
}
