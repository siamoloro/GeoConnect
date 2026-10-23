package com.example.geoconnect;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Date;

public class BusinessDashboardActivity extends AppCompatActivity {

    private ImageView btnSettings, btnEditProfile, btnEditHours, btnEditLocation, btnEditOfferings, btnNotifications;
    private TextView txtBusinessName, txtCategory, txtAbout, txtLocation, txtStatus, txtRating, txtMemberSince;

    // Firebase instances
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_business_dashboard);
        FreelancerUi.applyInsets(findViewById(android.R.id.content));

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize Views
        btnSettings = findViewById(R.id.btnSettings);
        btnNotifications = findViewById(R.id.btnNotifications);
        txtBusinessName = findViewById(R.id.txtBusinessName);
        txtCategory = findViewById(R.id.txtCategory);
        txtAbout = findViewById(R.id.txtAbout);
        txtLocation = findViewById(R.id.txtLocation);

        // Additional metric/status views present in your dashboard XML
        txtStatus = findViewById(R.id.txtStatus);
        txtRating = findViewById(R.id.txtRating);
        txtMemberSince = findViewById(R.id.txtMemberSince);

        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnEditHours = findViewById(R.id.btnEditHours);
        btnEditLocation = findViewById(R.id.btnEditLocation);
        btnEditOfferings = findViewById(R.id.btnEditOfferings);

        // Fetch and populate data from Firebase DB for the currently logged-in user
        loadBusinessDataFromFirestore();

        // Setup Button Click Listeners & Navigation
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(BusinessDashboardActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        btnNotifications.setOnClickListener(v -> {
            Intent intent = new Intent(BusinessDashboardActivity.this, NotificationsActivity.class);
            startActivity(intent);
        });

        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(BusinessDashboardActivity.this, BusinessEditProfileActivity.class);
            startActivity(intent);
        });

        btnEditLocation.setOnClickListener(v -> {
            Intent intent = new Intent(BusinessDashboardActivity.this, LocationPreferencesActivity.class);
            startActivity(intent);
        });

        btnEditHours.setOnClickListener(v -> {
            Intent intent = new Intent(BusinessDashboardActivity.this, BusinessOperatingHoursActivity.class);
            startActivity(intent);
        });

        btnEditOfferings.setOnClickListener(v -> {
            Intent intent = new Intent(BusinessDashboardActivity.this, BusinessOfferingsActivity.class);
            startActivity(intent);
        });
    }

    private void loadBusinessDataFromFirestore() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "No active user session found. Please sign in again.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(BusinessDashboardActivity.this, SigninActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        String userId = currentUser.getUid();

        // 1. Fetch business information from the 'businesses' collection
        db.collection("businesses").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String category = documentSnapshot.getString("category");
                        String description = documentSnapshot.getString("description");
                        String address = documentSnapshot.getString("address");
                        String approvalStatus = documentSnapshot.getString("approvalStatus");

                        // Populate text fields safely
                        if (name != null) {
                            txtBusinessName.setText(name);
                        }
                        if (category != null) {
                            txtCategory.setText(category);
                        }
                        if (description != null && !description.trim().isEmpty()) {
                            txtAbout.setText(description);
                        } else {
                            txtAbout.setText("No description provided yet.");
                        }
                        if (address != null && !address.trim().isEmpty()) {
                            txtLocation.setText(address);
                        } else {
                            txtLocation.setText("Location not set.");
                        }

                        // Handle Status field mapping
                        if (approvalStatus != null) {
                            if (approvalStatus.equalsIgnoreCase("approved")) {
                                txtStatus.setText("Verified");
                            } else {
                                txtStatus.setText(capitalizeFirstLetter(approvalStatus));
                            }
                        } else {
                            txtStatus.setText("Pending");
                        }

                        // Handle Rating Calculation safely (preventing division by zero)
                        Long totalRatings = documentSnapshot.getLong("totalRatings");
                        Long totalReviews = documentSnapshot.getLong("totalReviews");

                        if (totalReviews != null && totalReviews > 0 && totalRatings != null) {
                            double averageRating = (double) totalRatings / totalReviews;
                            txtRating.setText(String.format("%.1f", averageRating));
                        } else {
                            txtRating.setText("0.0"); // Default if no reviews exist yet
                        }

                    } else {
                        Toast.makeText(this, "Business profile data not found in database.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load profile: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        // 2. Fetch account creation details from the 'users' collection to calculate 'Member Since'
        db.collection("users").document(userId).get()
                .addOnSuccessListener(userDoc -> {
                    if (userDoc.exists()) {
                        Timestamp createdAtTimestamp = userDoc.getTimestamp("created_at");
                        if (createdAtTimestamp != null) {
                            Date creationDate = createdAtTimestamp.toDate();
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(creationDate);
                            int creationYear = calendar.get(Calendar.YEAR);

                            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                            int yearsDifference = currentYear - creationYear;

                            // If 0 or negative (created this year), default to 1 yr
                            if (yearsDifference <= 0) {
                                txtMemberSince.setText("1 yr");
                            } else {
                                txtMemberSince.setText(yearsDifference + (yearsDifference == 1 ? " yr" : " yrs"));
                            }
                        } else {
                            txtMemberSince.setText("1 yr");
                        }
                    }
                })
                .addOnFailureListener(e -> txtMemberSince.setText("1 yr"));
    }

    private String capitalizeFirstLetter(String text) {
        if (text == null || text.isEmpty()) return "";
        return text.substring(0, 1).toUpperCase() + text.toLowerCase().substring(1);
    }
}