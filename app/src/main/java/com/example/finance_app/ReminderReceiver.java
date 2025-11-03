package com.example.finance_app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String category = intent.getStringExtra("category");
        String message = intent.getStringExtra("message");
        int id = intent.getIntExtra("id", (int) System.currentTimeMillis());

        NotificationHelper helper = new NotificationHelper(context);
        String title = category != null ? ("Reminder: " + category) : "Expense Reminder";
        String body = message != null ? message : "It's time to add your regular expense.";
        helper.showReminderNotification(title, body, id);
    }
}


