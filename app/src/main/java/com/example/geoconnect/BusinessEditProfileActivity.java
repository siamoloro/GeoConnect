package com.example.geoconnect;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.app.TimePickerDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class BusinessEditProfileActivity extends AppCompatActivity {


    private AutoCompleteTextView spinnerDay;
    private MaterialButton btnOpeningTime, btnClosingTime;
    private MaterialButton btnAddHours, btnAddOffering;

    private CheckBox checkClosed, checkAllDay;

    private LinearLayout layoutOperatingHours, layoutOfferings;

    private TextInputEditText edtOfferingName;
    private TextInputEditText edtOfferingDescription;
    private TextInputEditText edtOfferingPrice;

    private String openingTime = "09:00 AM";
    private String closingTime = "05:00 PM";

    private final String[] days = {
            "Monday", "Tuesday", "Wednesday",
            "Thursday", "Friday", "Saturday", "Sunday"
    };

    // One operating-hours entry per day.
    private final Map<String, String> operatingHours =
            new HashMap<>();

    private final ArrayList<Offering> offerings =
            new ArrayList<>();

    private static class Offering {
        String name;
        String description;
        String price;

        Offering(String name, String description, String price) {
            this.name = name;
            this.description = description;
            this.price = price;
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_business_edit_profile);
        setupOperationsAndOfferings();
    }

    private void setupOperationsAndOfferings() {
        spinnerDay = findViewById(R.id.spinnerDay);
        btnOpeningTime = findViewById(R.id.btnOpeningTime);
        btnClosingTime = findViewById(R.id.btnClosingTime);
        btnAddHours = findViewById(R.id.btnAddHours);
        btnAddOffering = findViewById(R.id.btnAddOffering);

        checkClosed = findViewById(R.id.checkClosed);
        checkAllDay = findViewById(R.id.checkAllDay);

        layoutOperatingHours =
                findViewById(R.id.layoutOperatingHours);
        layoutOfferings = findViewById(R.id.layoutOfferings);

        edtOfferingName = findViewById(R.id.edtOfferingName);
        edtOfferingDescription =
                findViewById(R.id.edtOfferingDescription);
        edtOfferingPrice = findViewById(R.id.edtOfferingPrice);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                days
        );

        spinnerDay.setAdapter(adapter);
        spinnerDay.setText(days[0], false);

        btnOpeningTime.setOnClickListener(v ->
                showTimePicker(true));

        btnClosingTime.setOnClickListener(v ->
                showTimePicker(false));

        checkClosed.setOnCheckedChangeListener((button, checked) -> {
            if (checked) {
                checkAllDay.setChecked(false);
            }
            updateTimeControls();
        });

        checkAllDay.setOnCheckedChangeListener((button, checked) -> {
            if (checked) {
                checkClosed.setChecked(false);
            }
            updateTimeControls();
        });

        btnAddHours.setOnClickListener(v -> addOperatingHours());

        btnAddOffering.setOnClickListener(v -> addOffering());

        updateTimeControls();
        renderOperatingHours();
        renderOfferings();
    }

    private void showTimePicker(boolean isOpeningTime) {
        Calendar calendar = Calendar.getInstance();

        String current = isOpeningTime
                ? openingTime : closingTime;

        try {
            calendar.setTime(new SimpleDateFormat(
                    "hh:mm a", Locale.getDefault()).parse(current));
        } catch (Exception ignored) {
            // Use the current time if parsing fails.
        }

        new TimePickerDialog(
                this,
                (view, hour, minute) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(Calendar.HOUR_OF_DAY, hour);
                    selected.set(Calendar.MINUTE, minute);

                    String formatted = new SimpleDateFormat(
                            "hh:mm a",
                            Locale.getDefault()
                    ).format(selected.getTime());

                    if (isOpeningTime) {
                        openingTime = formatted;
                        btnOpeningTime.setText("Opens: " + formatted);
                    } else {
                        closingTime = formatted;
                        btnClosingTime.setText("Closes: " + formatted);
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
        ).show();
    }

    private void updateTimeControls() {
        boolean enabled =
                !checkClosed.isChecked() && !checkAllDay.isChecked();

        btnOpeningTime.setEnabled(enabled);
        btnClosingTime.setEnabled(enabled);
    }


    private void addOperatingHours() {
        String day = spinnerDay.getText().toString().trim();

        if (!java.util.Arrays.asList(days).contains(day)) {
            Toast.makeText(this, "Select a valid day.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (operatingHours.containsKey(day)) {
            Toast.makeText(this,
                    day + " already has operating hours.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String hours;

        if (checkClosed.isChecked()) {
            hours = "Closed";
        } else if (checkAllDay.isChecked()) {
            hours = "Open 24 hours";
        } else {
            if (openingTime.equals(closingTime)) {
                Toast.makeText(this,
                        "Opening and closing times cannot be equal.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            hours = openingTime + " - " + closingTime;
        }

        operatingHours.put(day, hours);
        renderOperatingHours();

        // Advance to the next day that has not been added.
        for (String nextDay : days) {
            if (!operatingHours.containsKey(nextDay)) {
                spinnerDay.setText(nextDay, false);
                break;
            }
        }

        checkClosed.setChecked(false);
        checkAllDay.setChecked(false);
        updateTimeControls();
    }

    private void renderOperatingHours() {
        layoutOperatingHours.removeAllViews();

        if (operatingHours.isEmpty()) {
            addEmptyMessage(layoutOperatingHours,
                    "No operating hours added yet.");
            return;
        }

        for (String day : days) {
            if (!operatingHours.containsKey(day)) {
                continue;
            }

            String hours = operatingHours.get(day);

            addEntryRow(
                    layoutOperatingHours,
                    day,
                    hours,
                    () -> {
                        operatingHours.remove(day);
                        renderOperatingHours();
                    }
            );
        }

    }

    private void addOffering() {
        String name = edtOfferingName.getText() == null
                ? "" : edtOfferingName.getText().toString().trim();

        String description = edtOfferingDescription.getText() == null
                ? "" : edtOfferingDescription.getText().toString().trim();

        String price = edtOfferingPrice.getText() == null
                ? "" : edtOfferingPrice.getText().toString().trim();

        if (name.isEmpty()) {
            edtOfferingName.setError(
                    "Enter a product or service name");
            edtOfferingName.requestFocus();
            return;
        }

        if (!price.isEmpty()) {
            try {
                double amount = Double.parseDouble(price);

                if (!Double.isFinite(amount) || amount < 0) {
                    edtOfferingPrice.setError(
                            "Enter a valid non-negative price");
                    return;
                }
            } catch (NumberFormatException e) {
                edtOfferingPrice.setError("Enter a valid price");
                return;
            }
        }

        for (Offering item : offerings) {
            if (item.name.equalsIgnoreCase(name)) {
                edtOfferingName.setError(
                        "This offering has already been added");
                return;
            }
        }

        offerings.add(new Offering(name, description, price));

        edtOfferingName.setText("");
        edtOfferingDescription.setText("");
        edtOfferingPrice.setText("");

        renderOfferings();
    }

    private void renderOfferings() {
        layoutOfferings.removeAllViews();

        if (offerings.isEmpty()) {
            addEmptyMessage(layoutOfferings,
                    "No products or services added yet.");
            return;
        }

        for (int i = 0; i < offerings.size(); i++) {
            final Offering item = offerings.get(i);

            String detail = item.description.isEmpty()
                    ? "No description"
                    : item.description;

            if (!item.price.isEmpty()) {
                detail += "  •  R" + item.price;
            } else {
                detail += "  •  Price on request";
            }

            addEntryRow(
                    layoutOfferings,
                    item.name,
                    detail,
                    () -> {
                        offerings.remove(item);
                        renderOfferings();
                    }
            );
        }


    }

    private void addEntryRow(
            LinearLayout container,
            String title,
            String subtitle,
            Runnable onRemove) {

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(4, 10, 4, 10);

        LinearLayout details = new LinearLayout(this);
        details.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams detailsParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                );

        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextColor(Color.rgb(17, 66, 36));
        titleView.setTextSize(14);
        titleView.setTypeface(null, Typeface.BOLD);

        TextView subtitleView = new TextView(this);
        subtitleView.setText(subtitle);
        subtitleView.setTextColor(Color.rgb(110, 110, 110));
        subtitleView.setTextSize(12);
        subtitleView.setPadding(0, 4, 0, 0);

        details.addView(titleView);
        details.addView(subtitleView);

        MaterialButton removeButton = new MaterialButton(
                this, null,
                com.google.android.material.R.attr.materialButtonOutlinedStyle
        );

        removeButton.setText("Remove");
        removeButton.setTextSize(10);
        removeButton.setMinWidth(0);
        removeButton.setMinimumWidth(0);
        removeButton.setInsetTop(0);
        removeButton.setInsetBottom(0);
        removeButton.setPadding(8, 0, 8, 0);
        removeButton.setTextColor(Color.rgb(180, 35, 24));
        removeButton.setStrokeColor(
                android.content.res.ColorStateList.valueOf(
                        Color.rgb(235, 210, 210)));

        removeButton.setOnClickListener(v -> onRemove.run());

        row.addView(details, detailsParams);

        LinearLayout.LayoutParams buttonParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        40
                );

        buttonParams.setMarginStart(8);
        row.addView(removeButton, buttonParams);

        container.addView(row);

        View divider = new View(this);
        divider.setBackgroundColor(Color.rgb(230, 234, 230));

        container.addView(divider, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1));
    }

    private void addEmptyMessage(
            LinearLayout container, String message) {

        TextView empty = new TextView(this);
        empty.setText(message);
        empty.setTextColor(Color.rgb(125, 125, 125));
        empty.setTextSize(12);
        empty.setGravity(Gravity.CENTER);
        empty.setPadding(8, 18, 8, 18);

        container.addView(empty);
    }
}