package com.example.geoconnect;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private LinearLayout rowLocationPreferences, rowPrivacySecurity, rowNotifications, rowTrustSafety;
    private TextView btnSignOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        btnBack = findViewById(R.id.btnBack);
        rowLocationPreferences = findViewById(R.id.rowLocationPreferences);
        rowPrivacySecurity = findViewById(R.id.rowPrivacySecurity);
        rowNotifications = findViewById(R.id.rowNotifications);
        rowTrustSafety = findViewById(R.id.rowTrustSafety);
        btnSignOut = findViewById(R.id.btnSignOut);

        // Back button action
        btnBack.setOnClickListener(v -> finish());

        // Option Row Actions
        rowLocationPreferences.setOnClickListener(v ->
                Toast.makeText(this, "Location preferences clicked", Toast.LENGTH_SHORT).show()
        );

        rowPrivacySecurity.setOnClickListener(v ->
                Toast.makeText(this, "Privacy & security clicked", Toast.LENGTH_SHORT).show()
        );

        rowNotifications.setOnClickListener(v ->
                Toast.makeText(this, "Notifications clicked", Toast.LENGTH_SHORT).show()
        );

        rowTrustSafety.setOnClickListener(v ->
                Toast.makeText(this, "Trust & safety clicked", Toast.LENGTH_SHORT).show()
        );

        // Sign Out action (Navigates back to SigninActivity)
        btnSignOut.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, SigninActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}