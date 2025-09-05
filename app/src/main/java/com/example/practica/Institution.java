package com.example.practica;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Institution {
    private int id;
    private String login = "";
    private String password = "";
    private String name = "";
    private String addressText = "";
    private double latitude;
    private double longitude;
    private List<String> imageUris = new ArrayList<>();
    private String shortDescription = "";
    private String fullDescription = "";
    private float avgRating;
    private List<Integer> reviewIds = new ArrayList<>(); // Изменено на List<Integer>

    // Конструкторы
    public Institution() {}

    public Institution(String name, String addressText) {
        this.name = name != null ? name : "";
        this.addressText = addressText != null ? addressText : "";
    }

    // Геттеры и сеттеры
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login != null ? login : "";
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password != null ? password : "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name != null ? name : "";
    }

    public String getAddressText() {
        return addressText;
    }

    public void setAddressText(String addressText) {
        this.addressText = addressText != null ? addressText : "";
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public List<String> getImageUris() {
        return Collections.unmodifiableList(imageUris);
    }

    public void setImageUris(List<String> imageUris) {
        this.imageUris = imageUris != null ? new ArrayList<>(imageUris) : new ArrayList<>();
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription != null ? shortDescription : "";
    }

    public String getFullDescription() {
        return fullDescription;
    }

    public void setFullDescription(String fullDescription) {
        this.fullDescription = fullDescription != null ? fullDescription : "";
    }

    public float getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(float avgRating) {
        this.avgRating = Math.max(0.0f, Math.min(5.0f, avgRating));
    }

    // Исправленные методы для работы с отзывами
    public List<Integer> getReviewIds() { // Переименовано и изменен тип возврата
        return Collections.unmodifiableList(reviewIds);
    }

    public void setReviewIds(List<Integer> reviewIds) { // Переименовано и изменен параметр
        this.reviewIds = reviewIds != null ? new ArrayList<>(reviewIds) : new ArrayList<>();
    }
}