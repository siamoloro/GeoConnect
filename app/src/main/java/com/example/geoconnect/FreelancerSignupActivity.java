package com.example.geoconnect;

import android.os.Bundle;
import android.content.Intent;
import android.util.Patterns;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FreelancerSignupActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_freelancer_signup);
        FreelancerUi.applyInsets(findViewById(R.id.main));
        FreelancerUi.setupBack(this);

        // Initialize Firebase Auth and Firestore instances
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

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

            // Disable button during network request to prevent duplicate submissions
            btnCreate.setEnabled(false);

            // Create Firebase Auth user
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        String userId = authResult.getUser().getUid();

                        // 1. Store base account details in 'users' collection (matches User_Account ERD table)
                        Map<String, Object> userAccount = new HashMap<>();
                        userAccount.put("userID", userId);
                        userAccount.put("email", email);
                        userAccount.put("type", "freelancer");
                        userAccount.put("status", "Active");
                        userAccount.put("created_at", System.currentTimeMillis());

                        db.collection("users").document(userId)
                                .set(userAccount)
                                .addOnSuccessListener(aVoid -> {
                                    // 2. Store profile specifics in 'freelancer' collection linked by foreign key (userID)
                                    Map<String, Object> freelancerData = new HashMap<>();
                                    freelancerData.put("userID", userId);
                                    freelancerData.put("name", name);
                                    freelancerData.put("phone", phone);
                                    freelancerData.put("category", category);
                                    freelancerData.put("description", ""); // Initial blank bio
                                    freelancerData.put("serviceArea", ""); // Initial blank service area
                                    freelancerData.put("availability", true);

                                    db.collection("freelancers")
                                            .add(freelancerData)
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
        });

        tvSignIn.setOnClickListener(v -> {
            startActivity(new Intent(this, SigninActivity.class));
            finish();
        });
    }
}