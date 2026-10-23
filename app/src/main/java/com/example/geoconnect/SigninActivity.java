package com.example.geoconnect;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SigninActivity extends AppCompatActivity {

    private TextView txtErrorMessage;
    private EditText etEmail;
    private EditText etPassword;
    private MaterialButton btnSignIn;
    private TextView txtForgotPassword;
    private View layoutAdminPortal;
    private MaterialToolbar toolbar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        toolbar = findViewById(R.id.toolbar);
        txtErrorMessage = findViewById(R.id.txtErrorMessage);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        txtForgotPassword = findViewById(R.id.txtForgotPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        layoutAdminPortal = findViewById(R.id.layoutAdminPortal);

        // Setup Toolbar back button navigation
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        btnSignIn.setOnClickListener(v -> signinUser());

        txtForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(SigninActivity.this, ResetPasswordActivity.class);
            startActivity(intent);
        });

        layoutAdminPortal.setOnClickListener(v -> {
            Toast.makeText(this, "Admin portal clicked", Toast.LENGTH_SHORT).show();
        });
    }

    private void signinUser() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            showError("Please enter both email and password.");
            return;
        }

        if (txtErrorMessage != null) {
            txtErrorMessage.setVisibility(View.GONE);
        }

        btnSignIn.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            fetchUserRoleAndRedirect(firebaseUser.getUid());
                        }
                    } else {
                        if (!isFinishing()) {
                            btnSignIn.setEnabled(true);
                        }

                        try {
                            throw task.getException();
                        } catch (com.google.firebase.auth.FirebaseAuthInvalidUserException e) {
                            showError("No account found with this email.");
                        } catch (com.google.firebase.auth.FirebaseAuthInvalidCredentialsException e) {
                            showError("Incorrect password. Please try again.");
                        } catch (Exception e) {
                            showError("Authentication failed: " + (e != null ? e.getMessage() : "Unknown error"));
                        }
                    }
                });
    }

    private void fetchUserRoleAndRedirect(String userId) {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!isFinishing()) {
                        btnSignIn.setEnabled(true);
                    }

                    if (documentSnapshot.exists()) {
                        String userRole = documentSnapshot.getString("type");

                        if (userRole == null) {
                            showError("User role is undefined. Please contact support.");
                            return;
                        }

                        Intent intent;

                        switch (userRole.toLowerCase()) {
                            case "customer":
                                intent = new Intent(SigninActivity.this, CustomerDashboardActivity.class);
                                break;
                            case "business":
                                intent = new Intent(SigninActivity.this, BusinessDashboardActivity.class);
                                break;
                            case "freelancer":
                                intent = new Intent(SigninActivity.this, FreelancerDashboardActivity.class);
                                break;
                            case "admin":
                                intent = new Intent(SigninActivity.this, BusinessDashboardActivity.class);
                                break;
                            default:
                                showError("Unknown user type.");
                                return;
                        }

                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();

                    } else {
                        showError("User profile record not found in database.");
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isFinishing()) {
                        btnSignIn.setEnabled(true);
                    }
                    showError("Failed to fetch user type: " + e.getMessage());
                });
    }

    private void showError(String message) {
        if (txtErrorMessage != null) {
            txtErrorMessage.setText(message);
            txtErrorMessage.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }
}