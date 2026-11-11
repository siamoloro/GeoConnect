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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

public class BookingRequestsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private BookingAdapter bookingAdapter;
    private List<Booking> bookingList;
    private String currentTab = "Pending";

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
                // The customer screens call an accepted booking "Confirmed"
                BookingStatusHelper.update(BookingRequestsActivity.this, booking.getId(), "Confirmed",
                        () -> loadBookings(currentTab));
            }

            @Override
            public void onDecline(Booking booking) {
                BookingStatusHelper.update(BookingRequestsActivity.this, booking.getId(), "Rejected",
                        () -> loadBookings(currentTab));
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
                currentTab = tab.getText().toString();
                loadBookings(currentTab);
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBookings(currentTab);
    }

    // Tab label shown to the freelancer -> status values stored in Firestore
    private boolean matchesTab(String tab, String status) {
        if (status == null) status = "Pending";
        switch (tab) {
            case "Accepted":
                return status.equalsIgnoreCase("Confirmed") || status.equalsIgnoreCase("Accepted");
            case "Completed":
                return status.equalsIgnoreCase("Completed");
            default:
                return status.equalsIgnoreCase("Pending");
        }
    }

    private void loadBookings(String tab) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Bookings store the freelancer's document ID in "providerId"
        db.collection("freelancers")
                .whereEqualTo("userID", uid)
                .limit(1)
                .get()
                .addOnSuccessListener(fs -> {
                    String providerId = fs.isEmpty() ? uid : fs.getDocuments().get(0).getId();

                    db.collection("booking")
                            .whereEqualTo("providerId", providerId)
                            .get()
                            .addOnSuccessListener(snap -> {
                                bookingList.clear();
                                for (DocumentSnapshot d : snap.getDocuments()) {
                                    String status = d.getString("status");
                                    if (!matchesTab(tab, status)) continue;

                                    String customerName = d.getString("customerName");
                                    if (customerName == null || customerName.isEmpty()) customerName = "Customer";

                                    Timestamp when = d.getTimestamp("bookingDate");
                                    String whenText = when != null
                                            ? DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                                            .format(when.toDate())
                                            : "";

                                    String price = d.getString("price");
                                    bookingList.add(new Booking(
                                            d.getId(),
                                            customerName,
                                            d.getString("service"),
                                            price != null ? price : "",
                                            whenText,
                                            d.getString("location"),
                                            status,
                                            customerName.substring(0, 1).toUpperCase()
                                    ));
                                }
                                bookingAdapter.notifyDataSetChanged();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this,
                                    "Failed to load bookings: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(this,
                        "Failed to identify freelancer profile: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void selectTab(TextView selected, TextView[] all) {
        for (TextView t : all) {
            boolean on = t == selected;
            t.setBackgroundResource(on ? R.drawable.bg_chip_active : R.drawable.bg_chip);
            t.setTextColor(ContextCompat.getColor(this, on ? R.color.glitter : R.color.forest_green));
        }
    }
}