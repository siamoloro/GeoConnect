package com.example.geoconnect;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class FreelancerDashboardActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private TextView tvGreeting, tvSubtitle, tvNewCount, tvAcceptedCount, tvRating;
    private TextView tvInitials, tvCustomerName, tvServiceDetails, tvPrice;
    private View cardRequest;
    private View[] upCards;
    private TextView[] upInitials, upTitle, upSub;
    private String activeBookingId = null;
    private SwitchCompat swAvailable;
    private boolean loadingSwitch = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_freelancer_dashboard);
        FreelancerUi.applyInsets(findViewById(R.id.main));
        FreelancerUi.setupNav(this, "Home");

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        tvGreeting = findViewById(R.id.tvGreeting);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        tvNewCount = findViewById(R.id.tvNewCount);
        tvAcceptedCount = findViewById(R.id.tvAcceptedCount);
        tvRating = findViewById(R.id.tvRating);
        cardRequest = findViewById(R.id.cardRequest);
        tvInitials = findViewById(R.id.tvInitials);
        tvCustomerName = findViewById(R.id.tvCustomerName);
        tvServiceDetails = findViewById(R.id.tvServiceDetails);
        tvPrice = findViewById(R.id.tvPrice);

        upCards = new View[]{findViewById(R.id.cardUp1), findViewById(R.id.cardUp2)};
        upInitials = new TextView[]{findViewById(R.id.tvUp1Initials), findViewById(R.id.tvUp2Initials)};
        upTitle = new TextView[]{findViewById(R.id.tvUp1Title), findViewById(R.id.tvUp2Title)};
        upSub = new TextView[]{findViewById(R.id.tvUp1Sub), findViewById(R.id.tvUp2Sub)};

        findViewById(R.id.tvAlerts).setOnClickListener(v ->
                startActivity(new Intent(this, NotificationsActivity.class)));
        findViewById(R.id.tvSeeAll).setOnClickListener(v ->
                startActivity(new Intent(this, BookingRequestsActivity.class)));

        swAvailable = findViewById(R.id.swAvailable);
        swAvailable.setOnCheckedChangeListener((button, isChecked) -> {
            if (!loadingSwitch) updateAvailabilityInFirestore(isChecked);
        });

        findViewById(R.id.btnAccept1).setOnClickListener(v -> {
            if (activeBookingId != null) BookingStatusHelper.update(this, activeBookingId, "Confirmed", this::loadBookings);
            else Toast.makeText(this, "No active request to accept", Toast.LENGTH_SHORT).show();
        });
        findViewById(R.id.btnDecline1).setOnClickListener(v -> {
            if (activeBookingId != null) BookingStatusHelper.update(this, activeBookingId, "Rejected", this::loadBookings);
            else Toast.makeText(this, "No active request to decline", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAvailability();
        loadProfileHeader();
        loadBookings();
    }

    private String str(DocumentSnapshot d, String field) {
        Object o = d.get(field);
        return o == null ? "" : o.toString();
    }

    private String initialsOf(String name) {
        if (name == null || name.trim().isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        String s = parts[0].substring(0, 1);
        if (parts.length > 1) s += parts[parts.length - 1].substring(0, 1);
        return s.toUpperCase();
    }

    private void loadProfileHeader() {
        if (mAuth.getCurrentUser() == null) return;
        db.collection("freelancers")
                .whereEqualTo("userID", mAuth.getCurrentUser().getUid())
                .limit(1).get()
                .addOnSuccessListener(snap -> {
                    if (snap.isEmpty()) return;
                    DocumentSnapshot d = snap.getDocuments().get(0);

                    String name = str(d, "fullName");
                    if (name.isEmpty()) name = str(d, "name");
                    String first = name.isEmpty() ? "" : name.split("\\s+")[0];
                    tvGreeting.setText(first.isEmpty() ? "Hello" : "Hello, " + first);

                    String category = str(d, "category");
                    if (category.isEmpty()) category = str(d, "skills");
                    tvSubtitle.setText(category);

                    Double rating = d.getDouble("rating");
                    tvRating.setText(rating != null
                            ? String.format(java.util.Locale.getDefault(), "%.1f", rating) : "—");
                });
    }

    private void loadAvailability() {
        if (mAuth.getCurrentUser() == null) return;
        db.collection("users").document(mAuth.getCurrentUser().getUid()).get()
                .addOnSuccessListener(d -> {
                    if (d.exists() && d.contains("available")) {
                        loadingSwitch = true;
                        swAvailable.setChecked(Boolean.TRUE.equals(d.getBoolean("available")));
                        loadingSwitch = false;
                    }
                });
    }

    private void updateAvailabilityInFirestore(boolean isAvailable) {
        if (mAuth.getCurrentUser() == null) return;
        db.collection("users").document(mAuth.getCurrentUser().getUid())
                .update("available", isAvailable)
                .addOnSuccessListener(a -> Toast.makeText(this,
                        isAvailable ? "You are now available for bookings" : "You are now unavailable",
                        Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this,
                        "Failed to update availability: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private String whenOf(DocumentSnapshot d) {
        com.google.firebase.Timestamp t = d.getTimestamp("bookingDate");
        if (t == null) return "";
        return java.text.DateFormat.getDateTimeInstance(
                java.text.DateFormat.MEDIUM, java.text.DateFormat.SHORT).format(t.toDate());
    }

    private void loadBookings() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        // Bookings store the freelancer's document ID in "providerId"
        db.collection("freelancers")
                .whereEqualTo("userID", uid)
                .limit(1).get()
                .addOnSuccessListener(fs -> {
                    String providerId = fs.isEmpty() ? uid : fs.getDocuments().get(0).getId();
                    db.collection("booking")
                            .whereEqualTo("providerId", providerId)
                            .get()
                            .addOnSuccessListener(snap -> showBookings(snap.getDocuments()))
                            .addOnFailureListener(e -> Toast.makeText(this,
                                    "Failed to load bookings: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(this,
                        "Failed to load profile: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showBookings(List<DocumentSnapshot> docs) {
        List<DocumentSnapshot> pending = new ArrayList<>();
        List<DocumentSnapshot> accepted = new ArrayList<>();
        for (DocumentSnapshot d : docs) {
            String status = str(d, "status");
            if (status.isEmpty() || "Pending".equalsIgnoreCase(status)) pending.add(d);
            else if ("Confirmed".equalsIgnoreCase(status) || "Accepted".equalsIgnoreCase(status)) accepted.add(d);
        }

        tvNewCount.setText(String.valueOf(pending.size()));
        tvAcceptedCount.setText(String.valueOf(accepted.size()));

        if (pending.isEmpty()) {
            activeBookingId = null;
            cardRequest.setVisibility(View.GONE);
        } else {
            DocumentSnapshot d = pending.get(0);
            activeBookingId = d.getId();
            String customer = str(d, "customerName");
            if (customer.isEmpty()) customer = "Customer";
            tvInitials.setText(initialsOf(customer));
            tvCustomerName.setText(customer);
            tvServiceDetails.setText(str(d, "service") + " · " + whenOf(d));
            tvPrice.setText(str(d, "price"));
            cardRequest.setVisibility(View.VISIBLE);
        }

        for (int i = 0; i < upCards.length; i++) {
            if (i < accepted.size()) {
                DocumentSnapshot d = accepted.get(i);
                String customer = str(d, "customerName");
                if (customer.isEmpty()) customer = "Customer";
                upInitials[i].setText(initialsOf(customer));
                upTitle[i].setText(customer + " · " + str(d, "service"));
                upSub[i].setText(whenOf(d) + " · " + str(d, "location"));
                upCards[i].setVisibility(View.VISIBLE);
            } else {
                upCards[i].setVisibility(View.GONE);
            }
        }
    }
}