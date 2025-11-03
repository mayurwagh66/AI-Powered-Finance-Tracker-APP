package com.example.finance_app;

import android.app.UiModeManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class SettingsActivity extends AppCompatActivity {

    private TextView nameTextView;
    private TextView currencyTextView;
    private TextView incomeTextView;
    private MaterialButton themeToggleButton;
    private MaterialButton logoutButton;

    private static final String PREFS_NAME = "FinanceAppPrefs";
    private static final String KEY_NAME = "name";
    private static final String KEY_CURRENCY = "currency";
    private static final String KEY_INCOME = "income";
    private static final String KEY_THEME = "theme";
    private static final String KEY_PROFILE_SETUP_COMPLETE = "profile_setup_complete";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.settings), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        nameTextView = findViewById(R.id.nameTextView);
        currencyTextView = findViewById(R.id.currencyTextView);
        incomeTextView = findViewById(R.id.incomeTextView);
        themeToggleButton = findViewById(R.id.themeToggleButton);
        logoutButton = findViewById(R.id.logoutButton);

        // Load and display user data
        loadUserData();

        // Set up theme toggle
        updateThemeButtonText();
        themeToggleButton.setOnClickListener(v -> toggleTheme());

        // Set up logout button
        logoutButton.setOnClickListener(v -> logout());
    }

    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String name = prefs.getString(KEY_NAME, "Not set");
        String currency = prefs.getString(KEY_CURRENCY, "Not set");
        String income = prefs.getString(KEY_INCOME, "Not set");

        nameTextView.setText("Name: " + name);
        currencyTextView.setText("Currency: " + currency);
        incomeTextView.setText("Income: " + income);
    }

    private void updateThemeButtonText() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int currentTheme = prefs.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        String buttonText;
        switch (currentTheme) {
            case AppCompatDelegate.MODE_NIGHT_YES:
                buttonText = "Switch to Light Theme";
                break;
            case AppCompatDelegate.MODE_NIGHT_NO:
                buttonText = "Switch to Dark Theme";
                break;
            default:
                buttonText = "Switch to Light Theme";
                break;
        }
        themeToggleButton.setText(buttonText);
    }

    private void toggleTheme() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int currentTheme = prefs.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        int newTheme;
        switch (currentTheme) {
            case AppCompatDelegate.MODE_NIGHT_YES:
                newTheme = AppCompatDelegate.MODE_NIGHT_NO;
                break;
            case AppCompatDelegate.MODE_NIGHT_NO:
                newTheme = AppCompatDelegate.MODE_NIGHT_YES;
                break;
            default:
                newTheme = AppCompatDelegate.MODE_NIGHT_NO;
                break;
        }

        // Save new theme preference
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_THEME, newTheme);
        editor.apply();

        // Apply the theme
        AppCompatDelegate.setDefaultNightMode(newTheme);

        // Update button text
        updateThemeButtonText();

        Toast.makeText(this, "Theme changed. Restart app to apply fully.", Toast.LENGTH_SHORT).show();
    }

    private void logout() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout? This will reset your profile setup.")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Clear all preferences
                    SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.clear();
                    editor.apply();

                    // Navigate to profile setup
                    Intent intent = new Intent(this, ProfileSetupActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }
}