package com.example.finance_app;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finance_app.adapters.NotificationAdapter;
import com.example.finance_app.models.Notification;
import com.example.finance_app.models.Budget;
import com.example.finance_app.models.Transaction;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private TextView emptyStateText;
    private List<Notification> notifications;
    private TransactionStorage transactionStorage;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notifications);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.notificationsRecyclerView);
        emptyStateText = findViewById(R.id.emptyStateText);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        gson = new Gson();
        transactionStorage = TransactionStorage.getInstance(this);
        loadNotifications();
    }

    private void loadNotifications() {
        notifications = new ArrayList<>();

        // Budget-based alerts
        Budget budget = loadBudget();
        if (budget != null) {
            addBudgetUsageAlerts(budget);
            addOnTrackSavingsAlert(budget);
        }

        if (notifications.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyStateText.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateText.setVisibility(View.GONE);
            adapter = new NotificationAdapter(notifications);
            recyclerView.setAdapter(adapter);
        }
    }

    private Budget loadBudget() {
        android.content.SharedPreferences prefs = getSharedPreferences("BudgetPrefs", MODE_PRIVATE);
        String json = prefs.getString("budget", null);
        if (json == null) return null;
        try {
            return gson.fromJson(json, Budget.class);
        } catch (Exception e) {
            return null;
        }
    }

    private void addBudgetUsageAlerts(Budget budget) {
        List<Transaction> transactions = transactionStorage.getTransactions();
        java.util.Calendar now = java.util.Calendar.getInstance();
        int m = now.get(java.util.Calendar.MONTH);
        int y = now.get(java.util.Calendar.YEAR);

        java.util.Map<String, Double> spentByCat = new java.util.HashMap<>();
        for (Transaction t : transactions) {
            if (!"Expense".equals(t.getType())) continue;
            java.util.Calendar c = java.util.Calendar.getInstance();
            c.setTime(t.getDate());
            if (c.get(java.util.Calendar.MONTH) == m && c.get(java.util.Calendar.YEAR) == y) {
                String cat = getTransactionCategory(t);
                spentByCat.put(cat, spentByCat.getOrDefault(cat, 0d) + t.getAmount());
            }
        }

        for (java.util.Map.Entry<String, Double> e : budget.getCategoryBudgets().entrySet()) {
            String cat = e.getKey();
            double limit = e.getValue();
            if (limit <= 0) continue;
            double spent = spentByCat.getOrDefault(cat, 0d);
            double ratio = spent / limit;
            if (ratio >= 0.8 && ratio < 1.0) {
                notifications.add(new Notification(java.util.UUID.randomUUID().toString(),
                        "Budget Warning",
                        String.format("80%% of %s budget used.", cat), new Date(), false, "budget_warning"));
            } else if (ratio >= 1.0) {
                notifications.add(new Notification(java.util.UUID.randomUUID().toString(),
                        "Budget Exceeded",
                        String.format("You've exceeded your %s budget.", cat), new Date(), true, "budget_exceeded"));
            }
        }
    }

    private void addOnTrackSavingsAlert(Budget budget) {
        // If monthly budget exists and remaining is positive, show on-track message
        double monthlyBudget = budget.getMonthlyBudget();
        if (monthlyBudget <= 0) return;

        double spent = 0d;
        List<Transaction> transactions = transactionStorage.getTransactions();
        java.util.Calendar now = java.util.Calendar.getInstance();
        int m = now.get(java.util.Calendar.MONTH);
        int y = now.get(java.util.Calendar.YEAR);
        for (Transaction t : transactions) {
            if (!"Expense".equals(t.getType())) continue;
            java.util.Calendar c = java.util.Calendar.getInstance();
            c.setTime(t.getDate());
            if (c.get(java.util.Calendar.MONTH) == m && c.get(java.util.Calendar.YEAR) == y) {
                spent += t.getAmount();
            }
        }
        double remaining = monthlyBudget - spent;
        if (remaining > 0) {
            notifications.add(new Notification(java.util.UUID.randomUUID().toString(),
                    "Savings Progress",
                    String.format("You're on track to save ₹%.0f.", remaining), new Date(), false, "savings_progress"));
        }
    }

    private String getTransactionCategory(Transaction transaction) {
        java.util.List<String> additionalItems = transaction.getAdditionalItems();
        if (additionalItems != null && !additionalItems.isEmpty()) {
            return additionalItems.get(0);
        }
        return transaction.getCategory();
    }
}