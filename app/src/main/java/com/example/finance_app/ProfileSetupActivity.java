package com.example.finance_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

public class ProfileSetupActivity extends AppCompatActivity {

    private TextInputEditText nameEditText;
    private AutoCompleteTextView currencySpinner;
    private TextInputEditText incomeEditText;
    private Button getStartedButton;

    private static final String PREFS_NAME = "FinanceAppPrefs";
    private static final String KEY_NAME = "name";
    private static final String KEY_CURRENCY = "currency";
    private static final String KEY_INCOME = "income";
    private static final String KEY_PROFILE_SETUP_COMPLETE = "profile_setup_complete";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.util.Log.d("ProfileSetupActivity", "onCreate: Starting ProfileSetupActivity");
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_setup);
        android.util.Log.d("ProfileSetupActivity", "onCreate: Content view set");
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profileSetup), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        android.util.Log.d("ProfileSetupActivity", "onCreate: Initializing views");
        // Initialize views
        nameEditText = findViewById(R.id.nameEditText);
        currencySpinner = findViewById(R.id.currencySpinner);
        incomeEditText = findViewById(R.id.incomeEditText);
        getStartedButton = findViewById(R.id.getStartedButton);
        android.util.Log.d("ProfileSetupActivity", "onCreate: Views initialized");

        // Set up currency spinner
        String[] currencies = {"USD", "EUR", "GBP", "INR", "JPY", "CAD", "AUD", "CHF", "CNY", "KRW"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, currencies);
        currencySpinner.setAdapter(adapter);

        // Set click listener for get started button
        getStartedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfileAndContinue();
            }
        });
    }

    private void saveProfileAndContinue() {
        String name = nameEditText.getText().toString().trim();
        String currency = currencySpinner.getText().toString().trim();
        String incomeStr = incomeEditText.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currency.isEmpty()) {
            Toast.makeText(this, "Please select a currency", Toast.LENGTH_SHORT).show();
            return;
        }

        double income = 0;
        if (!incomeStr.isEmpty()) {
            try {
                income = Double.parseDouble(incomeStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter a valid income amount", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Save to SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_CURRENCY, currency);
        editor.putString(KEY_INCOME, incomeStr);
        editor.putBoolean(KEY_PROFILE_SETUP_COMPLETE, true);
        editor.apply();

        // Show success message
        Toast.makeText(this, getString(R.string.profile_setup_success), Toast.LENGTH_LONG).show();

        // Navigate to main activity
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}