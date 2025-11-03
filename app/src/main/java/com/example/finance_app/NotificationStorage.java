package com.example.finance_app;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.finance_app.models.Notification;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class NotificationStorage {
    private static final String PREFS_NAME = "NotificationPrefs";
    private static final String KEY_NOTIFICATIONS = "notifications";
    private static NotificationStorage instance;
    private SharedPreferences prefs;
    private Gson gson;

    private NotificationStorage(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public static synchronized NotificationStorage getInstance(Context context) {
        if (instance == null) {
            instance = new NotificationStorage(context.getApplicationContext());
        }
        return instance;
    }

    public void saveNotification(Notification notification) {
        List<Notification> notifications = getNotifications();
        notifications.add(notification);
        saveNotifications(notifications);
    }

    public List<Notification> getNotifications() {
        String json = prefs.getString(KEY_NOTIFICATIONS, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<Notification>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public void saveNotifications(List<Notification> notifications) {
        String json = gson.toJson(notifications);
        prefs.edit().putString(KEY_NOTIFICATIONS, json).apply();
    }

    public void clearNotifications() {
        prefs.edit().remove(KEY_NOTIFICATIONS).apply();
    }
}