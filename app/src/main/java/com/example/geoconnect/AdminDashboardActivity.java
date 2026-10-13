package com.example.geoconnect;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_dashboard);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        BottomNavigationView bottomNav =
                findViewById(R.id.bottom_navigation);

        bottomNav.setSelectedItemId(R.id.nav_overview);

        bottomNav.setOnItemSelectedListener(item -> {

            if (item.getItemId() == R.id.nav_overview) {
                return true;
            }

            if (item.getItemId() == R.id.nav_users) {
                startActivity(new Intent(
                        AdminDashboardActivity.this,
                        AdminUsersActivity.class));
                return true;
            }

            if (item.getItemId() == R.id.nav_review) {
                startActivity(new Intent(
                        AdminDashboardActivity.this,
                        AdminReviewActivity.class));
                return true;
            }

            if (item.getItemId() == R.id.nav_reports) {
                startActivity(new Intent(
                        AdminDashboardActivity.this,
                        AdminReportsActivity.class));
                return true;
            }

            return false;
        });
      }
    }