package com.example.practica;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Review {
    private int id;
    private int institutionId;
    private float rating;
    private String textReview;
    private String addressText;
    private String addressLink;
    private String username;
    private String imageUrl;
    private String institutionName;

    // Основной конструктор
    public Review(int institutionId,
                  float rating,
                  String textReview,
                  String addressText,
                  String addressLink,
                  String username) {
        this.institutionId = institutionId;
        this.rating = rating >= 0 && rating <= 5 ? rating : 0;
        this.textReview = textReview != null ? textReview : "";
        this.addressText = addressText != null ? addressText : "";
        this.addressLink = addressLink != null ? addressLink : "";
        this.username = username != null ? username : "Гость";
    }

    public Review(int institutionId, float rating, String textReview,
                  String institutionName, String username) {
        this.institutionId = institutionId;
        this.rating = rating;
        this.textReview = textReview != null ? textReview : "";
        this.institutionName = institutionName != null ? institutionName : "";
        this.username = username != null ? username : "Гость";
    }

    // Конструктор для загрузки из БД
    public Review(int id,
                  int institutionId,
                  float rating,
                  String textReview,
                  String addressText,
                  String addressLink,
                  String username) {
        this(institutionId, rating, textReview, addressText, addressLink, username);
        this.id = id;
    }


    // Геттеры и сеттеры
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getInstitutionId() {
        return institutionId;
    }

    public void setInstitutionId(int institutionId) {
        this.institutionId = institutionId;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getTextReview() {
        return textReview;
    }

    public void setTextReview(String textReview) {
        this.textReview = textReview;
    }

    public String getAddressText() {
        return addressText;
    }

    public void setAddressText(String addressText) {
        this.addressText = addressText;
    }

    public String getAddressLink() {
        return addressLink;
    }

    public void setAddressLink(String addressLink) {
        this.addressLink = addressLink;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }
}