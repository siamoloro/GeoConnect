package com.example.geoconnect;

import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class BusinessOperatingHoursActivity extends AppCompatActivity {

    private final String[] timeFieldIds = {
            "edtMondayOpening", "edtMondayClosing",
            "edtTuesdayOpening", "edtTuesdayClosing",
            "edtWednesdayOpening", "edtWednesdayClosing",
            "edtThursdayOpening", "edtThursdayClosing",
            "edtFridayOpening", "edtFridayClosing",
            "edtSaturdayOpening", "edtSaturdayClosing",
            "edtSundayOpening", "edtSundayClosing"
    };

    // Boolean states for each day's "Closed" status
    private boolean isMondayClosed = false;
    private boolean isTuesdayClosed = false;
    private boolean isWednesdayClosed = false;
    private boolean isThursdayClosed = false;
    private boolean isFridayClosed = false;
    private boolean isSaturdayClosed = false;
    private boolean isSundayClosed = false;

    private MaterialButton btnSaveChanges;

    // Firebase instances
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userId;

    private final String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_business_operating_hours);
        FreelancerUi.applyInsets(findViewById(android.R.id.content));

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            Toast.makeText(this, "User session not found. Please log in again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        btnSaveChanges = findViewById(R.id.btnSaveChanges);

        // Setup click listeners for opening/closing time pickers
        for (String fieldId : timeFieldIds) {
            int resId = getResources().getIdentifier(fieldId, "id", getPackageName());
            TextView textView = findViewById(resId);

            if (textView != null) {
                textView.setOnClickListener(v -> openTimePicker(textView));
            }
        }

        setupClosedButtons();

        // 1. Load existing operating hours document if it already exists
        loadDataFromDatabase();

        // 2. Save or update changes to Firestore when button is clicked
        if (btnSaveChanges != null) {
            btnSaveChanges.setOnClickListener(v -> extractUserInputsAndSave());
        }
    }

    private void loadDataFromDatabase() {
        // Path: businesses/{userId}/operating_hours/schedule
        db.collection("businesses").document(userId)
                .collection("operating_hours").document("schedule")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Document exists: Read data and populate screen
                        for (String day : days) {
                            String openingTime = documentSnapshot.getString(day.toLowerCase() + "_opening");
                            String closingTime = documentSnapshot.getString(day.toLowerCase() + "_closing");
                            Boolean isClosed = documentSnapshot.getBoolean(day.toLowerCase() + "_is_closed");

                            int openingResId = getResources().getIdentifier("edt" + day + "Opening", "id", getPackageName());
                            int closingResId = getResources().getIdentifier("edt" + day + "Closing", "id", getPackageName());
                            int buttonResId  = getResources().getIdentifier("btn" + day + "Closed", "id", getPackageName());

                            TextView openingView = findViewById(openingResId);
                            TextView closingView = findViewById(closingResId);
                            MaterialButton btnClosed = findViewById(buttonResId);

                            if (openingView != null && openingTime != null) openingView.setText(openingTime);
                            if (closingView != null && closingTime != null) closingView.setText(closingTime);

                            boolean closedState = (isClosed != null) ? isClosed : false;
                            setClosedState(day, closedState);
                            if (btnClosed != null) {
                                updateClosedButtonStyle(btnClosed, closedState);
                            }
                        }
                    } else {
                        // Document does not exist yet: Leave fields at default or blank
                        Toast.makeText(this, "No operating hours found. Set your hours and click Save Changes.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load hours: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void openTimePicker(TextView targetTextView) {
        final Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (TimePicker view, int hourOfDay, int minute) -> {
                    String formattedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    targetTextView.setText(formattedTime);
                }, currentHour, currentMinute, false);

        timePickerDialog.show();
    }

    private void setupClosedButtons() {
        setupSingleClosedButton(R.id.btnMondayClosed, "Monday");
        setupSingleClosedButton(R.id.btnTuesdayClosed, "Tuesday");
        setupSingleClosedButton(R.id.btnWednesdayClosed, "Wednesday");
        setupSingleClosedButton(R.id.btnThursdayClosed, "Thursday");
        setupSingleClosedButton(R.id.btnFridayClosed, "Friday");
        setupSingleClosedButton(R.id.btnSaturdayClosed, "Saturday");
        setupSingleClosedButton(R.id.btnSundayClosed, "Sunday");
    }

    private void setupSingleClosedButton(int buttonId, String day) {
        MaterialButton btnClosed = findViewById(buttonId);

        if (btnClosed != null) {
            btnClosed.setOnClickListener(v -> {
                boolean currentState = getClosedState(day);
                boolean newState = !currentState;
                setClosedState(day, newState);
                updateClosedButtonStyle(btnClosed, newState);
            });
        }
    }

    private void updateClosedButtonStyle(MaterialButton button, boolean isClosed) {
        if (isClosed) {
            button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#114224")));
            button.setTextColor(Color.parseColor("#FFFFFF"));
        } else {
            button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
            button.setTextColor(Color.parseColor("#114224"));
        }
    }

    private boolean getClosedState(String day) {
        switch (day) {
            case "Monday": return isMondayClosed;
            case "Tuesday": return isTuesdayClosed;
            case "Wednesday": return isWednesdayClosed;
            case "Thursday": return isThursdayClosed;
            case "Friday": return isFridayClosed;
            case "Saturday": return isSaturdayClosed;
            case "Sunday": return isSundayClosed;
            default: return false;
        }
    }

    private void setClosedState(String day, boolean value) {
        switch (day) {
            case "Monday": isMondayClosed = value; break;
            case "Tuesday": isTuesdayClosed = value; break;
            case "Wednesday": isWednesdayClosed = value; break;
            case "Thursday": isThursdayClosed = value; break;
            case "Friday": isFridayClosed = value; break;
            case "Saturday": isSaturdayClosed = value; break;
            case "Sunday": isSundayClosed = value; break;
        }
    }

    private void extractUserInputsAndSave() {
        Map<String, Object> hoursMap = new HashMap<>();

        for (String day : days) {
            int openingResId = getResources().getIdentifier("edt" + day + "Opening", "id", getPackageName());
            int closingResId = getResources().getIdentifier("edt" + day + "Closing", "id", getPackageName());

            TextView openingView = findViewById(openingResId);
            TextView closingView = findViewById(closingResId);

            String openingTime = (openingView != null) ? openingView.getText().toString().trim() : "";
            String closingTime = (closingView != null) ? closingView.getText().toString().trim() : "";
            boolean isClosed   = getClosedState(day);

            // Put into map with lowercase prefix keys (e.g., monday_opening, monday_closing, monday_is_closed)
            String prefix = day.toLowerCase();
            hoursMap.put(prefix + "_opening", openingTime);
            hoursMap.put(prefix + "_closing", closingTime);
            hoursMap.put(prefix + "_is_closed", isClosed);
        }

        // Disable save button during network write to avoid duplicate clicks
        if (btnSaveChanges != null) {
            btnSaveChanges.setEnabled(false);
        }

        // Save or update the single "schedule" document under the operating_hours subcollection
        db.collection("businesses").document(userId)
                .collection("operating_hours").document("schedule")
                .set(hoursMap) // .set() creates the document if it doesn't exist, or overwrites/updates if it does
                .addOnSuccessListener(aVoid -> {
                    if (btnSaveChanges != null) {
                        btnSaveChanges.setEnabled(true);
                    }
                    Toast.makeText(BusinessOperatingHoursActivity.this, "Operating hours saved successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    if (btnSaveChanges != null) {
                        btnSaveChanges.setEnabled(true);
                    }
                    Toast.makeText(BusinessOperatingHoursActivity.this, "Failed to save hours: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}