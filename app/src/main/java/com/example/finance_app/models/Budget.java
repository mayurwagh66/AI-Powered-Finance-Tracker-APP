package com.example.finance_app.models;

import java.util.HashMap;
import java.util.Map;

public class Budget {
    private double monthlyBudget;
    private Map<String, Double> categoryBudgets; // category -> budget amount

    // Default constructor
    public Budget() {
        this.monthlyBudget = 0.0;
        this.categoryBudgets = new HashMap<>();
    }

    // Constructor with monthly budget
    public Budget(double monthlyBudget) {
        this.monthlyBudget = monthlyBudget;
        this.categoryBudgets = new HashMap<>();
    }

    // Getters and Setters
    public double getMonthlyBudget() {
        return monthlyBudget;
    }

    public void setMonthlyBudget(double monthlyBudget) {
        this.monthlyBudget = monthlyBudget;
    }

    public Map<String, Double> getCategoryBudgets() {
        return categoryBudgets;
    }

    public void setCategoryBudgets(Map<String, Double> categoryBudgets) {
        this.categoryBudgets = categoryBudgets;
    }

    public void setCategoryBudget(String category, double amount) {
        this.categoryBudgets.put(category, amount);
    }

    public double getCategoryBudget(String category) {
        return this.categoryBudgets.getOrDefault(category, 0.0);
    }

    public void removeCategoryBudget(String category) {
        this.categoryBudgets.remove(category);
    }
}