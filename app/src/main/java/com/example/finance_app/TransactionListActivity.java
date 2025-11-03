package com.example.finance_app;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finance_app.adapters.TransactionAdapter;
import com.example.finance_app.models.Transaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TransactionListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TransactionAdapter adapter;
    private EditText searchEditText;
    private Spinner categorySpinner;
    private Spinner dateSpinner;
    private Button sortByAmountButton;
    private Button sortByDateButton;

    private List<Transaction> allTransactions;
    private List<Transaction> filteredTransactions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_transaction_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        searchEditText = findViewById(R.id.searchEditText);
        categorySpinner = findViewById(R.id.categorySpinner);
        dateSpinner = findViewById(R.id.dateSpinner);
        sortByAmountButton = findViewById(R.id.sortByAmountButton);
        sortByDateButton = findViewById(R.id.sortByDateButton);

        allTransactions = new ArrayList<>();
        filteredTransactions = new ArrayList<>();

        loadTransactions();
        setupSearch();
        setupFilters();
        setupSorting();
        setupSwipeGestures();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTransactions();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            loadTransactions();
        }
    }

    private void loadTransactions() {
        allTransactions = TransactionStorage.getInstance(this).getTransactions();
        filteredTransactions.clear();
        filteredTransactions.addAll(allTransactions);
        if (adapter == null) {
            adapter = new TransactionAdapter(filteredTransactions);
            adapter.setContext(this);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.updateTransactions(filteredTransactions);
        }
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterTransactions();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFilters() {
        // Category filter
        List<String> categories = new ArrayList<>();
        categories.add("All");
        for (Transaction t : allTransactions) {
            if (!categories.contains(t.getCategory())) {
                categories.add(t.getCategory());
            }
        }
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterTransactions();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Date filter (simplified to "All", "Today", "This Week", "This Month")
        List<String> dateFilters = new ArrayList<>();
        dateFilters.add("All");
        dateFilters.add("Today");
        dateFilters.add("This Week");
        dateFilters.add("This Month");
        ArrayAdapter<String> dateAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dateFilters);
        dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dateSpinner.setAdapter(dateAdapter);
        dateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterTransactions();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupSorting() {
        sortByAmountButton.setOnClickListener(v -> {
            Collections.sort(filteredTransactions, Comparator.comparingDouble(Transaction::getAmount));
            adapter.notifyDataSetChanged();
        });

        sortByDateButton.setOnClickListener(v -> {
            Collections.sort(filteredTransactions, Comparator.comparing(Transaction::getDate));
            adapter.notifyDataSetChanged();
        });
    }

    private void setupSwipeGestures() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (direction == ItemTouchHelper.LEFT) {
                    // Delete
                    adapter.deleteTransaction(position);
                } else if (direction == ItemTouchHelper.RIGHT) {
                    // Edit
                    adapter.editTransaction(position);
                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void filterTransactions() {
        String searchText = searchEditText.getText().toString().toLowerCase();
        String selectedCategory = categorySpinner.getSelectedItem() != null ? categorySpinner.getSelectedItem().toString() : "All";
        String selectedDateFilter = dateSpinner.getSelectedItem() != null ? dateSpinner.getSelectedItem().toString() : "All";

        filteredTransactions.clear();
        for (Transaction t : allTransactions) {
            boolean matchesSearch = searchText.isEmpty() ||
                t.getCategory().toLowerCase().contains(searchText) ||
                t.getNotes().toLowerCase().contains(searchText) ||
                t.getPaymentMethod().toLowerCase().contains(searchText);

            boolean matchesCategory = selectedCategory.equals("All") || t.getCategory().equals(selectedCategory);

            boolean matchesDate = selectedDateFilter.equals("All"); // Simplified, implement date logic if needed

            if (matchesSearch && matchesCategory && matchesDate) {
                filteredTransactions.add(t);
            }
        }
        adapter.notifyDataSetChanged();
    }
}