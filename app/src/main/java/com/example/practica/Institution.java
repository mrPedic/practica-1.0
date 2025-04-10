package com.example.practica;

public class Institution {
    private int id;
    private String name;
    private String address_text;
    private String address_link;
    private String shortDescription;
    private String fullDescription;
    private String avgRating;
    private String reviewsIds;

    // Геттеры и сеттеры
    public String getAddress_text() {
        return address_text != null ? address_text : "";
    }
    public void setAddress_text(String addressText) { this.address_text = addressText; }
    public String getAddress_link() {
        return address_link != null ? address_link : "";
    }
    public void setAddress_link(String addressLink) { this.address_link = addressLink; }

    public int getId() { return id; }

    public void setId(int id) { this.id = id; }
    public void setName (String name) { this.name = name;}
    public String getName () {return name;}

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getFullDescription() {
        return fullDescription;
    }

    public void setFullDescription(String fullDescription) {
        this.fullDescription = fullDescription;
    }

    public String getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(String avgRating) {
        this.avgRating = avgRating;
    }

    public String getReviewsIds() {
        return reviewsIds;
    }

    public void setReviewsIds(String reviewsIds) {
        this.reviewsIds = reviewsIds;
    }
}