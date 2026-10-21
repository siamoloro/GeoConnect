package com.example.geoconnect;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

public class SettingsActivity extends AppCompatActivity {

    LinearLayout rowLocationPreferences, rowSecurity, rowSupport;
    Button btnSignOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

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

        // Sign Out action (Navigates back to SigninActivity)
        btnSignOut.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, SigninActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}