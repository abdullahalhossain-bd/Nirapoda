package com.example.nirapod;


public class Review {
    private String id;
    private String author;
    private String date;
    private int rating;
    private String comment;
    private int likes;
    private int dislikes;
    private boolean userLiked;
    private boolean userDisliked;

    public Review(String id, String author, String date, int rating, String comment) {
        this.id = id;
        this.author = author;
        this.date = date;
        this.rating = rating;
        this.comment = comment;
        this.likes = 0;
        this.dislikes = 0;
        this.userLiked = false;
        this.userDisliked = false;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public String getDate() {
        return date;
    }

    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public int getLikes() {
        return likes;
    }

    public int getDislikes() {
        return dislikes;
    }

    public boolean isUserLiked() {
        return userLiked;
    }

    public boolean isUserDisliked() {
        return userDisliked;
    }

    // Methods for likes/dislikes
    public void like() {
        if (userLiked) {
            // Remove like
            likes--;
            userLiked = false;
        } else {
            // Add like and remove dislike if exists
            likes++;
            if (userDisliked) {
                dislikes--;
                userDisliked = false;
            }
            userLiked = true;
        }
    }

    public void dislike() {
        if (userDisliked) {
            // Remove dislike
            dislikes--;
            userDisliked = false;
        } else {
            // Add dislike and remove like if exists
            dislikes++;
            if (userLiked) {
                likes--;
                userLiked = false;
            }
            userDisliked = true;
        }
    }
}