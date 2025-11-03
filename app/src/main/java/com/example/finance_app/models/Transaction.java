package com.example.finance_app.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Transaction {
    private double amount;
    private String type; // "Income" or "Expense"
    private String category;
    private Date date;
    private String paymentMethod;
    private String notes;
    private List<String> additionalItems;

    // Default constructor
    public Transaction() {
        this.additionalItems = new ArrayList<>();
    }

    // Constructor with all fields
    public Transaction(double amount, String type, String category, Date date, String paymentMethod, String notes) {
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.date = date;
        this.paymentMethod = paymentMethod;
        this.notes = notes;
        this.additionalItems = new ArrayList<>();
    }

    // Constructor with additional items
    public Transaction(double amount, String type, String category, Date date, String paymentMethod, String notes, List<String> additionalItems) {
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.date = date;
        this.paymentMethod = paymentMethod;
        this.notes = notes;
        this.additionalItems = additionalItems != null ? new ArrayList<>(additionalItems) : new ArrayList<>();
    }

    // Getters and Setters
    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<String> getAdditionalItems() {
        return additionalItems;
    }

    public void setAdditionalItems(List<String> additionalItems) {
        this.additionalItems = additionalItems != null ? new ArrayList<>(additionalItems) : new ArrayList<>();
    }

    public void addAdditionalItem(String item) {
        if (this.additionalItems == null) {
            this.additionalItems = new ArrayList<>();
        }
        this.additionalItems.add(item);
    }

    public void removeAdditionalItem(String item) {
        if (this.additionalItems != null) {
            this.additionalItems.remove(item);
        }
    }
}