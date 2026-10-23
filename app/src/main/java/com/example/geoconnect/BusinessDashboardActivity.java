package com.example.geoconnect;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

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
        setContentView(R.layout.activity_business_dashboard);

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

        // 1. Fetch user 'type' from the 'users' collection to determine the correct target collection
        db.collection("users").document(userId).get()
                .addOnSuccessListener(userDoc -> {
                    String userType = "business"; // Default fallback
                    if (userDoc.exists()) {
                        String type = userDoc.getString("type");
                        if (type != null && !type.trim().isEmpty()) {
                            userType = type.trim().toLowerCase();
                        }

                        // Calculate 'Member Since' year
                        Timestamp createdAtTimestamp = userDoc.getTimestamp("created_at");
                        if (createdAtTimestamp != null) {
                            Date creationDate = createdAtTimestamp.toDate();
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(creationDate);
                            int creationYear = calendar.get(Calendar.YEAR);

                            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                            int yearsDifference = currentYear - creationYear;

                            if (yearsDifference <= 0) {
                                txtMemberSince.setText("1 yr");
                            } else {
                                txtMemberSince.setText(yearsDifference + (yearsDifference == 1 ? " yr" : " yrs"));
                            }
                        } else {
                            txtMemberSince.setText("1 yr");
                        }
                    } else {
                        txtMemberSince.setText("1 yr");
                    }

                    // Resolve target collection name
                    String targetCollection;
                    switch (userType) {
                        case "freelancer":
                            targetCollection = "freelancers";
                            break;
                        case "customer":
                            targetCollection = "customers";
                            break;
                        case "business":
                        default:
                            targetCollection = "businesses";
                            break;
                    }

                    // 2. Fetch profile details from the resolved collection
                    db.collection(targetCollection).document(userId).get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    String name = documentSnapshot.getString("name");
                                    String category = documentSnapshot.getString("category");
                                    String description = documentSnapshot.getString("description");
                                    String approvalStatus = documentSnapshot.getString("approvalStatus");

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

                                    // Handle Rating Calculation safely
                                    Long totalRatings = documentSnapshot.getLong("totalRatings");
                                    Long totalReviews = documentSnapshot.getLong("totalReviews");

                                    if (totalReviews != null && totalReviews > 0 && totalRatings != null) {
                                        double averageRating = (double) totalRatings / totalReviews;
                                        txtRating.setText(String.format("%.1f", averageRating));
                                    } else {
                                        txtRating.setText("0.0");
                                    }
                                }
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to load profile: " + e.getMessage(), Toast.LENGTH_SHORT).show());

                    // 3. Fetch address from the 'location' subcollection (with main document fallback)
                    db.collection(targetCollection).document(userId).collection("location")
                            .orderBy("timestamp", Query.Direction.DESCENDING)
                            .limit(1)
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                if (!querySnapshot.isEmpty()) {
                                    String address = querySnapshot.getDocuments().get(0).getString("address");
                                    if (address != null && !address.trim().isEmpty()) {
                                        txtLocation.setText(address);
                                    } else {
                                        txtLocation.setText("Address not set");
                                    }
                                } else {
                                    // Fallback: Check if address is stored directly on the main document
                                    db.collection(targetCollection).document(userId).get()
                                            .addOnSuccessListener(doc -> {
                                                String directAddress = doc.getString("address");
                                                if (directAddress != null && !directAddress.trim().isEmpty()) {
                                                    txtLocation.setText(directAddress);
                                                } else {
                                                    txtLocation.setText("Address not set");
                                                }
                                            })
                                            .addOnFailureListener(e -> txtLocation.setText("Address not set"));
                                }
                            })
                            .addOnFailureListener(e -> txtLocation.setText("Address not set"));

                })
                .addOnFailureListener(e -> {
                    txtMemberSince.setText("1 yr");
                    txtLocation.setText("Address not set");
                });
    }

    private String capitalizeFirstLetter(String text) {
        if (text == null || text.isEmpty()) return "";
        return text.substring(0, 1).toUpperCase() + text.toLowerCase().substring(1);
    }
}