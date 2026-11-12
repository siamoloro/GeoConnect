
package com.example.geoconnect;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyBookingActivity extends AppCompatActivity {

    // FIREBASE
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private ListenerRegistration bookingListener;

    // XML COMPONENTS
    private RadioGroup bookingFilters;
    private LinearLayout bookingsContainer;
    private LinearLayout emptyState;
    private TextView emptyMessage;
    private ImageView backButton;

    // BOOKING DATA
    private final List<DocumentSnapshot> allBookings =
            new ArrayList<>();

    private String selectedStatus = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_my_booking);

        Toast.makeText(
                this,
                "My Bookings Activity opened",
                Toast.LENGTH_LONG
        ).show();

        // INITIALIZE FIREBASE
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // CONNECT XML COMPONENTS
        backButton = findViewById(R.id.backButton);
        bookingFilters = findViewById(R.id.bookingFilters);
        bookingsContainer = findViewById(R.id.bookingsContainer);
        emptyState = findViewById(R.id.emptyState);
        emptyMessage = findViewById(R.id.emptyMessage);

        // BACK BUTTON
        backButton.setOnClickListener(v -> finish());

        // BOOKING FILTERS
        bookingFilters.setOnCheckedChangeListener(
                (group, checkedId) -> {

                    if (checkedId == R.id.filterPending) {
                        selectedStatus = "Pending";

                    } else if (checkedId == R.id.filterConfirmed) {
                        selectedStatus = "Confirmed";

                    } else if (checkedId == R.id.filterCompleted) {
                        selectedStatus = "Completed";

                    } else if (checkedId == R.id.filterRejected) {
                        selectedStatus = "Rejected";

                    } else {
                        selectedStatus = "All";
                    }

                    showBookings(selectedStatus);
                }
        );

        // LOAD REAL BOOKINGS
        loadBookings();
    }

    // RETRIEVE LOGGED-IN CUSTOMER'S BOOKINGS
    private void loadBookings() {

        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user == null) {
            showMessage("Please log in to view your bookings.");
            return;
        }

        String customerId = user.getUid();

        showMessage("Loading your bookings...");

        bookingListener = firestore.collection("booking")
                .whereEqualTo("customerID", customerId)
                .addSnapshotListener((snapshot, error) -> {

                    if (error != null) {

                        showMessage(
                                "Unable to load bookings: "
                                        + error.getMessage()
                        );
                        return;
                    }

                    allBookings.clear();

                    if (snapshot != null) {
                        allBookings.addAll(snapshot.getDocuments());
                    }

                    showBookings(selectedStatus);
                });
    }

    // FILTER AND DISPLAY BOOKINGS
    private void showBookings(String status) {

        bookingsContainer.removeAllViews();

        int visibleBookings = 0;

        for (DocumentSnapshot booking : allBookings) {

            String bookingStatus =
                    booking.getString("status");

            if (bookingStatus == null) {
                bookingStatus = "Pending";
            }

            if (!status.equals("All")
                    && !bookingStatus.equalsIgnoreCase(status)) {
                continue;
            }

            addBookingCard(booking, bookingStatus);
            visibleBookings++;
        }

        if (visibleBookings == 0) {

            if (status.equals("All")) {
                showMessage("Your service bookings will appear here.");
            } else {
                showMessage(
                        "No " + status.toLowerCase(Locale.ROOT)
                                + " bookings to display."
                );
            }

        } else {
            emptyState.setVisibility(View.GONE);
        }
    }


    private void addBookingCard(
            DocumentSnapshot booking,
            String status) {

        // GET REAL BOOKING DATA
        String providerName = booking.getString("providerName");
        String service = booking.getString("service");
        String providerType = booking.getString("providerType");

        if (providerName == null || providerName.trim().isEmpty()) {
            providerName = "Provider details unavailable";
        }

        if (service == null || service.trim().isEmpty()) {
            service = "Service not specified";
        }

        if (providerType == null) {
            providerType = "";
        }

        // GET BOOKING DATE
        String bookingDateText = "";

        com.google.firebase.Timestamp timestamp =
                booking.getTimestamp("bookingDate");

        if (timestamp != null) {
            Date date = timestamp.toDate();

            bookingDateText = DateFormat.getDateTimeInstance(
                    DateFormat.MEDIUM,
                    DateFormat.SHORT
            ).format(date);
        }

        // MAIN CARD
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));

        GradientDrawable cardBackground = new GradientDrawable();
        cardBackground.setColor(Color.WHITE);
        cardBackground.setCornerRadius(dp(16));
        cardBackground.setStroke(
                dp(1),
                Color.parseColor("#D5E9DE")
        );

        card.setBackground(cardBackground);
        card.setElevation(dp(3));

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.bottomMargin = dp(16);
        card.setLayoutParams(cardParams);

        // HEADER ROW
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        TextView typeText = new TextView(this);
        typeText.setText(providerType.toUpperCase(Locale.ROOT));
        typeText.setTextSize(12);
        typeText.setTypeface(null, Typeface.BOLD);
        typeText.setTextColor(Color.parseColor("#176B50"));

        header.addView(
                typeText,
                new LinearLayout.LayoutParams(0,
                        LinearLayout.LayoutParams.WRAP_CONTENT, 1)
        );

        // STATUS BADGE
        TextView statusBadge = new TextView(this);
        statusBadge.setText(status);
        statusBadge.setTextSize(12);
        statusBadge.setTypeface(null, Typeface.BOLD);
        statusBadge.setGravity(Gravity.CENTER);
        statusBadge.setPadding(dp(12), dp(7), dp(12), dp(7));

        int badgeBackground;
        int badgeTextColor;

        switch (status.toLowerCase(Locale.ROOT)) {
            case "confirmed":
                badgeBackground = Color.parseColor("#DCEBFF");
                badgeTextColor = Color.parseColor("#245AA5");
                break;

            case "completed":
                badgeBackground = Color.parseColor("#DDF3E5");
                badgeTextColor = Color.parseColor("#176B50");
                break;

            case "rejected":
            case "cancelled":
                badgeBackground = Color.parseColor("#FCE3E3");
                badgeTextColor = Color.parseColor("#B42318");
                break;

            default:
                badgeBackground = Color.parseColor("#FFF0D2");
                badgeTextColor = Color.parseColor("#946200");
                break;
        }

        GradientDrawable badgeShape = new GradientDrawable();
        badgeShape.setColor(badgeBackground);
        badgeShape.setCornerRadius(dp(20));

        statusBadge.setBackground(badgeShape);
        statusBadge.setTextColor(badgeTextColor);

        header.addView(statusBadge);
        card.addView(header);

        // PROVIDER NAME
        TextView nameText = new TextView(this);
        nameText.setText(providerName);
        nameText.setTextSize(18);
        nameText.setTypeface(null, Typeface.BOLD);
        nameText.setTextColor(Color.parseColor("#173D31"));

        LinearLayout.LayoutParams nameParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        nameParams.topMargin = dp(12);
        card.addView(nameText, nameParams);

        // SERVICE
        TextView serviceText = new TextView(this);
        serviceText.setText("Service: " + service);
        serviceText.setTextSize(14);
        serviceText.setTextColor(Color.parseColor("#566A60"));

        LinearLayout.LayoutParams serviceParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        serviceParams.topMargin = dp(8);
        card.addView(serviceText, serviceParams);

        // DATE AND TIME
        if (!bookingDateText.isEmpty()) {

            TextView dateText = new TextView(this);
            dateText.setText("Date: " + bookingDateText);
            dateText.setTextSize(14);
            dateText.setTextColor(Color.parseColor("#566A60"));

            LinearLayout.LayoutParams dateParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            dateParams.topMargin = dp(8);
            card.addView(dateText, dateParams);
        }

        // REVIEW BUTTON FOR COMPLETED BOOKINGS
        if ("Completed".equalsIgnoreCase(status)) {

            Button reviewButton = new Button(this);
            reviewButton.setText("Write a Review");
            reviewButton.setTransformationMethod(null);
            reviewButton.setTextColor(Color.WHITE);
            reviewButton.setTextSize(14);

            GradientDrawable buttonBackground = new GradientDrawable();
            buttonBackground.setColor(Color.parseColor("#176B50"));
            buttonBackground.setCornerRadius(dp(10));

            reviewButton.setBackground(buttonBackground);

            LinearLayout.LayoutParams buttonParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            dp(48)
                    );

            buttonParams.topMargin = dp(16);

            card.addView(reviewButton, buttonParams);

            // KEEP EXISTING REVIEW NAVIGATION
            reviewButton.setOnClickListener(v -> {

                String bookingId = booking.getId();
                String providerId = booking.getString("providerId");
                String providerTypeValue = booking.getString("providerType");
                String providerNameValue = booking.getString("providerName");
                String providerCategoryValue =
                        booking.getString("providerCategory");

                if (providerId == null || providerTypeValue == null) {

                    Toast.makeText(
                            this,
                            "Provider information missing",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }

                Intent intent = new Intent(
                        MyBookingActivity.this,
                        ReviewActivity.class
                );

                intent.putExtra("bookingId", bookingId);
                intent.putExtra("providerId", providerId);
                intent.putExtra("providerType", providerTypeValue);
                intent.putExtra("providerName", providerNameValue);
                intent.putExtra("providerCategory", providerCategoryValue);
                intent.putExtra("bookingStatus", status);

                startActivity(intent);
            });
        }

        bookingsContainer.addView(card);
    }
    private int dp(int value) {
        return (int) (
                value * getResources().getDisplayMetrics().density + 0.5f
        );
    }
    // DISPLAY EMPTY, LOADING OR ERROR MESSAGE
    private void showMessage(String message) {

        bookingsContainer.removeAllViews();

        emptyState.setVisibility(View.VISIBLE);

        emptyMessage.setText(message);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // STOP LISTENING WHEN PAGE IS DESTROYED
        if (bookingListener != null) {
            bookingListener.remove();
        }
    }

}
