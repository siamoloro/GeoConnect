package com.example.geoconnect;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminReportsActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_reports2);
        FreelancerUi.applyInsets(findViewById(android.R.id.content));

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Load report data from Firestore
        loadReportData();

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_reports);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_overview) {
                startActivity(new Intent(AdminReportsActivity.this, AdminDashboardActivity.class));
                return true;
            } else if (id == R.id.nav_users) {
                startActivity(new Intent(AdminReportsActivity.this, AdminUsersActivity.class));
                return true;
            } else if (id == R.id.nav_review) {
                startActivity(new Intent(AdminReportsActivity.this, AdminReviewActivity.class));
                return true;
            } else if (id == R.id.nav_reports) {
                return true;
            }

            return false;
        });
    }

    private void loadReportData() {
        // Fetch all support cases or analytical records from Firestore for reporting
        db.collection("cases")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalCasesReported = queryDocumentSnapshots.size();
                    // TODO: Bind totalCasesReported to your report views/charts if needed
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load reports: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}