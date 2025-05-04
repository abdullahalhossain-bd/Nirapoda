package com.example.nirapod.models;


import java.util.ArrayList;
import java.util.List;

public class HealthcareProvider {
    private int id;
    private String name;
    private String specialty;
    private String address;
    private String phone;
    private String email;
    private String website;
    private String hours;
    private String about;
    private String imageUrl;
    private double distance;
    private float rating;
    private List<String> specialties;
    private List<Review> reviews;
    private int reviewCount;

    public HealthcareProvider() {
        specialties = new ArrayList<>();
        reviews = new ArrayList<>();
    }

    // Constructor with minimum required fields
    public HealthcareProvider(int id, String name, String specialty, String address, String phone, double distance, float rating) {
        this.id = id;
        this.name = name;
        this.specialty = specialty;
        this.address = address;
        this.phone = phone;
        this.distance = distance;
        this.rating = rating;
        this.specialties = new ArrayList<>();
        this.reviews = new ArrayList<>();
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getHours() {
        return hours;
    }

    public void setHours(String hours) {
        this.hours = hours;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public List<String> getSpecialties() {
        return specialties;
    }

    public void setSpecialties(List<String> specialties) {
        this.specialties = specialties;
    }

    public void addSpecialty(String specialty) {
        if (this.specialties == null) {
            this.specialties = new ArrayList<>();
        }
        this.specialties.add(specialty);
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
        this.reviewCount = reviews.size();
    }

    public void addReview(Review review) {
        if (this.reviews == null) {
            this.reviews = new ArrayList<>();
        }
        this.reviews.add(review);
        this.reviewCount = reviews.size();

        // Recalculate average rating
        float totalRating = 0;
        for (Review r : reviews) {
            totalRating += r.getRating();
        }
        this.rating = reviews.isEmpty() ? 0 : totalRating / reviews.size();
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    // Get first initial for profile display
    public String getInitial() {
        return name != null && !name.isEmpty() ? String.valueOf(name.charAt(0)) : "?";
    }
}
