package com.example.finance_app;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.finance_app.models.Transaction;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TransactionStorage {
    private static final String PREFS_NAME = "TransactionPrefs";
    private static final String KEY_TRANSACTIONS = "transactions";
    private static TransactionStorage instance;
    private SharedPreferences prefs;
    private Gson gson;

    private TransactionStorage(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public static synchronized TransactionStorage getInstance(Context context) {
        if (instance == null) {
            instance = new TransactionStorage(context.getApplicationContext());
        }
        return instance;
    }

    public void saveTransaction(Transaction transaction) {
        List<Transaction> transactions = getTransactions();
        transactions.add(transaction);
        saveTransactions(transactions);
    }

    public List<Transaction> getTransactions() {
        String json = prefs.getString(KEY_TRANSACTIONS, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<Transaction>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public void saveTransactions(List<Transaction> transactions) {
        String json = gson.toJson(transactions);
        prefs.edit().putString(KEY_TRANSACTIONS, json).apply();
    }

    public void clearTransactions() {
        prefs.edit().remove(KEY_TRANSACTIONS).apply();
    }
}