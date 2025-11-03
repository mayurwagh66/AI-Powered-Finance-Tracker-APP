package com.example.finance_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.finance_app.models.CalendarEvent;

import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView welcomeText;
    private TextView incomeText;
    private Button addTransactionButton;
    private Button transactionHistoryButton;
    private Button viewBudgetButton;
    private Button reportsButton;
    private CalendarView calendarView;
    private TextView selectedDateText;
    private TextView totalSpentText;
    private Button addNoteButton;
    private TextView noteTitleText;
    private TextView noteDescriptionText;
    private Toolbar toolbar;

    private static final String PREFS_NAME = "FinanceAppPrefs";
    private static final String KEY_NAME = "name";
    private static final String KEY_CURRENCY = "currency";
    private static final String KEY_INCOME = "income";
    private static final String KEY_PROFILE_SETUP_COMPLETE = "profile_setup_complete";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.util.Log.d("MainActivity", "onCreate: Starting MainActivity");

        // Check if profile setup is complete
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isProfileSetupComplete = prefs.getBoolean(KEY_PROFILE_SETUP_COMPLETE, false);
        android.util.Log.d("MainActivity", "onCreate: Profile setup complete: " + isProfileSetupComplete);

        if (!isProfileSetupComplete) {
            // Redirect to profile setup
            android.util.Log.d("MainActivity", "onCreate: Redirecting to ProfileSetupActivity");
            try {
                Intent intent = new Intent(this, ProfileSetupActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                android.util.Log.d("MainActivity", "onCreate: Created intent for ProfileSetupActivity");
                startActivity(intent);
                android.util.Log.d("MainActivity", "onCreate: Started ProfileSetupActivity");
                finish();
                android.util.Log.d("MainActivity", "onCreate: Finished MainActivity");
                return;
            } catch (Exception e) {
                android.util.Log.e("MainActivity", "onCreate: Error starting ProfileSetupActivity", e);
                throw e;
            }
        }

        android.util.Log.d("MainActivity", "onCreate: Setting up UI");
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        android.util.Log.d("MainActivity", "onCreate: Content view set");
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        android.util.Log.d("MainActivity", "onCreate: Window insets listener set");

        android.util.Log.d("MainActivity", "onCreate: Initializing views");
        // Initialize views
        welcomeText = findViewById(R.id.welcomeText);
        incomeText = findViewById(R.id.incomeText);
        addTransactionButton = findViewById(R.id.addTransactionButton);
        transactionHistoryButton = findViewById(R.id.transactionHistoryButton);
        viewBudgetButton = findViewById(R.id.viewBudgetButton);
        reportsButton = findViewById(R.id.reportsButton);
        calendarView = findViewById(R.id.calendarView);
        selectedDateText = findViewById(R.id.selectedDateText);
        totalSpentText = findViewById(R.id.totalSpentText);
        addNoteButton = findViewById(R.id.addNoteButton);
        noteTitleText = findViewById(R.id.noteTitleText);
        noteDescriptionText = findViewById(R.id.noteDescriptionText);
        toolbar = findViewById(R.id.toolbar);
        android.util.Log.d("MainActivity", "onCreate: Views initialized");

        // Load and display user data
        android.util.Log.d("MainActivity", "onCreate: Loading user data");
        loadUserData();

        // Set up calendar
        android.util.Log.d("MainActivity", "onCreate: Setting up calendar");
        setupCalendar();
        android.util.Log.d("MainActivity", "onCreate: MainActivity setup complete");

        // Set up toolbar
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_hamburger);

        // Schedule smart reminders based on learned patterns
        ReminderScheduler.schedulePredictedReminders(this);

        // Set up add note button
        addNoteButton.setOnClickListener(v -> {
            long selectedDateMillis = calendarView.getDate();
            Intent intent = new Intent(this, NotesActivity.class);
            intent.putExtra("selected_date", selectedDateMillis);
            startActivity(intent);
        });

        // Set up click listeners for quick action buttons
        addTransactionButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddTransactionActivity.class);
            startActivity(intent);
        });

        transactionHistoryButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, TransactionListActivity.class);
            startActivity(intent);
        });

        viewBudgetButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, BudgetActivity.class);
            startActivity(intent);
        });

        reportsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ReportsActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String name = prefs.getString(KEY_NAME, "");
        String currency = prefs.getString(KEY_CURRENCY, "");
        String income = prefs.getString(KEY_INCOME, "");

        // Display welcome message
        if (!name.isEmpty()) {
            welcomeText.setText(String.format(getString(R.string.welcome_message), name));
        } else {
            welcomeText.setText("Welcome!");
        }

        // Display income
        if (!income.isEmpty() && !currency.isEmpty()) {
            incomeText.setText(String.format(getString(R.string.income_display), currency + " " + income));
        } else if (!income.isEmpty()) {
            incomeText.setText(String.format(getString(R.string.income_display), income));
        } else {
            incomeText.setText("Income: Not set");
        }
    }

    private void setupCalendar() {
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            // Create date from selected calendar date
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.set(year, month, dayOfMonth);
            Date selectedDate = calendar.getTime();

            // Update selected date text
            java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
            selectedDateText.setText("Selected: " + dateFormat.format(selectedDate));

            // Calculate and display total spent for this date
            double totalSpent = calculateTotalSpentForDate(selectedDate);
            String currency = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString(KEY_CURRENCY, "$");
            totalSpentText.setText(String.format(java.util.Locale.getDefault(), "Total Spent: %s%.2f", currency, totalSpent));

            // Update calendar event storage
            CalendarEventStorage.getInstance(this).updateEventForDate(selectedDate, totalSpent);

            // Update note display
            updateNoteDisplay(selectedDate);
        });

        // Set up double-click detection for opening notes
        final long[] lastClickTime = {0};
        calendarView.setOnClickListener(v -> {
            long clickTime = System.currentTimeMillis();
            if (clickTime - lastClickTime[0] < 300) { // Double click within 300ms
                // Get current selected date
                long selectedDateMillis = calendarView.getDate();
                Intent intent = new Intent(this, NotesActivity.class);
                intent.putExtra("selected_date", selectedDateMillis);
                startActivity(intent);
            }
            lastClickTime[0] = clickTime;
        });

        // Set current date as default selection
        long currentTimeMillis = System.currentTimeMillis();
        calendarView.setDate(currentTimeMillis);

        // Trigger initial date selection
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTimeInMillis(currentTimeMillis);
        Date initialDate = calendar.getTime();
        updateNoteDisplay(initialDate);
    }

    private double calculateTotalSpentForDate(Date date) {
        List<com.example.finance_app.models.Transaction> transactions = TransactionStorage.getInstance(this).getTransactions();
        double totalSpent = 0.0;

        java.util.Calendar selectedCal = java.util.Calendar.getInstance();
        selectedCal.setTime(date);

        for (com.example.finance_app.models.Transaction transaction : transactions) {
            if (transaction.getType().equals("Expense")) {
                java.util.Calendar transCal = java.util.Calendar.getInstance();
                transCal.setTime(transaction.getDate());

                if (selectedCal.get(java.util.Calendar.YEAR) == transCal.get(java.util.Calendar.YEAR) &&
                    selectedCal.get(java.util.Calendar.MONTH) == transCal.get(java.util.Calendar.MONTH) &&
                    selectedCal.get(java.util.Calendar.DAY_OF_MONTH) == transCal.get(java.util.Calendar.DAY_OF_MONTH)) {
                    totalSpent += transaction.getAmount();
                }
            }
        }

        return totalSpent;
    }


    private void updateNoteDisplay(Date date) {
        CalendarEventStorage storage = CalendarEventStorage.getInstance(this);
        List<CalendarEvent> events = storage.getEvents();
        for (CalendarEvent event : events) {
            if (storage.isSameDate(event.getDate(), date)) {
                noteTitleText.setText("Note Title: " + event.getTitle());
                noteDescriptionText.setText("Note Description: " + event.getDescription());
                return;
            }
        }
        // No note found
        noteTitleText.setText("Note Title: None");
        noteDescriptionText.setText("Note Description: None");
    }

}