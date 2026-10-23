package com.example.geoconnect;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

public class SecurityActivity extends AppCompatActivity {

    LinearLayout rowChangePassword, rowActiveSessions;
    Button btnDeleteAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_security);
        FreelancerUi.applyInsets(findViewById(android.R.id.content));

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        rowChangePassword = findViewById(R.id.rowChangePassword);
        rowActiveSessions = findViewById(R.id.rowActiveSessions);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);

        toolbar.setNavigationOnClickListener(v -> finish());

        rowChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(SecurityActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        });

        rowActiveSessions.setOnClickListener(v -> {
            Intent intent = new Intent(SecurityActivity.this, ActiveSessionsActivity.class);
            startActivity(intent);
        });

        btnDeleteAccount.setOnClickListener(v -> {

        });
    }
}