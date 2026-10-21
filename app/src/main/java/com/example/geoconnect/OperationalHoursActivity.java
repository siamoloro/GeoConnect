package com.example.geoconnect;

import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.Calendar;
import java.util.Locale;

public class OperationalHoursActivity extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_operational_hours);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        for (String fieldId : timeFieldIds) {
            int resId = getResources().getIdentifier(fieldId, "id", getPackageName());
            TextView textView = findViewById(resId);

            if (textView != null) {
                textView.setOnClickListener(v -> openTimePicker(textView));
            }
        }

        setupClosedButtons();

    }

    private void loadDataFromDatabase() {
        for (String fieldId : timeFieldIds) {
            int resId = getResources().getIdentifier(fieldId, "id", getPackageName());
            TextView textView = findViewById(resId);

            if (textView != null) {
                textView.setOnClickListener(v -> openTimePicker(textView));
            }
        }

        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        for (String day : days) {
            // Dynamically find the views using the loop technique
            int openingResId = getResources().getIdentifier("edt" + day + "Opening", "id", getPackageName());
            int closingResId = getResources().getIdentifier("edt" + day + "Closing", "id", getPackageName());
            int buttonResId  = getResources().getIdentifier("btn" + day + "Closed", "id", getPackageName());

            TextView openingView = findViewById(openingResId);
            TextView closingView = findViewById(closingResId);
            MaterialButton btnClosed = findViewById(buttonResId);

            // Mock Database Values for this Day
            String dbOpening = "09:00";
            String dbClosing = day.equals("Sunday") ? "" : "17:00";
            boolean dbIsClosed = day.equals("Sunday"); // Example: Sunday is closed from DB

            // Set the text into the views of this day
            if (openingView != null) openingView.setText(dbOpening);
            if (closingView != null) closingView.setText(dbClosing);

            // Update the boolean state and sync the Closed button color of this day
            setClosedState(day, dbIsClosed);
            if (btnClosed != null) {
                updateClosedButtonStyle(btnClosed, dbIsClosed);
            }
        }
    }

    private void openTimePicker(TextView targetTextView) {
        // Get the current system time to set as default in the picker
        final Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);

        // Initialize the TimePickerDialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (TimePicker view, int hourOfDay, int minute) -> {
                    // Format and display the selected time into the specific TextView that was clicked
                    String formattedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    targetTextView.setText(formattedTime);
                }, currentHour, currentMinute, false); // 'false' sets it to 12-hour format with AM/PM

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
                // Toggle boolean state
                boolean currentState = getClosedState(day);
                boolean newState = !currentState;
                setClosedState(day, newState);

                // Update button background and text colors based on boolean
                updateClosedButtonStyle(btnClosed, newState);
            });
        }
    }

    private void updateClosedButtonStyle(MaterialButton button, boolean isClosed) {
        if (isClosed) {
            // True: Background #114224, Text White
            button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#114224")));
            button.setTextColor(Color.parseColor("#FFFFFF"));
        } else {
            // False: Background White, Text #114224
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
        // --- B. Extract Operating Hours & Closed Status Dynamically ---
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

        for (String day : days) {
            int openingResId = getResources().getIdentifier("edt" + day + "Opening", "id", getPackageName());
            int closingResId = getResources().getIdentifier("edt" + day + "Closing", "id", getPackageName());

            TextView openingView = findViewById(openingResId);
            TextView closingView = findViewById(closingResId);

            // Extract the current text from the views and the boolean state
            String openingTime = (openingView != null) ? openingView.getText().toString().trim() : "";
            String closingTime = (closingView != null) ? closingView.getText().toString().trim() : "";
            boolean isClosed   = getClosedState(day);

            // TODO: Pass or save these values (openingTime, closingTime, isClosed)
            // to your database table/model for each specific day here.
        }

        // TODO: Finalize and execute your database save/update operation for the general info too
    }
}