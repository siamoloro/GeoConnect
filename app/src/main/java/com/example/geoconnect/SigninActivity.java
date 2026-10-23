package com.example.geoconnect;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
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

    // Firebase instances
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signin); // Keeps your existing XML layout name
        FreelancerUi.applyInsets(findViewById(android.R.id.content));

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize Views safely
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
            Toast.makeText(this, "Forgot password clicked", Toast.LENGTH_SHORT).show();
        });

        layoutAdminPortal.setOnClickListener(v ->
                startActivity(new Intent(this, AdminSigninActivity.class)));
    }

    private void signinUser() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        // Basic Validation
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            showError("Please enter both email and password.");
            return;
        }

        if (txtErrorMessage != null) {
            txtErrorMessage.setVisibility(View.GONE);
        }

        // Disable button to prevent double-clicks during signin network request
        btnSignIn.setEnabled(false);

        // 1. Authenticate with Firebase Auth
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            registerSession(firebaseUser.getUid());
                            fetchUserRoleAndRedirect(firebaseUser.getUid());
                        }
                    } else {
                        if (!isFinishing()) {
                            btnSignIn.setEnabled(true);
                        }
                        // Handle signin failure messages cleanly
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
        // 2. Query the 'users' collection to check the user 'role' field
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!isFinishing()) {
                        btnSignIn.setEnabled(true);
                    }

                    if (documentSnapshot.exists()) {
                        // The signup screens save the role as 'type'; admins (made by hand) use 'role'
                        String userRole = documentSnapshot.getString("type");
                        if (userRole == null) userRole = documentSnapshot.getString("role");

                        if (userRole == null) {
                            showError("User role is undefined. Please contact support.");
                            return;
                        }

                        Intent intent;
                        // 3. Route user to the appropriate Dashboard based on their role
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
                                intent = new Intent(SigninActivity.this, AdminDashboardActivity.class);
                                break;
                            default:
                                showError("Unknown user role type.");
                                return;
                        }

                        // Clear task stack so user cannot press back into signin screen after signing in
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
                    showError("Failed to fetch user role: " + e.getMessage());
                });
    }
    private void registerSession(String uid) {
        java.util.Map<String, Object> session = new java.util.HashMap<>();
        session.put("userID", uid);
        session.put("deviceName", android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL);
        session.put("locationInfo", "Active " + new java.text.SimpleDateFormat("dd MMM, HH:mm", java.util.Locale.getDefault()).format(new java.util.Date()));
        session.put("createdAt", System.currentTimeMillis());

        db.collection("sessions").add(session)
                .addOnSuccessListener(ref -> getSharedPreferences("geoconnect", MODE_PRIVATE)
                        .edit().putString("sessionId", ref.getId()).apply());
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