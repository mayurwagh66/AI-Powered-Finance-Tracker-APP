package com.example.finance_app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.app.AlertDialog;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.finance_app.models.Budget;
import com.example.finance_app.models.Transaction;
import com.google.gson.Gson;

import java.util.Calendar;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BudgetActivity extends AppCompatActivity {

    private EditText monthlyBudgetInput;
    private Button setMonthlyBudgetButton;
    private Button updateBudgetButton;
    private LinearLayout budgetProgressContainer;
    private LinearLayout recommendationsContainer;
    private TextView savingsStatusText;

    private Budget budget;
    private TransactionStorage transactionStorage;
    private SharedPreferences prefs;
    private Gson gson;

    private static final String PREFS_NAME = "BudgetPrefs";
    private static final String KEY_BUDGET = "budget";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_budget);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize components
        initializeViews();
        initializeStorage();

        // Load existing budget
        loadBudget();

        // Set up listeners
        setupListeners();

        // Display budget progress
        updateBudgetProgress();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update budget progress when returning to the activity
        loadBudget();
        updateBudgetProgress();
    }

    private void initializeViews() {
        monthlyBudgetInput = findViewById(R.id.monthlyBudgetInput);
        setMonthlyBudgetButton = findViewById(R.id.setMonthlyBudgetButton);
        updateBudgetButton = findViewById(R.id.updateBudgetButton);
        budgetProgressContainer = findViewById(R.id.budgetProgressContainer);
        recommendationsContainer = findViewById(R.id.recommendationsContainer);
        savingsStatusText = findViewById(R.id.savingsStatusText);
    }

    private void initializeStorage() {
        transactionStorage = TransactionStorage.getInstance(this);
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        gson = new Gson();
    }

    private void setupListeners() {
        setMonthlyBudgetButton.setOnClickListener(v -> setMonthlyBudget());
        updateBudgetButton.setOnClickListener(v -> updateBudgetProgress());
    }

    private void setMonthlyBudget() {
        String monthlyBudgetStr = monthlyBudgetInput.getText().toString().trim();
        if (monthlyBudgetStr.isEmpty()) {
            Toast.makeText(this, "Please enter a monthly budget amount", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double monthlyBudget = Double.parseDouble(monthlyBudgetStr);
            budget.setMonthlyBudget(monthlyBudget);
            saveBudget();
            Toast.makeText(this, "Monthly budget set successfully", Toast.LENGTH_SHORT).show();
            monthlyBudgetInput.setText("");
            updateBudgetProgress();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
        }
    }


    private void loadBudget() {
        String budgetJson = prefs.getString(KEY_BUDGET, null);
        if (budgetJson != null) {
            budget = gson.fromJson(budgetJson, Budget.class);
        } else {
            budget = new Budget();
        }
    }

    private void saveBudget() {
        String budgetJson = gson.toJson(budget);
        prefs.edit().putString(KEY_BUDGET, budgetJson).apply();
    }

    private void updateBudgetProgress() {
        budgetProgressContainer.removeAllViews();
        recommendationsContainer.removeAllViews();

        // Calculate current month spending
        double monthlySpent = calculateMonthlySpent();
        double monthlyBudget = budget.getMonthlyBudget();

        // Add monthly budget progress
        if (monthlyBudget > 0) {
            addBudgetProgressView("Monthly Budget", monthlySpent, monthlyBudget);
        }

        // Add budget progress for all categories from transactions
        List<Transaction> transactions = transactionStorage.getTransactions();
        Calendar cal = Calendar.getInstance();
        int currentMonth = cal.get(Calendar.MONTH);
        int currentYear = cal.get(Calendar.YEAR);

        // Collect unique categories from current month transactions
        Set<String> categories = new HashSet<>();
        for (Transaction transaction : transactions) {
            if (transaction.getType().equals("Expense")) {
                Calendar transCal = Calendar.getInstance();
                transCal.setTime(transaction.getDate());
                if (transCal.get(Calendar.MONTH) == currentMonth &&
                    transCal.get(Calendar.YEAR) == currentYear) {
                    // Use additional items as category names if available
                    String category = getTransactionCategory(transaction);
                    categories.add(category);
                }
            }
        }

        // Add budget progress for each category
        for (String category : categories) {
            double categoryBudget = budget.getCategoryBudget(category);
            double categorySpent = calculateCategorySpent(category);
            addBudgetProgressView(category, categorySpent, categoryBudget);
        }

        // Savings status
        if (monthlyBudget > 0) {
            double remaining = monthlyBudget - monthlySpent;
            if (remaining >= 0) {
                savingsStatusText.setText(String.format("You're on track to save ₹%.2f.", remaining));
                savingsStatusText.setTextColor(getColor(android.R.color.holo_green_dark));
            } else {
                savingsStatusText.setText(String.format("You've exceeded your budget by ₹%.2f.", Math.abs(remaining)));
                savingsStatusText.setTextColor(getColor(android.R.color.holo_red_dark));
            }
        } else {
            savingsStatusText.setText("");
        }

        // Recommendations: 90% of average spend per category (last 3 months)
        addBudgetRecommendations();
    }

    private String getTransactionCategory(Transaction transaction) {
        // Use additional items as category names if available
        List<String> additionalItems = transaction.getAdditionalItems();
        if (additionalItems != null && !additionalItems.isEmpty()) {
            // Use the first additional item as the category name
            return additionalItems.get(0);
        }
        // Fall back to regular category
        return transaction.getCategory();
    }

    private double calculateMonthlySpent() {
        List<Transaction> transactions = transactionStorage.getTransactions();
        Calendar cal = Calendar.getInstance();
        int currentMonth = cal.get(Calendar.MONTH);
        int currentYear = cal.get(Calendar.YEAR);

        double total = 0;
        for (Transaction transaction : transactions) {
            if (transaction.getType().equals("Expense")) {
                Calendar transCal = Calendar.getInstance();
                transCal.setTime(transaction.getDate());
                if (transCal.get(Calendar.MONTH) == currentMonth &&
                    transCal.get(Calendar.YEAR) == currentYear) {
                    total += transaction.getAmount();
                }
            }
        }
        return total;
    }

    private double calculateCategorySpent(String category) {
        List<Transaction> transactions = transactionStorage.getTransactions();
        Calendar cal = Calendar.getInstance();
        int currentMonth = cal.get(Calendar.MONTH);
        int currentYear = cal.get(Calendar.YEAR);

        double total = 0;
        for (Transaction transaction : transactions) {
            if (transaction.getType().equals("Expense")) {
                // Check if this transaction matches the category (using additional items if available)
                String transactionCategory = getTransactionCategory(transaction);
                if (transactionCategory.equals(category)) {
                    Calendar transCal = Calendar.getInstance();
                    transCal.setTime(transaction.getDate());
                    if (transCal.get(Calendar.MONTH) == currentMonth &&
                        transCal.get(Calendar.YEAR) == currentYear) {
                        total += transaction.getAmount();
                    }
                }
            }
        }
        return total;
    }

    private void addBudgetProgressView(String title, double spent, double budgetAmount) {
        View progressView = getLayoutInflater().inflate(R.layout.item_budget_progress, budgetProgressContainer, false);

        TextView titleText = progressView.findViewById(R.id.budgetTitleText);
        TextView progressText = progressView.findViewById(R.id.budgetProgressText);
        TextView remainingText = progressView.findViewById(R.id.budgetRemainingText);
        ProgressBar progressBar = progressView.findViewById(R.id.budgetProgressBar);

        titleText.setText(title);
        progressText.setText(String.format("₹%.2f / ₹%.2f", spent, budgetAmount));

        double remaining = budgetAmount - spent;
        remainingText.setText(String.format("Remaining: ₹%.2f", Math.max(remaining, 0)));

        int progress = budgetAmount > 0 ? (int) ((spent / budgetAmount) * 100) : 0;
        progressBar.setProgress(Math.min(progress, 100));

        // Change color if over budget
        if (spent > budgetAmount) {
            progressBar.setProgressTintList(getColorStateList(android.R.color.holo_red_dark));
            progressText.setTextColor(getColor(android.R.color.holo_red_dark));
            remainingText.setTextColor(getColor(android.R.color.holo_red_dark));
            Toast.makeText(this, "Budget exceeded for " + title, Toast.LENGTH_SHORT).show();
        } else if (progress > 80) {
            progressBar.setProgressTintList(getColorStateList(android.R.color.holo_orange_dark));
            remainingText.setTextColor(getColor(android.R.color.holo_orange_dark));
        } else {
            progressBar.setProgressTintList(getColorStateList(android.R.color.holo_green_dark));
            remainingText.setTextColor(getColor(android.R.color.darker_gray));
        }

        // Add click listener to edit budget
        progressView.setOnClickListener(v -> showEditBudgetDialog(title, budgetAmount));

        budgetProgressContainer.addView(progressView);
    }

    private void showEditBudgetDialog(String title, double currentBudget) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit " + title);

        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setText(String.valueOf(currentBudget));
        input.setSelection(input.getText().length());
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            try {
                double newBudget = Double.parseDouble(input.getText().toString().trim());
                if (title.equals("Monthly Budget")) {
                    budget.setMonthlyBudget(newBudget);
                } else {
                    budget.setCategoryBudget(title, newBudget);
                }
                saveBudget();
                updateBudgetProgress();
                Toast.makeText(this, title + " updated successfully", Toast.LENGTH_SHORT).show();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void addBudgetRecommendations() {
        Map<String, List<Double>> categoryMonthlyTotals = computeCategoryMonthlyTotals();
        for (Map.Entry<String, List<Double>> entry : categoryMonthlyTotals.entrySet()) {
            String category = entry.getKey();
            List<Double> months = entry.getValue();
            if (months.isEmpty()) continue;
            int n = months.size();
            double sum = 0d;
            int count = 0;
            for (int i = Math.max(0, n - 3); i < n; i++) {
                sum += months.get(i);
                count++;
            }
            double avg = count > 0 ? sum / count : 0d;
            double recommended = avg * 0.9; // 90% of average

            TextView tv = new TextView(this);
            tv.setText(String.format("Your average %s spending is ₹%.0f; try limiting to ₹%.0f next month.", category, avg, recommended));
            tv.setTextSize(14f);
            recommendationsContainer.addView(tv);
        }
    }

    private Map<String, List<Double>> computeCategoryMonthlyTotals() {
        List<Transaction> all = transactionStorage.getTransactions();
        Map<String, Map<String, Double>> catMonthMap = new HashMap<>(); // cat -> (yyyy-MM -> total)
        java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("yyyy-MM");

        for (Transaction t : all) {
            if (!"Expense".equals(t.getType())) continue;
            Calendar transCal = Calendar.getInstance();
            transCal.setTime(t.getDate());
            String key = fmt.format(transCal.getTime());
            String cat = getTransactionCategory(t);
            catMonthMap.putIfAbsent(cat, new HashMap<>());
            Map<String, Double> map = catMonthMap.get(cat);
            map.put(key, map.getOrDefault(key, 0d) + t.getAmount());
        }

        // Convert to lists ordered by month key
        Map<String, List<Double>> result = new HashMap<>();
        for (Map.Entry<String, Map<String, Double>> e : catMonthMap.entrySet()) {
            List<String> keys = new ArrayList<>(e.getValue().keySet());
            keys.sort(String::compareTo);
            List<Double> vals = new ArrayList<>();
            for (String k : keys) vals.add(e.getValue().get(k));
            result.put(e.getKey(), vals);
        }
        return result;
    }
}
