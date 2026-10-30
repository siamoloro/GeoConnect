package com.example.geoconnect;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookingsActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private TextView providerName;
    private TextView providerCategory;
    private TextView servicePrice;
    private TextView backButton;

    private Spinner serviceSpinner;
    private DatePicker bookingDatePicker;
    private TimePicker bookingTimePicker;
    private EditText serviceLocationInput;
    private EditText bookingNotesInput;
    private Button submitBookingButton;

    private String providerId;
    private String providerType;
    private String selectedProviderName;
    private String selectedProviderCategory;

    private boolean providerLoaded = false;
    private boolean servicesLoaded = false;

    private final List<String> serviceNames =
            new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_bookings);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Connects Java to existing XML views
        providerName = findViewById(R.id.providerName);
        providerCategory = findViewById(R.id.providerCategory);
        servicePrice = findViewById(R.id.servicePrice);
        backButton = findViewById(R.id.backButton);

        serviceSpinner = findViewById(R.id.serviceSpinner);

        bookingDatePicker =
                findViewById(R.id.bookingDatePicker);

        bookingTimePicker =
                findViewById(R.id.bookingTimePicker);

        serviceLocationInput =
                findViewById(R.id.serviceLocationInput);

        bookingNotesInput =
                findViewById(R.id.bookingNotesInput);

        submitBookingButton =
                findViewById(R.id.submitBookingButton);

        // Back button
        backButton.setOnClickListener(v -> finish());

        // Prevents choosing previous dates
        bookingDatePicker.setMinDate(
                System.currentTimeMillis() - 1000
        );

        // Gets provider information from DetailsActivity
        Intent intent = getIntent();

        providerId = intent.getStringExtra("providerId");
        providerType = intent.getStringExtra("providerType");

        submitBookingButton.setEnabled(false);

        if (providerId == null ||
                providerId.trim().isEmpty() ||
                (!"Business".equalsIgnoreCase(providerType)
                        && !"Freelancer".equalsIgnoreCase(providerType))) {

            Toast.makeText(
                    this,
                    "Provider information is missing",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        // Loads selected provider from Firebase
        loadProvider();

        // Booking request button
        submitBookingButton.setOnClickListener(v ->
                submitBooking()
        );
    }

    private void loadProvider() {

        String collection =
                "Business".equalsIgnoreCase(providerType)
                        ? "businesses"
                        : "freelancers";

        db.collection(collection)
                .document(providerId)
                .get()
                .addOnSuccessListener(document -> {

                    if (!document.exists()) {

                        Toast.makeText(
                                this,
                                "Provider not found",
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    selectedProviderName = firstString(
                            document,
                            "name",
                            "businessName",
                            "fullname"
                    );

                    selectedProviderCategory = firstString(
                            document,
                            "category",
                            "serviceCategory",
                            "service"
                    );

                    providerName.setText(selectedProviderName);
                    providerCategory.setText(
                            selectedProviderCategory
                    );

                    providerLoaded = true;

                    loadServices(document);
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "Unable to load provider: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private String firstString(
            DocumentSnapshot document,
            String... fields) {

        for (String field : fields) {

            Object value = document.get(field);

            if (value instanceof String &&
                    !((String) value).trim().isEmpty()) {

                return ((String) value).trim();
            }
        }

        return "";
    }

    private void loadServices(DocumentSnapshot document) {

        serviceNames.clear();

        // 1. Check whether the provider has services
        Object services = document.get("services");

        if (services instanceof List<?>) {

            for (Object item : (List<?>) services) {

                if (item instanceof String &&
                        !((String) item).trim().isEmpty()) {

                    serviceNames.add(((String) item).trim());
                }
            }
        }

        // 2. If no services exist, use the provider's category
        // Works for BOTH businesses and freelancers
        if (serviceNames.isEmpty()) {

            String category = firstString(
                    document,
                    "category",
                    "serviceCategory",
                    "service"
            );

            if (!category.isEmpty()) {
                serviceNames.add(category);
            }
        }

        // 3. Check if a valid booking option exists
        servicesLoaded = !serviceNames.isEmpty();

        if (!servicesLoaded) {
            serviceNames.add("No services available");
        }

        // 4. Display services or category
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                serviceNames
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        serviceSpinner.setAdapter(adapter);

        // 5. Display price information
        servicePrice.setText(
                "Price will be confirmed by the provider"
        );

        // 6. Enable booking if a valid option exists
        submitBookingButton.setEnabled(
                providerLoaded && servicesLoaded
        );
    }

    private void submitBooking() {

        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {

            Toast.makeText(
                    this,
                    "Please log in first",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (!providerLoaded || !servicesLoaded) {

            Toast.makeText(
                    this,
                    "No service is available for booking",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String selectedService =
                serviceSpinner.getSelectedItem().toString();

        String location =
                serviceLocationInput.getText()
                        .toString().trim();

        String notes =
                bookingNotesInput.getText()
                        .toString().trim();

        // Validate service location
        if (location.isEmpty()) {

            serviceLocationInput.setError(
                    "Please enter a service location"
            );

            serviceLocationInput.requestFocus();
            return;
        }

        // Get selected date and time
        Calendar calendar = Calendar.getInstance();

        calendar.set(
                bookingDatePicker.getYear(),
                bookingDatePicker.getMonth(),
                bookingDatePicker.getDayOfMonth(),
                bookingTimePicker.getHour(),
                bookingTimePicker.getMinute(),
                0
        );

        calendar.set(Calendar.MILLISECOND, 0);

        // Prevent booking in the past
        if (calendar.getTimeInMillis()
                <= System.currentTimeMillis()) {

            Toast.makeText(
                    this,
                    "Please choose a future date and time",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        Timestamp bookingDate =
                new Timestamp(calendar.getTime());

        // Booking details to save in Firestore
        Map<String, Object> booking = new HashMap<>();

        booking.put(
                "customerID",
                currentUser.getUid()
        );

        booking.put("providerId", providerId);
        booking.put("providerType", providerType);

        booking.put(
                "providerName",
                selectedProviderName
        );

        booking.put(
                "providerCategory",
                selectedProviderCategory
        );

        booking.put("service", selectedService);
        booking.put("bookingDate", bookingDate);
        booking.put("location", location);
        booking.put("notes", notes);

        // Provider must confirm the request
        booking.put("status", "Pending");

        booking.put(
                "createdAt",
                FieldValue.serverTimestamp()
        );

        // Prevent duplicate submissions
        submitBookingButton.setEnabled(false);

        db.collection("booking")
                .add(booking)
                .addOnSuccessListener(documentReference -> {

                    Toast.makeText(
                            this,
                            "Booking request submitted successfully!",
                            Toast.LENGTH_LONG
                    ).show();

                    // Open My Bookings page
                    Intent myBookingsIntent = new Intent(
                            BookingsActivity.this,
                            MyBookingActivity.class
                    );

                    startActivity(myBookingsIntent);
                    finish();
                })
                .addOnFailureListener(e -> {

                    submitBookingButton.setEnabled(true);

                    Toast.makeText(
                            this,
                            "Booking failed: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }
}