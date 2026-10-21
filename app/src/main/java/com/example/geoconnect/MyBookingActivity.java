package com.example.geoconnect;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MyBookingActivity extends AppCompatActivity {

    private RadioGroup bookingFilters;
    private LinearLayout bookingsContainer;
    private LinearLayout emptyState;
    private TextView emptyMessage;
    private TextView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_booking);

        backButton = findViewById(R.id.backButton);
        bookingFilters = findViewById(R.id.bookingFilters);
        bookingsContainer = findViewById(R.id.bookingsContainer);
        emptyState = findViewById(R.id.emptyState);
        emptyMessage = findViewById(R.id.emptyMessage);

        backButton.setOnClickListener(v -> finish());

        bookingFilters.setOnCheckedChangeListener(
                (group, checkedId) -> {

                    String status = "All";

                    if (checkedId == R.id.filterPending) {
                        status = "Pending";
                    } else if (checkedId == R.id.filterConfirmed) {
                        status = "Confirmed";
                    } else if (checkedId == R.id.filterCompleted) {
                        status = "Completed";
                    } else if (checkedId == R.id.filterRejected) {
                        status = "Rejected";
                    }

                    showBookings(status);
                }
        );

        showBookings("All");
    }

    private void showBookings(String status) {

        // No real bookings are loaded until API integration.
        bookingsContainer.removeAllViews();

        emptyState.setVisibility(View.VISIBLE);

        if (status.equals("All")) {
            emptyMessage.setText(
                    "Your service bookings will appear here."
            );
        } else {
            emptyMessage.setText(
                    "No " + status.toLowerCase() + " bookings to display."
            );
        }
    }
}