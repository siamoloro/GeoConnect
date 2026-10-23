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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class FreelancerDashboardActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // UI references for the top request card
    private TextView txtCustomerName, txtServiceDetails, txtPriceTag;
    private String activeBookingId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_freelancer_dashboard);
        FreelancerUi.applyInsets(findViewById(R.id.main));
        FreelancerUi.setupNav(this, "Home");

        // Initialize Firebase instances
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.tvAlerts).setOnClickListener(v ->
                startActivity(new Intent(this, NotificationsActivity.class)));
        findViewById(R.id.tvSeeAll).setOnClickListener(v ->
                startActivity(new Intent(this, BookingRequestsActivity.class)));

        SwitchCompat swAvailable = findViewById(R.id.swAvailable);

        // Load availability status from the 'users' collection
        loadFreelancerAvailability(swAvailable);

        swAvailable.setOnCheckedChangeListener((button, isChecked) -> {
            updateAvailabilityInFirestore(isChecked);
        });

        // Load real-time pending request from Firestore 'bookings' collection
        loadLatestPendingRequest();

        // Dynamic Accept Button Action
        findViewById(R.id.btnAccept1).setOnClickListener(v -> {
            if (activeBookingId != null) {
                updateBookingStatus(activeBookingId, "Accepted");
            } else {
                Toast.makeText(this, "No active request to accept", Toast.LENGTH_SHORT).show();
            }
        });

        // Dynamic Decline Button Action
        findViewById(R.id.btnDecline1).setOnClickListener(v -> {
            if (activeBookingId != null) {
                updateBookingStatus(activeBookingId, "Declined");
            } else {
                Toast.makeText(this, "No active request to decline", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFreelancerAvailability(SwitchCompat swAvailable) {
        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();
            db.collection("users").document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists() && documentSnapshot.contains("available")) {
                            boolean isAvailable = Boolean.TRUE.equals(documentSnapshot.getBoolean("available"));
                            swAvailable.setChecked(isAvailable);
                        }
                    });
        }
    }

    private void updateAvailabilityInFirestore(boolean isAvailable) {
        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();
            db.collection("users").document(uid)
                    .update("available", isAvailable)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this,
                                isAvailable ? "You are now available for bookings" : "You are now unavailable",
                                Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to update availability: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void loadLatestPendingRequest() {
        if (mAuth.getCurrentUser() == null) return;
        // Only this freelancer's requests — without the providerID filter Accept/Decline hit anyone's booking
        db.collection("bookings")
                .whereEqualTo("providerID", mAuth.getCurrentUser().getUid())
                .whereEqualTo("status", "Pending")
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        QueryDocumentSnapshot document = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                        activeBookingId = document.getId(); // Dynamically capture the document/booking ID

                        String customerName = document.getString("customerName");
                        Toast.makeText(this, "Loaded request from: " + customerName, Toast.LENGTH_SHORT).show();
                    } else {
                        activeBookingId = null;
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load requests: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateBookingStatus(String docId, String newStatus) {
        db.collection("bookings").document(docId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Request " + newStatus.toLowerCase(), Toast.LENGTH_SHORT).show();
                    loadLatestPendingRequest(); // Refresh to pull the next pending request
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error updating request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}