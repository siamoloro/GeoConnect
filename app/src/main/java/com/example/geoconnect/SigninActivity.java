package com.example.geoconnect;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class SigninActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private TextView txtErrorMessage, txtForgotPassword;
    private MaterialButton btnSignIn;
    private LinearLayout layoutAdminPortal;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        btnBack = findViewById(R.id.btnBack);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        txtErrorMessage = findViewById(R.id.txtErrorMessage);
        txtForgotPassword = findViewById(R.id.txtForgotPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        layoutAdminPortal = findViewById(R.id.layoutAdminPortal);

        // Back button action
        btnBack.setOnClickListener(v -> finish());

        // Sign in logic with validation check
        btnSignIn.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim().toLowerCase();
            String password = etPassword.getText().toString().trim();

            // Example check: To be replaced with Firebase or backend authentication logic
            if (email.isEmpty() || password.isEmpty() || !email.equals("user@example.com")) {
                // Show the error banner if credentials are invalid/empty
                txtErrorMessage.setVisibility(View.VISIBLE);
            } else {
                // Hide error banner and proceed to dashboard
                txtErrorMessage.setVisibility(View.GONE);
                Intent intent = new Intent(SigninActivity.this, CustomerDashboardActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Admin portal navigation
        layoutAdminPortal.setOnClickListener(v -> {
            Intent intent = new Intent(SigninActivity.this, AdminSigninActivity.class);
            startActivity(intent);
        });
    }
}