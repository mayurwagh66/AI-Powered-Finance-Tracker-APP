package com.example.finance_app;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.finance_app.models.Transaction;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportsActivity extends AppCompatActivity {

    private PieChart pieChart;
    private BarChart barChart;
    private RadioGroup filterGroup;
    private TextView predictedSpendingText;
    private LinearLayout insightsContainer;
    private TransactionStorage transactionStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reports);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        pieChart = findViewById(R.id.pieChart);
        barChart = findViewById(R.id.barChart);
        filterGroup = findViewById(R.id.filterGroup);
        predictedSpendingText = findViewById(R.id.predictedSpendingText);
        insightsContainer = findViewById(R.id.insightsContainer);

        transactionStorage = TransactionStorage.getInstance(this);

        filterGroup.setOnCheckedChangeListener((group, checkedId) -> {
            updateCharts();
        });

        updateCharts();
        updateForecastAndInsights();
    }

    private void updateCharts() {
        List<Transaction> transactions = transactionStorage.getTransactions();
        List<Transaction> filteredTransactions = filterTransactions(transactions);

        setupPieChart(filteredTransactions);
        setupBarChart(filteredTransactions);
    }

    private List<Transaction> filterTransactions(List<Transaction> transactions) {
        int selectedId = filterGroup.getCheckedRadioButtonId();
        Calendar cal = Calendar.getInstance();
        Calendar filterCal = Calendar.getInstance();

        if (selectedId == R.id.filterWeekly) {
            cal.add(Calendar.DAY_OF_YEAR, -7);
        } else if (selectedId == R.id.filterMonthly) {
            cal.add(Calendar.MONTH, -1);
        } else if (selectedId == R.id.filterYearly) {
            cal.add(Calendar.YEAR, -1);
        }

        List<Transaction> filtered = new ArrayList<>();
        for (Transaction t : transactions) {
            filterCal.setTime(t.getDate());
            if (filterCal.after(cal) || filterCal.equals(cal)) {
                filtered.add(t);
            }
        }
        return filtered;
    }

    private void setupPieChart(List<Transaction> transactions) {
        Map<String, Float> categoryExpenses = new HashMap<>();

        for (Transaction t : transactions) {
            if ("Expense".equals(t.getType())) {
                // Use additional items as category names if available, otherwise use regular category
                String category = getTransactionCategory(t);
                categoryExpenses.put(category, categoryExpenses.getOrDefault(category, 0f) + (float) t.getAmount());
            }
        }

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Float> entry : categoryExpenses.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Expense Categories");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false);
        pieChart.invalidate();
    }

    private void setupBarChart(List<Transaction> transactions) {
        Map<String, Float> monthlyIncome = new HashMap<>();
        Map<String, Float> monthlyExpenses = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM yyyy");

        for (Transaction t : transactions) {
            String monthYear = sdf.format(t.getDate());
            if ("Income".equals(t.getType())) {
                monthlyIncome.put(monthYear, monthlyIncome.getOrDefault(monthYear, 0f) + (float) t.getAmount());
            } else if ("Expense".equals(t.getType())) {
                monthlyExpenses.put(monthYear, monthlyExpenses.getOrDefault(monthYear, 0f) + (float) t.getAmount());
            }
        }

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int index = 0;

        // Assuming we want to show last 12 months or available data
        for (String month : monthlyIncome.keySet()) {
            labels.add(month);
            float income = monthlyIncome.getOrDefault(month, 0f);
            float expense = monthlyExpenses.getOrDefault(month, 0f);
            entries.add(new BarEntry(index++, income - expense)); // Net income
        }

        BarDataSet dataSet = new BarDataSet(entries, "Monthly Net Income");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        BarData data = new BarData(dataSet);
        barChart.setData(data);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);

        barChart.getDescription().setEnabled(false);
        barChart.invalidate();
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

    private void updateForecastAndInsights() {
        List<Transaction> all = transactionStorage.getTransactions();
        Map<String, Double> monthToExpense = new HashMap<>();
        SimpleDateFormat keyFmt = new SimpleDateFormat("yyyy-MM");

        for (Transaction t : all) {
            if (!"Expense".equals(t.getType())) continue;
            String key = keyFmt.format(t.getDate());
            monthToExpense.put(key, monthToExpense.getOrDefault(key, 0d) + t.getAmount());
        }

        // Sort month keys
        List<String> months = new ArrayList<>(monthToExpense.keySet());
        months.sort(String::compareTo);

        // Prediction: average of last 3 months, weighted (3,2,1)
        double predicted = 0d;
        if (!months.isEmpty()) {
            int n = months.size();
            double numerator = 0d;
            double denom = 0d;
            int weight = 3;
            for (int i = Math.max(0, n - 3); i < n; i++) {
                double val = monthToExpense.get(months.get(i));
                int w = weight - (n - 1 - i); // 3 for most recent, then 2, then 1
                if (w < 1) w = 1;
                numerator += w * val;
                denom += w;
            }
            predicted = denom > 0 ? numerator / denom : 0d;
        }
        predictedSpendingText.setText(String.format("Predicted Next Month Spending: ₹%.2f", predicted));

        // Insights
        insightsContainer.removeAllViews();
        if (months.size() >= 2) {
            String last = months.get(months.size() - 1);
            String prev = months.get(months.size() - 2);
            double lastVal = monthToExpense.getOrDefault(last, 0d);
            double prevVal = monthToExpense.getOrDefault(prev, 0d);
            double diff = lastVal - prevVal;
            double pct = prevVal > 0 ? (diff / prevVal) * 100.0 : 0.0;
            String header;
            if (diff > 0) {
                header = String.format("Your spending increased by %.0f%% last month.", pct);
            } else if (diff < 0) {
                header = String.format("Good job! You spent %.0f%% less than previous month.", Math.abs(pct));
            } else {
                header = "Spending was the same as previous month.";
            }
            addInsight(header);

            // Per-category trends for last two months
            Map<String, Double> catPrev = new HashMap<>();
            Map<String, Double> catLast = new HashMap<>();
            Calendar cal = Calendar.getInstance();
            for (Transaction t : all) {
                if (!"Expense".equals(t.getType())) continue;
                String key = keyFmt.format(t.getDate());
                String cat = getTransactionCategory(t);
                if (prev.equals(key)) {
                    catPrev.put(cat, catPrev.getOrDefault(cat, 0d) + t.getAmount());
                } else if (last.equals(key)) {
                    catLast.put(cat, catLast.getOrDefault(cat, 0d) + t.getAmount());
                }
            }
            // Union of categories
            Map<String, Double> allCats = new HashMap<>(catPrev);
            for (Map.Entry<String, Double> e : catLast.entrySet()) {
                allCats.putIfAbsent(e.getKey(), 0d);
            }
            for (String cat : new ArrayList<>(allCats.keySet())) {
                double a = catPrev.getOrDefault(cat, 0d);
                double b = catLast.getOrDefault(cat, 0d);
                double cdiff = b - a;
                if (Math.abs(cdiff) < 0.01) continue;
                if (cdiff < 0) {
                    addInsight(String.format("You saved ₹%.0f on %s.", Math.abs(cdiff), cat));
                } else {
                    addInsight(String.format("%s spending increased by ₹%.0f.", cat, cdiff));
                }
            }
        } else {
            addInsight("Add more data to see month-over-month insights.");
        }
    }

    private void addInsight(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(14f);
        insightsContainer.addView(tv);
    }
}