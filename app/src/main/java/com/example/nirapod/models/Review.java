package com.example.nirapod.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Review {
    private int id;
    private int providerId;
    private String authorName;
    private String content;
    private float rating;
    private Date date;
    private int likes;
    private int dislikes;

    public Review() {
        this.date = new Date(); // Default to current date
    }

    public Review(String authorName, String content, float rating) {
        this.authorName = authorName;
        this.content = content;
        this.rating = rating;
        this.date = new Date();
        this.likes = 0;
        this.dislikes = 0;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProviderId() {
        return providerId;
    }

    public void setProviderId(int providerId) {
        this.providerId = providerId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        return sdf.format(date);
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public void incrementLikes() {
        this.likes++;
    }

    public int getDislikes() {
        return dislikes;
    }

    public void setDislikes(int dislikes) {
        this.dislikes = dislikes;
    }

    public void incrementDislikes() {
        this.dislikes++;
    }
}