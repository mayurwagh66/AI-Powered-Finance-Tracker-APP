package com.example.finance_app;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.finance_app.models.Transaction;
import com.example.finance_app.models.Budget;
import com.example.finance_app.models.Notification;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class AddTransactionActivity extends AppCompatActivity {

    private EditText amountEditText;
    private RadioGroup typeRadioGroup;
    private RadioButton incomeRadioButton;
    private RadioButton expenseRadioButton;
    private Spinner categorySpinner;
    private EditText dateEditText;
    private Spinner paymentMethodSpinner;
    private EditText notesEditText;
    private TextInputLayout customCategoryLayout;
    private EditText customCategoryEditText;
    private ArrayAdapter<String> additionalItemsAdapter;
    private Button saveButton;
    private Button additionalItemsButton;

    private Calendar calendar = Calendar.getInstance();
    private NotificationStorage notificationStorage;
    private SharedPreferences budgetPrefs;
    private Gson gson;
    private NotificationHelper notificationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_transaction);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        amountEditText = findViewById(R.id.amountEditText);
        typeRadioGroup = findViewById(R.id.typeRadioGroup);
        incomeRadioButton = findViewById(R.id.incomeRadioButton);
        expenseRadioButton = findViewById(R.id.expenseRadioButton);
        categorySpinner = findViewById(R.id.categorySpinner);
        dateEditText = findViewById(R.id.dateEditText);
        paymentMethodSpinner = findViewById(R.id.paymentMethodSpinner);
        notesEditText = findViewById(R.id.notesEditText);
        customCategoryLayout = findViewById(R.id.customCategoryLayout);
        customCategoryEditText = findViewById(R.id.customCategoryEditText);
        saveButton = findViewById(R.id.saveButton);
        additionalItemsButton = findViewById(R.id.additionalItemsButton);

        // Initialize additional items adapter
        additionalItemsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

        // Initialize notification and budget storage
        notificationStorage = NotificationStorage.getInstance(this);
        budgetPrefs = getSharedPreferences("BudgetPrefs", MODE_PRIVATE);
        gson = new Gson();
        notificationHelper = new NotificationHelper(this);

        // Set up category spinner
        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(
                this, R.array.categories, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        // Show additional items button and custom category input when category is selected (only for "Other")
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCategory = parent.getItemAtPosition(position).toString();
                if (selectedCategory.equals("Other")) {
                    additionalItemsButton.setVisibility(View.VISIBLE);
                    customCategoryLayout.setVisibility(View.VISIBLE);
                } else {
                    additionalItemsButton.setVisibility(View.GONE);
                    customCategoryLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                additionalItemsButton.setVisibility(View.GONE);
                customCategoryLayout.setVisibility(View.GONE);
            }
        });

        // Set up payment method spinner
        ArrayAdapter<CharSequence> paymentMethodAdapter = ArrayAdapter.createFromResource(
                this, R.array.payment_methods, android.R.layout.simple_spinner_item);
        paymentMethodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        paymentMethodSpinner.setAdapter(paymentMethodAdapter);

        // Set up date picker
        dateEditText.setOnClickListener(v -> showDatePickerDialog());

        // Set current date as default
        updateDateDisplay();

        // Set up additional items button (popup dialog)
        additionalItemsButton.setOnClickListener(v -> showAdditionalItemsDialog());

        // Initially hide the additional items button and custom category input
        additionalItemsButton.setVisibility(View.GONE);
        customCategoryLayout.setVisibility(View.GONE);

        // Set up save button
        saveButton.setOnClickListener(v -> saveTransaction());
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateDisplay();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void updateDateDisplay() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        dateEditText.setText(dateFormat.format(calendar.getTime()));
    }

    private void showAdditionalItemsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Additional Items");

        // Inflate the dialog layout
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_additional_items, null);
        builder.setView(dialogView);

        EditText dialogItemEditText = dialogView.findViewById(R.id.dialogItemEditText);
        Button dialogAddButton = dialogView.findViewById(R.id.dialogAddButton);
        ListView dialogItemsListView = dialogView.findViewById(R.id.dialogItemsListView);

        ArrayAdapter<String> dialogAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        dialogItemsListView.setAdapter(dialogAdapter);

        // Copy current items to dialog
        for (int i = 0; i < additionalItemsAdapter.getCount(); i++) {
            dialogAdapter.add(additionalItemsAdapter.getItem(i));
        }

        dialogAddButton.setOnClickListener(v -> {
            String item = dialogItemEditText.getText().toString().trim();
            if (!item.isEmpty()) {
                dialogAdapter.add(item);
                dialogItemEditText.setText("");
            } else {
                Toast.makeText(this, "Please enter an item", Toast.LENGTH_SHORT).show();
            }
        });

        dialogItemsListView.setOnItemClickListener((parent, view, position, id) -> {
            String item = dialogAdapter.getItem(position);
            dialogAdapter.remove(item);
            Toast.makeText(this, "Item removed: " + item, Toast.LENGTH_SHORT).show();
        });

        builder.setPositiveButton("Save", (dialog, which) -> {
            // Update main adapter with dialog items
            additionalItemsAdapter.clear();
            for (int i = 0; i < dialogAdapter.getCount(); i++) {
                additionalItemsAdapter.add(dialogAdapter.getItem(i));
            }
            Toast.makeText(this, "Additional items saved", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void saveTransaction() {
        // Validate inputs
        String amountStr = amountEditText.getText().toString().trim();
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get selected type
        String type;
        int selectedTypeId = typeRadioGroup.getCheckedRadioButtonId();
        if (selectedTypeId == R.id.incomeRadioButton) {
            type = "Income";
        } else {
            type = "Expense";
        }

        // Get category
        String category = categorySpinner.getSelectedItem().toString();
        if (category.equals("Other")) {
            String customCategory = customCategoryEditText.getText().toString().trim();
            if (!customCategory.isEmpty()) {
                category = customCategory;
            }
        }

        // Get date
        Date date = calendar.getTime();

        // Get payment method
        String paymentMethod = paymentMethodSpinner.getSelectedItem().toString();

        // Get notes
        String notes = notesEditText.getText().toString().trim();

        // Get additional items
        List<String> additionalItems = new ArrayList<>();
        for (int i = 0; i < additionalItemsAdapter.getCount(); i++) {
            additionalItems.add(additionalItemsAdapter.getItem(i));
        }

        // Create transaction object
        Transaction transaction = new Transaction(amount, type, category, date, paymentMethod, notes, additionalItems);

        // Save transaction to storage
        TransactionStorage.getInstance(this).saveTransaction(transaction);

        // Update smart reminders after new data
        ReminderScheduler.schedulePredictedReminders(this);

        // Check budgets and generate notifications if limits are hit
        if (type.equals("Expense")) {
            checkBudgetsAndNotify(transaction);
        }

        Toast.makeText(this, getString(R.string.transaction_saved), Toast.LENGTH_SHORT).show();

        // Return to main activity
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
    private void checkBudgetsAndNotify(Transaction transaction) {
        // Load budget
        String budgetJson = budgetPrefs.getString("budget", null);
        if (budgetJson == null) return;

        Budget budget = gson.fromJson(budgetJson, Budget.class);

        // Calculate current spending
        TransactionStorage transactionStorage = TransactionStorage.getInstance(this);
        double monthlySpent = calculateMonthlySpent(transactionStorage);
        double monthlyBudget = budget.getMonthlyBudget();

        // Check monthly budget
        if (monthlyBudget > 0 && monthlySpent >= monthlyBudget) {
            createBudgetNotification("Monthly Budget Limit Hit",
                "Your monthly budget of ₹" + monthlyBudget + " has been reached or exceeded.",
                "monthly_budget_hit");
        }

        // Check category budget
        String category = getTransactionCategory(transaction);
        double categoryBudget = budget.getCategoryBudget(category);
        double categorySpent = calculateCategorySpent(transactionStorage, category);

        if (categoryBudget > 0 && categorySpent >= categoryBudget) {
            createBudgetNotification("Category Budget Limit Hit",
                "Your budget for '" + category + "' of ₹" + categoryBudget + " has been reached or exceeded.",
                "category_budget_hit");
        }
    }

    private double calculateMonthlySpent(TransactionStorage transactionStorage) {
        List<Transaction> transactions = transactionStorage.getTransactions();
        Calendar cal = Calendar.getInstance();
        int currentMonth = cal.get(Calendar.MONTH);
        int currentYear = cal.get(Calendar.YEAR);

        double total = 0;
        for (Transaction t : transactions) {
            if (t.getType().equals("Expense")) {
                Calendar transCal = Calendar.getInstance();
                transCal.setTime(t.getDate());
                if (transCal.get(Calendar.MONTH) == currentMonth &&
                    transCal.get(Calendar.YEAR) == currentYear) {
                    total += t.getAmount();
                }
            }
        }
        return total;
    }

    private double calculateCategorySpent(TransactionStorage transactionStorage, String category) {
        List<Transaction> transactions = transactionStorage.getTransactions();
        Calendar cal = Calendar.getInstance();
        int currentMonth = cal.get(Calendar.MONTH);
        int currentYear = cal.get(Calendar.YEAR);

        double total = 0;
        for (Transaction t : transactions) {
            if (t.getType().equals("Expense")) {
                String transactionCategory = getTransactionCategory(t);
                if (transactionCategory.equals(category)) {
                    Calendar transCal = Calendar.getInstance();
                    transCal.setTime(t.getDate());
                    if (transCal.get(Calendar.MONTH) == currentMonth &&
                        transCal.get(Calendar.YEAR) == currentYear) {
                        total += t.getAmount();
                    }
                }
            }
        }
        return total;
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

    private void createBudgetNotification(String title, String message, String type) {
        String id = UUID.randomUUID().toString();
        Notification notification = new Notification(id, title, message, new Date(), false, type);
        notificationStorage.saveNotification(notification);

        // Show system notification with vibration
        int notificationId = id.hashCode(); // Use hashCode for unique notification ID
        notificationHelper.showBudgetNotification(title, message, notificationId);
    }
}