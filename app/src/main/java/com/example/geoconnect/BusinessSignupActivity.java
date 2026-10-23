package com.example.geoconnect;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class BusinessSignupActivity extends AppCompatActivity {

    private TextView txtErrorMessage;
    private EditText etBusinessName;
    private Spinner spCategory;
    private EditText etAbout;
    private EditText etBusinessEmail;
    private EditText etContactNumber;
    private EditText etWebsite;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private MaterialButton btnRegister;

    // Firebase instances
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_business_signup);
        FreelancerUi.applyInsets(findViewById(android.R.id.content));

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        txtErrorMessage = findViewById(R.id.txtErrorMessage);
        etBusinessName = findViewById(R.id.etBusinessName);
        spCategory = findViewById(R.id.spCategory);
        etAbout = findViewById(R.id.etAbout);
        etBusinessEmail = findViewById(R.id.etBusinessEmail);
        etContactNumber = findViewById(R.id.etContactNumber);
        etWebsite = findViewById(R.id.etWebsite);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        btnRegister.setOnClickListener(v -> registerBusinessUser());
    }

    private void registerBusinessUser() {
        // Extract data
        String businessName = etBusinessName.getText() != null ? etBusinessName.getText().toString().trim() : "";
        String category = spCategory.getSelectedItem() != null ? spCategory.getSelectedItem().toString() : "";
        String description = etAbout.getText() != null ? etAbout.getText().toString().trim() : "";
        String email = etBusinessEmail.getText() != null ? etBusinessEmail.getText().toString().trim() : "";
        String phone = etContactNumber.getText() != null ? etContactNumber.getText().toString().trim() : "";
        String website = etWebsite.getText() != null ? etWebsite.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";

        // Basic Validation
        if (TextUtils.isEmpty(businessName) || TextUtils.isEmpty(category) ||
                TextUtils.isEmpty(email) || TextUtils.isEmpty(phone) ||
                TextUtils.isEmpty(password)) {
            showError("Please fill in all required fields.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match.");
            return;
        }

        if (txtErrorMessage != null) {
            txtErrorMessage.setVisibility(View.GONE);
        }

        // Disable button to prevent multiple clicks during network call
        btnRegister.setEnabled(false);

        // Create user in Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (!isFinishing()) {
                        btnRegister.setEnabled(true);
                    }

                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();

                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("email", email);
                            userMap.put("type", "business");
                            userMap.put("created_at", FieldValue.serverTimestamp());

                            Map<String, Object> businessMap = new HashMap<>();
                            businessMap.put("name", businessName);
                            businessMap.put("description", description);
                            businessMap.put("category", category);
                            businessMap.put("phone", phone);
                            businessMap.put("website", website);
                            businessMap.put("totalRatings", 0);
                            businessMap.put("totalReviews", 0);
                            businessMap.put("accountStatus", "active");
                            businessMap.put("reviewStatus", "pending");
                            businessMap.put("approvalStatus", "pending");

                            db.collection("users").document(userId)
                                    .set(userMap)
                                    .addOnSuccessListener(aVoid -> {
                                        db.collection("businesses").document(userId)
                                                .set(businessMap)
                                                .addOnSuccessListener(aVoid1 -> {
                                                    // Automatically add "Welcome to GeoConnect" notification
                                                    addWelcomeNotification(userId);

                                                    Toast.makeText(BusinessSignupActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(BusinessSignupActivity.this, BusinessDashboardActivity.class);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(intent);
                                                    finish();
                                                })
                                                .addOnFailureListener(e -> showError("Failed to save business profile: " + e.getMessage()));
                                    })
                                    .addOnFailureListener(e -> showError("Failed to save user data: " + e.getMessage()));
                        }
                    } else {
                        // Handle registration failures
                        try {
                            throw task.getException();
                        } catch (com.google.firebase.auth.FirebaseAuthUserCollisionException e) {
                            showError("This email address is already registered.");
                        } catch (com.google.firebase.auth.FirebaseAuthWeakPasswordException e) {
                            showError("Password is too weak. Please use at least 6 characters.");
                        } catch (com.google.firebase.auth.FirebaseAuthInvalidCredentialsException e) {
                            showError("Invalid email format entered.");
                        } catch (Exception e) {
                            showError("Authentication Failed: " + (e != null ? e.getMessage() : "Unknown error"));
                        }
                    }
                });
    }

    private void addWelcomeNotification(String userId) {
        Map<String, Object> notificationMap = new HashMap<>();
        notificationMap.put("title", "Welcome to GeoConnect");
        notificationMap.put("content", "Your business account has been successfully created. Set up your location and operating hours to get started.");
        notificationMap.put("timestamp", FieldValue.serverTimestamp());

        db.collection("users")
                .document(userId)
                .collection("notifications")
                .add(notificationMap);
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