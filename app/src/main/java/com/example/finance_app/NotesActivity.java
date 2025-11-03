package com.example.finance_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.finance_app.models.CalendarEvent;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotesActivity extends AppCompatActivity {

    private TextView selectedDateText;
    private TextInputEditText noteTitleEdit;
    private TextInputEditText noteDescriptionEdit;
    private Button cancelButton;
    private Button saveButton;

    private Date selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notes);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.notes), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        selectedDateText = findViewById(R.id.selectedDateText);
        noteTitleEdit = findViewById(R.id.noteTitleEdit);
        noteDescriptionEdit = findViewById(R.id.noteDescriptionEdit);
        cancelButton = findViewById(R.id.cancelButton);
        saveButton = findViewById(R.id.saveButton);

        // Get selected date from intent
        long selectedDateMillis = getIntent().getLongExtra("selected_date", System.currentTimeMillis());
        selectedDate = new Date(selectedDateMillis);

        // Display selected date
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        selectedDateText.setText("Notes for: " + dateFormat.format(selectedDate));

        // Load existing note if any
        loadExistingNote();

        // Set up button listeners
        cancelButton.setOnClickListener(v -> finish());

        saveButton.setOnClickListener(v -> {
            saveNote();
            Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void loadExistingNote() {
        CalendarEventStorage storage = CalendarEventStorage.getInstance(this);
        List<CalendarEvent> events = storage.getEvents();
        for (CalendarEvent event : events) {
            if (storage.isSameDate(event.getDate(), selectedDate)) {
                noteTitleEdit.setText(event.getTitle());
                noteDescriptionEdit.setText(event.getDescription());
                break;
            }
        }
    }

    private void saveNote() {
        String title = noteTitleEdit.getText().toString().trim();
        String description = noteDescriptionEdit.getText().toString().trim();

        if (title.isEmpty()) {
            title = "Daily Spending";
        }
        if (description.isEmpty()) {
            description = "Total amount spent on this day";
        }

        CalendarEventStorage storage = CalendarEventStorage.getInstance(this);
        storage.saveNoteForDate(selectedDate, title, description);
    }
}