package com.example.geoconnect;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminSigninActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private EditText etEmail, etPassword;
    private TextView txtForgot;
    private MaterialButton btnLogin;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_signin);

        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Bind Views matching your XML layout IDs
        btnBack = findViewById(R.id.btnBack);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        txtForgot = findViewById(R.id.txtForgot);
        btnLogin = findViewById(R.id.btnLogin);

        // Back button action
        btnBack.setOnClickListener(v -> finish());

        // Login button action
        btnLogin.setOnClickListener(v -> loginAdmin());

        // Reset password action
        txtForgot.setOnClickListener(v -> {
            String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
            if (TextUtils.isEmpty(email)) {
                etEmail.setError("Enter your email address first");
                etEmail.requestFocus();
                return;
            }

            mAuth.sendPasswordResetEmail(email)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Password reset email sent!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    private void loginAdmin() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        // Step 1: Authenticate with Firebase Auth
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        checkIfUserIsAdmin(user.getUid());
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Login Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void checkIfUserIsAdmin(String uid) {
        // Step 2: Check Firestore 'users' collection for admin privileges
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Adjust "role" field to match what you have in your Firestore document (e.g., "admin")
                        String role = documentSnapshot.getString("role");

                        if ("admin".equals(role)) {
                            Toast.makeText(this, "Admin Login Successful!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(AdminSigninActivity.this, AdminDashboardActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // If they authenticated successfully but are a customer/freelancer, deny access
                            Toast.makeText(this, "Access Denied: You do not have administrator rights.", Toast.LENGTH_LONG).show();
                            mAuth.signOut();
                        }
                    } else {
                        Toast.makeText(this, "User record not found in database.", Toast.LENGTH_LONG).show();
                        mAuth.signOut();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to verify admin role: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    mAuth.signOut();
                });
    }
}