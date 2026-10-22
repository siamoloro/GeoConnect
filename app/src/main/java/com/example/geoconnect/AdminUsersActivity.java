package com.example.geoconnect;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminUsersActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_users2);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Load users from Firestore
        loadUsersData();

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_users);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_overview) {
                startActivity(new Intent(AdminUsersActivity.this, AdminDashboardActivity.class));
                return true;
            } else if (id == R.id.nav_users) {
                return true;
            } else if (id == R.id.nav_review) {
                startActivity(new Intent(AdminUsersActivity.this, AdminReviewActivity.class));
                return true;
            } else if (id == R.id.nav_reports) {
                startActivity(new Intent(AdminUsersActivity.this, AdminReportsActivity.class));
                return true;
            }

            return false;
        });
    }

    private void loadUsersData() {
        // Fetch users from Firestore 'users' collection to display in the admin user management panel
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalRegisteredUsers = queryDocumentSnapshots.size();
                    // TODO: Bind your user data to a RecyclerView/Adapter here if you are displaying a list of users
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load users: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}