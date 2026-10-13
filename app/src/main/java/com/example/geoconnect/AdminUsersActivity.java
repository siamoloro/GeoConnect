package com.example.geoconnect;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminUsersActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_users2);

        BottomNavigationView bottomNav =
                findViewById(R.id.bottomNav);

        bottomNav.setSelectedItemId(R.id.nav_users);

        bottomNav.setOnItemSelectedListener(item -> {

            if (item.getItemId() == R.id.nav_overview) {
                startActivity(new Intent(
                        AdminUsersActivity.this,
                        AdminDashboardActivity.class));
                return true;
            }

            if (item.getItemId() == R.id.nav_users) {
                return true;
            }

            if (item.getItemId() == R.id.nav_review) {
                startActivity(new Intent(
                        AdminUsersActivity.this,
                        AdminReviewActivity.class));
                return true;
            }

            if (item.getItemId() == R.id.nav_reports) {
                startActivity(new Intent(
                        AdminUsersActivity.this,
                        AdminReportsActivity.class));
                return true;
            }

            return false;
        });
    }
}