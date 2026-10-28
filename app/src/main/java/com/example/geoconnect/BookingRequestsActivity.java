package com.example.geoconnect;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

public class BookingRequestsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private BookingAdapter bookingAdapter;
    private List<Booking> bookingList;
    private String currentStatus = "Pending";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_booking_requests);
        FreelancerUi.applyInsets(findViewById(R.id.main));
        FreelancerUi.setupNav(this, "Requests");

        db = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.recyclerViewBookings);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        bookingList = new ArrayList<>();
        bookingAdapter = new BookingAdapter(bookingList, new BookingAdapter.OnBookingActionListener() {
            @Override
            public void onAccept(Booking booking) {
                updateBookingStatus(booking.getId(), "Accepted");
            }

            @Override
            public void onDecline(Booking booking) {
                updateBookingStatus(booking.getId(), "Declined");
            }
        });
        recyclerView.setAdapter(bookingAdapter);

        TextView tabPending = findViewById(R.id.tabPending);
        TextView tabAccepted = findViewById(R.id.tabAccepted);
        TextView tabCompleted = findViewById(R.id.tabCompleted);
        TextView[] tabs = {tabPending, tabAccepted, tabCompleted};

        for (TextView tab : tabs) {
            tab.setOnClickListener(v -> {
                selectTab(tab, tabs);
                currentStatus = tab.getText().toString();
                loadBookingsFromFirestore(currentStatus);
            });
        }

        loadBookingsFromFirestore("Pending");
    }

    private void loadBookingsFromFirestore(String status) {
        if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("freelancers")
                .whereEqualTo("userID", currentUserId)
                .limit(1)
                .get()
                .addOnSuccessListener(freelancerSnapshots -> {
                    if (!freelancerSnapshots.isEmpty()) {
                        String freelancerId = freelancerSnapshots.getDocuments().get(0).getId();

                        // Field names must match what BookingsActivity writes (providerID, service, bookingDate)
                        db.collection("bookings")
                                .whereEqualTo("status", status)
                                .whereEqualTo("providerID", freelancerId)
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    bookingList.clear();
                                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                        String customerName = document.getString("customerName");
                                        if (customerName == null || customerName.isEmpty()) customerName = "Customer";
                                        Timestamp when = document.getTimestamp("bookingDate");
                                        Booking booking = new Booking(
                                                document.getId(),
                                                customerName,
                                                document.getString("service"),
                                                "",
                                                when != null ? DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(when.toDate()) : "",
                                                document.getString("location"),
                                                document.getString("status"),
                                                customerName.substring(0, 1).toUpperCase()
                                        );
                                        bookingList.add(booking);
                                    }
                                    bookingAdapter.notifyDataSetChanged();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to load bookings: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to identify freelancer profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateBookingStatus(String docId, String newStatus) {
        db.collection("bookings").document(docId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Booking " + newStatus.toLowerCase(), Toast.LENGTH_SHORT).show();
                    loadBookingsFromFirestore(currentStatus);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error updating status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void selectTab(TextView selected, TextView[] all) {
        for (TextView t : all) {
            boolean on = t == selected;
            t.setBackgroundResource(on ? R.drawable.bg_chip_active : R.drawable.bg_chip);
            t.setTextColor(ContextCompat.getColor(this, on ? R.color.glitter : R.color.forest_green));
        }
    }
}