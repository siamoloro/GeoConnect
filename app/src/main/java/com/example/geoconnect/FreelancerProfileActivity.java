package com.example.geoconnect;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;

public class FreelancerProfileActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private TextView tvProfileName, tvProfileCategory, tvProfileBio, tvProfilePhone, tvProfileServiceArea;
    private TextView tvProfileRating, tvProfileJobsDone, tvProfileResponse, tvProfileExperience, tvServiceRate1;
    private TextView tvProfileInitials;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_freelancer_profile);
        FreelancerUi.applyInsets(findViewById(R.id.main));
        FreelancerUi.setupNav(this, "Profile");

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        tvProfileInitials = findViewById(R.id.tvProfileInitials);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileCategory = findViewById(R.id.tvProfileCategory);
        tvProfileBio = findViewById(R.id.tvProfileBio);
        tvProfilePhone = findViewById(R.id.tvProfilePhone);
        tvProfileServiceArea = findViewById(R.id.tvProfileServiceArea);

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
        loadBookingStats();
    }

    private String str(DocumentSnapshot d, String field) {
        Object o = d.get(field);
        return o == null ? "" : o.toString().trim();
    }

    private String initialsOf(String name) {
        if (name == null || name.trim().isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        String s = parts[0].substring(0, 1);
        if (parts.length > 1) s += parts[parts.length - 1].substring(0, 1);
        return s.toUpperCase();
    }

    private void loadFreelancerProfile() {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("freelancers")
                .whereEqualTo("userID", userId)
                .limit(1)
                .get()
                .addOnSuccessListener(snap -> {
                    if (snap.isEmpty()) return;
                    DocumentSnapshot d = snap.getDocuments().get(0);

                    String name = str(d, "fullName");
                    if (name.isEmpty()) name = str(d, "name");

                    String category = str(d, "category");
                    if (category.isEmpty()) category = str(d, "skills");

                    String serviceArea = str(d, "serviceArea");
                    String bio = str(d, "description");
                    String phone = str(d, "phone");
                    String approvalStatus = str(d, "approvalStatus");

                    tvProfileName.setText(name.isEmpty() ? "Unnamed Freelancer" : name);
                    tvProfileInitials.setText(initialsOf(name));
                    tvProfileCategory.setText(category);
                    tvProfileServiceArea.setText(serviceArea);
                    tvProfilePhone.setText(phone);
                    tvProfileBio.setText(bio.isEmpty() ? "No bio added yet." : bio);

                    // Rating: only show a number when there are real reviews
                    Double rating = d.getDouble("rating");
                    Long reviewsCount = d.getLong("reviewsCount");
                    if (rating != null && reviewsCount != null && reviewsCount > 0) {
                        tvProfileRating.setText(String.format(Locale.getDefault(),
                                "★ %.1f (%d reviews)", rating, reviewsCount));
                    } else {
                        tvProfileRating.setText("No reviews yet");
                    }

                    // Experience: saved by the Edit profile screen as whole years
                    Long years = d.getLong("experienceYears");
                    if (years != null && years >= 0) {
                        tvProfileExperience.setText(years == 1 ? "1 yr" : years + " yrs");
                    } else {
                        tvProfileExperience.setText("—");
                    }

                    // Hourly rate saved by the Edit profile screen
                    String rate = str(d, "rate");
                    tvServiceRate1.setText(rate.isEmpty() ? "Not set" : "R" + rate + "/hr");

                    TextView tvVerifiedBadge = findViewById(R.id.tvVerifiedBadge);
                    if (tvVerifiedBadge != null) {
                        if ("Approved".equalsIgnoreCase(approvalStatus) || "Verified".equalsIgnoreCase(approvalStatus)) {
                            tvVerifiedBadge.setVisibility(View.VISIBLE);
                            tvVerifiedBadge.setText("Verified");
                        } else if ("Pending".equalsIgnoreCase(approvalStatus)) {
                            tvVerifiedBadge.setVisibility(View.VISIBLE);
                            tvVerifiedBadge.setText("Pending Approval");
                        } else {
                            tvVerifiedBadge.setVisibility(View.GONE);
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this,
                        "Failed to load profile: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // Jobs done and response rate are calculated from this freelancer's real bookings
    private void loadBookingStats() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

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
                                int total = 0;
                                int answered = 0;
                                int completed = 0;

                                for (DocumentSnapshot b : snap.getDocuments()) {
                                    total++;
                                    String status = str(b, "status");
                                    if (!status.isEmpty() && !"Pending".equalsIgnoreCase(status)) answered++;
                                    if ("Completed".equalsIgnoreCase(status)) completed++;
                                }

                                tvProfileJobsDone.setText(String.valueOf(completed));
                                if (total == 0) {
                                    tvProfileResponse.setText("—");
                                } else {
                                    tvProfileResponse.setText(Math.round(answered * 100f / total) + "%");
                                }
                            })
                            .addOnFailureListener(e -> {
                                tvProfileJobsDone.setText("—");
                                tvProfileResponse.setText("—");
                            });
                });
    }
}