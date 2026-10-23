package com.example.geoconnect;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {

    LinearLayout rowLocationPreferences, rowSecurity, rowSupport;
    Button btnSignOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        FreelancerUi.applyInsets(findViewById(android.R.id.content));

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        rowLocationPreferences = findViewById(R.id.rowLocationPreferences);
        rowSecurity = findViewById(R.id.rowPrivacySecurity);
        rowSupport = findViewById(R.id.rowTrustSafety);
        btnSignOut = findViewById(R.id.btnSignOut);


        toolbar.setNavigationOnClickListener(v -> finish());

        // Option Row Actions
        rowLocationPreferences.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, LocationPreferencesActivity.class);
            startActivity(intent);
        });

        rowSecurity.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, SecurityActivity.class);
            startActivity(intent);
        });

        rowSupport.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, SupportActivity.class);
            startActivity(intent);
        });

        // Sign Out action navigates back to Signin
        btnSignOut.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(SettingsActivity.this, SigninActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}