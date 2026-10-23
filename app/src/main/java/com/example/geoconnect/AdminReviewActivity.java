package com.example.geoconnect;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminReviewActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_review2);
        FreelancerUi.applyInsets(findViewById(android.R.id.content));

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Load reviews from Firestore
        loadReviewsData();

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_review);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_overview) {
                startActivity(new Intent(AdminReviewActivity.this, AdminDashboardActivity.class));
                return true;
            } else if (id == R.id.nav_users) {
                startActivity(new Intent(AdminReviewActivity.this, AdminUsersActivity.class));
                return true;
            } else if (id == R.id.nav_review) {
                return true;
            } else if (id == R.id.nav_reports) {
                startActivity(new Intent(AdminReviewActivity.this, AdminReportsActivity.class));
                return true;
            }

            return false;
        });
    }

    private void loadReviewsData() {
        // Fetch reviews/feedback from Firestore 'reviews' collection
        db.collection("reviews")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalReviews = queryDocumentSnapshots.size();
                    // TODO: Bind reviews list/adapter or display totalReviews count here
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load reviews: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}