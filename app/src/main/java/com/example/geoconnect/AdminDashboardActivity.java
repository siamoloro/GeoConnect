package com.example.geoconnect;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminDashboardActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_dashboard);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Load metrics from Firestore
        loadDashboardMetrics();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_overview);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_overview) {
                return true;
            } else if (id == R.id.nav_users) {
                startActivity(new Intent(AdminDashboardActivity.this, AdminUsersActivity.class));
                return true;
            } else if (id == R.id.nav_review) {
                startActivity(new Intent(AdminDashboardActivity.this, AdminReviewActivity.class));
                return true;
            } else if (id == R.id.nav_reports) {
                startActivity(new Intent(AdminDashboardActivity.this, AdminReportsActivity.class));
                return true;
            }

            return false;
        });
    }

    private void loadDashboardMetrics() {
        // Fetch users count from Firestore
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalUsers = queryDocumentSnapshots.size();
                    // You can display totalUsers in a TextView if you want
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load metrics: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}