package com.example.geoconnect;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminReportsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_reports2);

        BottomNavigationView bottomNav =
                findViewById(R.id.bottomNav);

        bottomNav.setSelectedItemId(R.id.nav_reports);

        bottomNav.setOnItemSelectedListener(item -> {

            if (item.getItemId() == R.id.nav_overview) {
                startActivity(new Intent(
                        AdminReportsActivity.this,
                        AdminDashboardActivity.class));
                return true;
            }

            if (item.getItemId() == R.id.nav_users) {
                startActivity(new Intent(
                        AdminReportsActivity.this,
                        AdminUsersActivity.class));
                return true;
            }

            if (item.getItemId() == R.id.nav_review) {
                startActivity(new Intent(
                        AdminReportsActivity.this,
                        AdminReviewActivity.class));
                return true;
            }

            if (item.getItemId() == R.id.nav_reports) {
                return true;
            }

            return false;
        });
    }
}