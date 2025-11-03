package com.example.finance_app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.example.finance_app.models.Transaction;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReminderScheduler {

    public static void schedulePredictedReminders(Context context) {
        List<com.example.finance_app.models.Transaction> all = TransactionStorage.getInstance(context).getTransactions();

        // Group by category for Expenses only
        Map<String, List<Calendar>> byCategory = new HashMap<>();
        for (Transaction t : all) {
            if (!"Expense".equals(t.getType())) continue;
            String cat = getTransactionCategory(t);
            byCategory.putIfAbsent(cat, new ArrayList<>());
            Calendar c = Calendar.getInstance();
            c.setTime(t.getDate());
            byCategory.get(cat).add(c);
        }

        for (Map.Entry<String, List<Calendar>> entry : byCategory.entrySet()) {
            String category = entry.getKey();
            List<Calendar> dates = entry.getValue();
            if (dates.size() < 3) continue; // need at least 3 occurrences to learn
            dates.sort(Calendar::compareTo);

            // Compute intervals (days) between consecutive occurrences
            List<Integer> intervals = new ArrayList<>();
            for (int i = 1; i < dates.size(); i++) {
                long diffMs = dates.get(i).getTimeInMillis() - dates.get(i - 1).getTimeInMillis();
                int days = (int) Math.round(diffMs / 86400000.0);
                intervals.add(days);
            }
            if (intervals.isEmpty()) continue;

            // Use last up-to-3 intervals to compute typical period
            int start = Math.max(0, intervals.size() - 3);
            List<Integer> recent = intervals.subList(start, intervals.size());
            int median = medianInt(recent);

            // Heuristics: clamp to common periods
            int periodDays = normalizePeriod(median);

            // Predict next occurrence
            Calendar last = dates.get(dates.size() - 1);
            Calendar next = (Calendar) last.clone();
            next.add(Calendar.DAY_OF_YEAR, periodDays);

            // Schedule at 9:00 AM local
            next.set(Calendar.HOUR_OF_DAY, 9);
            next.set(Calendar.MINUTE, 0);
            next.set(Calendar.SECOND, 0);
            next.set(Calendar.MILLISECOND, 0);

            // If time already passed, push one more period ahead
            Calendar now = Calendar.getInstance();
            if (next.before(now)) {
                next.add(Calendar.DAY_OF_YEAR, periodDays);
            }

            scheduleAlarm(context, category, next);
        }
    }

    private static void scheduleAlarm(Context context, String category, Calendar when) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("category", category);
        intent.putExtra("message", "It's time to add your regular " + category + " expense.");

        int requestCode = (category.hashCode() & 0x7fffffff); // stable per category
        PendingIntent pi = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long triggerAt = when.getTimeInMillis();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            am.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pi);
        } else {
            am.set(AlarmManager.RTC_WAKEUP, triggerAt, pi);
        }
    }

    private static int medianInt(List<Integer> values) {
        List<Integer> copy = new ArrayList<>(values);
        Collections.sort(copy);
        int n = copy.size();
        if (n % 2 == 1) return copy.get(n / 2);
        return (int) Math.round((copy.get(n / 2 - 1) + copy.get(n / 2)) / 2.0);
    }

    private static int normalizePeriod(int days) {
        if (days >= 27 && days <= 32) return 30; // monthly-ish
        if (days >= 20 && days <= 26) return 21; // ~3 weeks
        if (days >= 13 && days <= 16) return 14; // biweekly
        if (days >= 6 && days <= 8) return 7;    // weekly
        if (days >= 40 && days <= 62) return 56; // 8 weeks catch-all
        return Math.max(7, Math.min(days, 60));  // clamp to 1–2 months
    }

    private static String getTransactionCategory(Transaction transaction) {
        List<String> additionalItems = transaction.getAdditionalItems();
        if (additionalItems != null && !additionalItems.isEmpty()) {
            return additionalItems.get(0);
        }
        return transaction.getCategory();
    }
}


