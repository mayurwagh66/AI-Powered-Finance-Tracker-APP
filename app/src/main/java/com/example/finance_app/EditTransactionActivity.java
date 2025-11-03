package com.example.finance_app;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.finance_app.models.Transaction;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EditTransactionActivity extends AppCompatActivity {

    private EditText amountEditText;
    private RadioGroup typeRadioGroup;
    private Spinner categorySpinner;
    private EditText dateEditText;
    private Spinner paymentMethodSpinner;
    private EditText notesEditText;
    private Button saveButton;

    private Transaction transaction;
    private int transactionIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_transaction); // Reuse the add transaction layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        amountEditText = findViewById(R.id.amountEditText);
        typeRadioGroup = findViewById(R.id.typeRadioGroup);
        categorySpinner = findViewById(R.id.categorySpinner);
        dateEditText = findViewById(R.id.dateEditText);
        paymentMethodSpinner = findViewById(R.id.paymentMethodSpinner);
        notesEditText = findViewById(R.id.notesEditText);
        saveButton = findViewById(R.id.saveButton);

        transactionIndex = getIntent().getIntExtra("transaction_index", -1);
        if (transactionIndex != -1) {
            List<Transaction> transactions = TransactionStorage.getInstance(this).getTransactions();
            transaction = transactions.get(transactionIndex);
            populateFields();
        }

        setupSpinners();
        setupDatePicker();
        saveButton.setOnClickListener(v -> saveTransaction());
    }

    private void populateFields() {
        amountEditText.setText(String.valueOf(transaction.getAmount()));
        if (transaction.getType().equals("Income")) {
            typeRadioGroup.check(R.id.incomeRadioButton);
        } else {
            typeRadioGroup.check(R.id.expenseRadioButton);
        }
        // Category spinner will be set in setupSpinners
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        dateEditText.setText(dateFormat.format(transaction.getDate()));
        // Payment method spinner will be set in setupSpinners
        notesEditText.setText(transaction.getNotes());
    }

    private void setupSpinners() {
        // Category spinner
        String[] categories = {"Food", "Transportation", "Entertainment", "Utilities", "Other"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);
        if (transaction != null) {
            int categoryPosition = categoryAdapter.getPosition(transaction.getCategory());
            categorySpinner.setSelection(categoryPosition);
        }

        // Payment method spinner
        String[] paymentMethods = {"Cash", "Credit Card", "Debit Card", "Bank Transfer", "Other"};
        ArrayAdapter<String> paymentAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, paymentMethods);
        paymentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        paymentMethodSpinner.setAdapter(paymentAdapter);
        if (transaction != null) {
            int paymentPosition = paymentAdapter.getPosition(transaction.getPaymentMethod());
            paymentMethodSpinner.setSelection(paymentPosition);
        }
    }

    private void setupDatePicker() {
        dateEditText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            if (transaction != null) {
                calendar.setTime(transaction.getDate());
            }
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
                String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                dateEditText.setText(selectedDate);
            }, year, month, day);
            datePickerDialog.show();
        });
    }

    private void saveTransaction() {
        try {
            double amount = Double.parseDouble(amountEditText.getText().toString());
            String type = ((RadioButton) findViewById(typeRadioGroup.getCheckedRadioButtonId())).getText().toString();
            String category = categorySpinner.getSelectedItem().toString();
            String dateString = dateEditText.getText().toString();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = dateFormat.parse(dateString);
            String paymentMethod = paymentMethodSpinner.getSelectedItem().toString();
            String notes = notesEditText.getText().toString();

            transaction.setAmount(amount);
            transaction.setType(type);
            transaction.setCategory(category);
            transaction.setDate(date);
            transaction.setPaymentMethod(paymentMethod);
            transaction.setNotes(notes);

            TransactionStorage.getInstance(this).saveTransactions(TransactionStorage.getInstance(this).getTransactions());
            Toast.makeText(this, "Transaction updated", Toast.LENGTH_SHORT).show();
            finish();
        } catch (ParseException e) {
            Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
        }
    }
}