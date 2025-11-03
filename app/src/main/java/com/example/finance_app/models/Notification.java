package com.example.finance_app.models;

import java.util.Date;

public class Notification {
    private String id;
    private String title;
    private String message;
    private Date date;
    private boolean isRead;
    private String type; // e.g., "bill_reminder", "expense_entry", etc.

    // Default constructor
    public Notification() {}

    // Constructor with all fields
    public Notification(String id, String title, String message, Date date, boolean isRead, String type) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.date = date;
        this.isRead = isRead;
        this.type = type;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}