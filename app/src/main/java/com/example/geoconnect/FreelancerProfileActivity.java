package com.example.geoconnect;

import android.os.Bundle;
import android.content.Intent;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class FreelancerProfileActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private TextView tvProfileName, tvProfileCategory, tvProfileBio, tvProfilePhone, tvProfileServiceArea;
    private TextView tvProfileRating, tvProfileJobsDone, tvProfileResponse, tvProfileExperience, tvServiceRate1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_freelancer_profile);
        FreelancerUi.applyInsets(findViewById(R.id.main));
        FreelancerUi.setupNav(this, "Profile");

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Bind all TextView variables to their matching IDs in your XML layout
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileCategory = findViewById(R.id.tvProfileCategory);
        tvProfileBio = findViewById(R.id.tvProfileBio);
        tvProfilePhone = findViewById(R.id.tvProfilePhone);
        tvProfileServiceArea = findViewById(R.id.tvProfileServiceArea);

        // Bind dynamic stat and rate fields
        tvProfileRating = findViewById(R.id.tvProfileRating);
        tvProfileJobsDone = findViewById(R.id.tvProfileJobsDone);
        tvProfileResponse = findViewById(R.id.tvProfileResponse);
        tvProfileExperience = findViewById(R.id.tvProfileExperience);
        tvServiceRate1 = findViewById(R.id.tvServiceRate1);

        findViewById(R.id.btnEditProfile).setOnClickListener(v ->
                startActivity(new Intent(this, FreelancerEditProfileActivity.class)));
        findViewById(R.id.btnSettings).setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFreelancerProfile();
    }
    private void loadFreelancerProfile() {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("freelancers")
                .whereEqualTo("userID", userId)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        var document = queryDocumentSnapshots.getDocuments().get(0);

                        String name = document.getString("name");
                        if (name == null || name.isEmpty()) {
                            name = document.getString("fullName");
                        }

                        String category = document.getString("category");
                        String serviceArea = document.getString("serviceArea");
                        String bio = document.getString("description");
                        String phone = document.getString("phone");

                        // Fetch approval status from database
                        String approvalStatus = document.getString("approvalStatus");

                        Double rating = document.getDouble("rating");
                        Long reviewsCount = document.getLong("reviewsCount");
                        Long jobsDone = document.getLong("jobsDone");
                        String responseRate = document.getString("responseRate");
                        String experience = document.getString("experience");
                        String serviceRate1 = document.getString("serviceRate1");

                        // Update standard UI fields
                        if (tvProfileName != null) tvProfileName.setText(name != null ? name : "Unnamed Freelancer");
                        if (tvProfileCategory != null && category != null) tvProfileCategory.setText(category);
                        if (tvProfileServiceArea != null && serviceArea != null) tvProfileServiceArea.setText(serviceArea);
                        if (tvProfilePhone != null && phone != null) tvProfilePhone.setText(phone);
                        if (tvProfileBio != null && bio != null) tvProfileBio.setText(bio);

                        if (tvProfileRating != null && rating != null) {
                            String reviews = reviewsCount != null ? " (" + reviewsCount + " reviews)" : "";
                            tvProfileRating.setText("★ " + rating + reviews);
                        }
                        if (tvProfileJobsDone != null && jobsDone != null) tvProfileJobsDone.setText(String.valueOf(jobsDone));
                        if (tvProfileResponse != null && responseRate != null) tvProfileResponse.setText(responseRate);
                        if (tvProfileExperience != null && experience != null) tvProfileExperience.setText(experience);
                        if (tvServiceRate1 != null && serviceRate1 != null) tvServiceRate1.setText(serviceRate1);

                        // --- Dynamic Verified Badge Logic ---
                        TextView tvVerifiedBadge = findViewById(R.id.tvVerifiedBadge); // Make sure to give your badge TextView this ID in XML if it doesn't have one
                        if (tvVerifiedBadge != null) {
                            if ("Approved".equalsIgnoreCase(approvalStatus) || "Verified".equalsIgnoreCase(approvalStatus)) {
                                tvVerifiedBadge.setVisibility(android.view.View.VISIBLE);
                                tvVerifiedBadge.setText(approvalStatus); // Displays "Approved" or "Verified"
                            } else if ("Pending".equalsIgnoreCase(approvalStatus)) {
                                tvVerifiedBadge.setVisibility(android.view.View.VISIBLE);
                                tvVerifiedBadge.setText("Pending Approval");
                            } else {
                                // Hide badge or show unverified if status is missing/rejected
                                tvVerifiedBadge.setVisibility(android.view.View.GONE);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}