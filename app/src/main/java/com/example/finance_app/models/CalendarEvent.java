package com.example.finance_app.models;

import java.util.Date;

public class CalendarEvent {
    private String id;
    private Date date;
    private String title;
    private String description;
    private double totalSpent; // Calculated field for total spending on this date

    // Default constructor
    public CalendarEvent() {}

    // Constructor with all fields
    public CalendarEvent(String id, Date date, String title, String description, double totalSpent) {
        this.id = id;
        this.date = date;
        this.title = title;
        this.description = description;
        this.totalSpent = totalSpent;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(double totalSpent) {
        this.totalSpent = totalSpent;
    }
}