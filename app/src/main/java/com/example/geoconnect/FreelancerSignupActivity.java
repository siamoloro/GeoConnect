package com.example.geoconnect;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FreelancerSignupActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_freelancer_signup);
        FreelancerUi.applyInsets(findViewById(R.id.main));
        FreelancerUi.setupBack(this);

        // Initialize Firebase Auth, Firestore, and Location client
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        EditText etFullName = findViewById(R.id.etFullName);
        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPhone = findViewById(R.id.etPhone);
        Spinner spCategory = findViewById(R.id.spCategory);
        EditText etPassword = findViewById(R.id.etPassword);
        CheckBox cbTerms = findViewById(R.id.cbTerms);
        Button btnCreate = findViewById(R.id.btnCreateAccount);
        TextView tvSignIn = findViewById(R.id.tvSignIn);

        btnCreate.setOnClickListener(v -> {
            String name = etFullName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String category = spCategory.getSelectedItem() != null ? spCategory.getSelectedItem().toString() : "";
            String password = etPassword.getText().toString();

            if (name.isEmpty()) {
                etFullName.setError("Enter your full name");
                etFullName.requestFocus();
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Enter a valid email address");
                etEmail.requestFocus();
                return;
            }
            if (phone.length() < 9) {
                etPhone.setError("Enter a valid phone number");
                etPhone.requestFocus();
                return;
            }
            if (spCategory.getSelectedItemPosition() == 0) {
                Toast.makeText(this, "Please select your service category", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.length() < 9) {
                etPassword.setError("Password must be at least 9 characters");
                etPassword.requestFocus();
                return;
            }
            if (!cbTerms.isChecked()) {
                Toast.makeText(this, "Please accept the Terms of Service", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check location permissions dynamically before proceeding
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                Toast.makeText(this, "Please grant location permission to register your location.", Toast.LENGTH_LONG).show();
                return;
            }

            // Disable button during network/location request to prevent duplicate submissions
            btnCreate.setEnabled(false);

            // Fetch current device location dynamically
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                double latitude = 0.0;
                double longitude = 0.0;

                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                }

                // Proceed with registration using dynamic coordinates
                registerUserInFirebase(name, email, phone, category, password, latitude, longitude, btnCreate);
            }).addOnFailureListener(e -> {
                // Fallback if location fetching fails, register with default 0.0 coordinates
                registerUserInFirebase(name, email, phone, category, password, 0.0, 0.0, btnCreate);
            });
        });

        tvSignIn.setOnClickListener(v -> {
            startActivity(new Intent(this, SigninActivity.class));
            finish();
        });
    }

    // Helper method placed correctly outside onCreate
    private void registerUserInFirebase(String name, String email, String phone, String category, String password, double latitude, double longitude, Button btnCreate) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String userId = authResult.getUser().getUid();

                    // 1. Store base account details in 'users' collection
                    Map<String, Object> userAccount = new HashMap<>();
                    userAccount.put("userID", userId);
                    userAccount.put("email", email);
                    userAccount.put("type", "freelancer");
                    userAccount.put("status", "Active");
                    userAccount.put("created_at", System.currentTimeMillis());

                    db.collection("users").document(userId)
                            .set(userAccount)
                            .addOnSuccessListener(aVoid -> {
                                // 2. Store profile specifics in 'freelancers' collection
                                Map<String, Object> freelancerData = new HashMap<>();
                                freelancerData.put("userID", userId);
                                freelancerData.put("email", email);
                                freelancerData.put("fullName", name); // Correctly matches customer queries
                                freelancerData.put("phone", phone);
                                freelancerData.put("accountStatus", "active");
                                freelancerData.put("reviewStatus", "pending");
                                freelancerData.put("approvalStatus", "pending");
                                freelancerData.put("category", category);
                                freelancerData.put("description", "");
                                freelancerData.put("serviceArea", "");
                                freelancerData.put("availability", true);

                                // Dynamic GPS coordinates
                                freelancerData.put("latitude", latitude);
                                freelancerData.put("longitude", longitude);

                                freelancerData.put("reviewsCount", 0L);
                                freelancerData.put("jobsDone", 0L);

                                db.collection("freelancers").document(userId)
                                        .set(freelancerData)
                                        .addOnSuccessListener(documentReference -> {
                                            Toast.makeText(this, "Freelancer account created successfully!", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(this, FreelancerDashboardActivity.class));
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            btnCreate.setEnabled(true);
                                            Toast.makeText(this, "Failed to save freelancer profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                btnCreate.setEnabled(true);
                                Toast.makeText(this, "Failed to save user account: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    btnCreate.setEnabled(true);
                    Toast.makeText(this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}