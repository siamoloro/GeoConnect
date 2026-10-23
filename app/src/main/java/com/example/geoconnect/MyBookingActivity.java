
package com.example.geoconnect;

import android.os.Bundle;
import android.view.View;
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

    // CREATE BOOKING CARD USING REAL FIRESTORE DATA
    private void addBookingCard(
            DocumentSnapshot booking,
            String status) {

        String providerName =
                booking.getString("providerName");

        String service =
                booking.getString("service");

        String providerType =
                booking.getString("providerType");

        if (providerName == null || providerName.isEmpty()) {
            providerName = "Provider details unavailable";
        }

        if (service == null || service.isEmpty()) {
            service = "Service not specified";
        }

        if (providerType == null) {
            providerType = "";
        }

        String bookingDateText = "";

        com.google.firebase.Timestamp timestamp =
                booking.getTimestamp("bookingDate");

        if (timestamp != null) {

            Date date = timestamp.toDate();

            bookingDateText = DateFormat
                    .getDateTimeInstance(
                            DateFormat.MEDIUM,
                            DateFormat.SHORT
                    )
                    .format(date);
        }

        // CREATE BOOKING CARD
        TextView bookingCard = new TextView(this);

        String cardText =
                providerName + "\n" +
                        service + "\n" +
                        (providerType.isEmpty()
                                ? ""
                                : providerType + "\n") +
                        "Status: " + status +
                        (bookingDateText.isEmpty()
                                ? ""
                                : "\nDate: " + bookingDateText);

        bookingCard.setText(cardText);
        bookingCard.setTextSize(16);
        bookingCard.setPadding(28, 24, 28, 24);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.bottomMargin = 16;

        bookingCard.setLayoutParams(params);

        bookingsContainer.addView(bookingCard);
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
