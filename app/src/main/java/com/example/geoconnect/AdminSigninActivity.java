package com.example.geoconnect;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class AdminSigninActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_signin);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        TextView textViewError = findViewById(R.id.textViewError);
        TextInputEditText etUsername = findViewById(R.id.etUsername); // Acts as the email field
        TextInputEditText etPassword = findViewById(R.id.etPassword);
        MaterialButton btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {

            String email = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(
                        AdminSigninActivity.this,
                        "Please enter email and password",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            // Clear previous errors
            textViewError.setText("");

            // Standard Firebase Authentication sign-in
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        Toast.makeText(
                                AdminSigninActivity.this,
                                "Login Successful",
                                Toast.LENGTH_SHORT
                        ).show();

                        startActivity(new Intent(
                                AdminSigninActivity.this,
                                AdminDashboardActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        textViewError.setText(e.getMessage());
                        Toast.makeText(
                                AdminSigninActivity.this,
                                "Login Failed: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    });
        });
    }
}