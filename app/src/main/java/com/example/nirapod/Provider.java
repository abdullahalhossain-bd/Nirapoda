package com.example.nirapod;

import java.util.ArrayList;
import java.util.List;

public class Provider {
    private String id;
    private String name;
    private String type;
    private String distance;
    private int rating;
    private String address;
    private String imageUrl;
    private String phone;
    private String email;
    private String website;
    private String hours;
    private String about;
    private List<Review> reviews = new ArrayList<>();

    public Provider(String id, String name, String type, String distance, int rating,
                    String address, String imageUrl) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.distance = distance;
        this.rating = rating;
        this.address = address;
        this.imageUrl = imageUrl;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getDistance() {
        return distance;
    }

    public int getRating() {
        return rating;
    }

    public String getAddress() {
        return address;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getWebsite() {
        return website;
    }

    public String getHours() {
        return hours;
    }

    public String getAbout() {
        return about;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    // Setters
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public void setHours(String hours) {
        this.hours = hours;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public void addReview(Review review) {
        this.reviews.add(review);
    }
}