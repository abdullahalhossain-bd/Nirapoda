package com.example.nirapod.models;

public class Medication {
    private int id;
    private String name;
    private String dosage;
    private String time;
    private String instructions;
    private String frequency; // "Daily", "Specific days", "As needed"
    private String days; // For "Specific days" - comma separated day abbreviations: "Mon,Wed,Fri"
    private String status; // "Upcoming", "Taken", "Skipped"

    public Medication() {
        // Default constructor
    }

    public Medication(String name, String dosage, String time, String instructions,
                      String frequency, String days, String status) {
        this.name = name;
        this.dosage = dosage;
        this.time = time;
        this.instructions = instructions;
        this.frequency = frequency;
        this.days = days;
        this.status = status;
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

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getDays() {
        return days;
    }

    public void setDays(String days) {
        this.days = days;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Medication{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", dosage='" + dosage + '\'' +
                ", time='" + time + '\'' +
                ", instructions='" + instructions + '\'' +
                ", frequency='" + frequency + '\'' +
                ", days='" + days + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}