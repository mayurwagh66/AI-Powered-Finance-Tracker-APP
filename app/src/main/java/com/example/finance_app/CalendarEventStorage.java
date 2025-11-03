package com.example.finance_app;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.finance_app.models.CalendarEvent;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class CalendarEventStorage {
    private static CalendarEventStorage instance;
    private static final String PREFS_NAME = "CalendarEventPrefs";
    private static final String EVENTS_KEY = "events";
    private Context context;
    private List<CalendarEvent> events;

    private CalendarEventStorage(Context context) {
        this.context = context.getApplicationContext();
        loadEvents();
    }

    public static synchronized CalendarEventStorage getInstance(Context context) {
        if (instance == null) {
            instance = new CalendarEventStorage(context);
        }
        return instance;
    }

    private void loadEvents() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String eventsJson = prefs.getString(EVENTS_KEY, null);
        if (eventsJson != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<CalendarEvent>>() {}.getType();
            events = gson.fromJson(eventsJson, type);
        } else {
            events = new ArrayList<>();
        }
    }

    private void saveEvents() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String eventsJson = gson.toJson(events);
        editor.putString(EVENTS_KEY, eventsJson);
        editor.apply();
    }

    public List<CalendarEvent> getEvents() {
        return new ArrayList<>(events);
    }

    public void updateEventForDate(Date date, double totalSpent) {
        CalendarEvent existingEvent = findEventByDate(date);
        if (existingEvent != null) {
            existingEvent.setTotalSpent(totalSpent);
        } else {
            CalendarEvent newEvent = new CalendarEvent(UUID.randomUUID().toString(), date, "Daily Spending", "Total amount spent on this day", totalSpent);
            events.add(newEvent);
        }
        saveEvents();
    }

    public void saveNoteForDate(Date date, String title, String description) {
        CalendarEvent existingEvent = findEventByDate(date);
        if (existingEvent != null) {
            existingEvent.setTitle(title);
            existingEvent.setDescription(description);
        } else {
            CalendarEvent newEvent = new CalendarEvent(UUID.randomUUID().toString(), date, title, description, 0.0);
            events.add(newEvent);
        }
        saveEvents();
    }

    public boolean isSameDate(Date date1, Date date2) {
        java.util.Calendar cal1 = java.util.Calendar.getInstance();
        cal1.setTime(date1);
        java.util.Calendar cal2 = java.util.Calendar.getInstance();
        cal2.setTime(date2);
        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
               cal1.get(java.util.Calendar.MONTH) == cal2.get(java.util.Calendar.MONTH) &&
               cal1.get(java.util.Calendar.DAY_OF_MONTH) == cal2.get(java.util.Calendar.DAY_OF_MONTH);
    }

    private CalendarEvent findEventByDate(Date date) {
        for (CalendarEvent event : events) {
            if (isSameDate(event.getDate(), date)) {
                return event;
            }
        }
        return null;
    }
}