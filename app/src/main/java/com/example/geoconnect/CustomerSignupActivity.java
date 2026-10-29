package com.example.geoconnect;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.Timestamp;

import java.util.HashMap;
import java.util.Map;

public class CustomerSignupActivity extends AppCompatActivity {

    private EditText fullNameInput;
    private EditText emailInput;
    private EditText phoneInput;
    private EditText passwordInput;

    private View createButton;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_customer_signup);

        TextView backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> {
            finish();
        });

        // Connect XML components
        fullNameInput = findViewById(R.id.FillNameInput);
        emailInput = findViewById(R.id.emailInput);
        phoneInput = findViewById(R.id.phoneInput);
        passwordInput = findViewById(R.id.passwordInput);
        createButton = findViewById(R.id.createAccountButton);

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Sign-up button
        createButton.setOnClickListener(v -> validateAndRegister());
    }

    private void validateAndRegister() {

        String fullName = fullNameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

        // Full name validation
        if (fullName.isEmpty()) {
            fullNameInput.setError("Enter your full name");
            fullNameInput.requestFocus();
            return;
        }

        // Email validation
        if (email.isEmpty() ||
                !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Enter a valid email address");
            emailInput.requestFocus();
            return;
        }

        // South African phone number validation
        if (!phone.matches("0[6-8][0-9]{8}")) {
            phoneInput.setError("Enter a valid South African mobile number");
            phoneInput.requestFocus();
            return;
        }

        // Password validation
        if (password.length() < 8) {
            passwordInput.setError(
                    "Password must be at least 8 characters");
            passwordInput.requestFocus();
            return;
        }

        registerCustomer(fullName, email, phone, password);
    }

    private void registerCustomer(String fullName,
                                  String email,
                                  String phone,
                                  String password) {

        createButton.setEnabled(false);

        // Create account in Firebase Authentication
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {

                    if (!task.isSuccessful()) {
                        createButton.setEnabled(true);

                        String error = task.getException() != null
                                ? task.getException().getMessage()
                                : "Registration failed";

                        Toast.makeText(this, error,
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    FirebaseUser firebaseUser =
                            firebaseAuth.getCurrentUser();

                    if (firebaseUser == null) {
                        createButton.setEnabled(true);
                        Toast.makeText(this,
                                "Could not retrieve user account",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    String userId = firebaseUser.getUid();

                    // General user information
                    Map<String, Object> user = new HashMap<>();
                    user.put("userID", userId);
                    user.put("email", email);
                    user.put("type", "customer");
                    user.put("status", "active");
                    user.put("created_at", Timestamp.now());

                    // Customer-specific information
                    Map<String, Object> customer = new HashMap<>();
                    customer.put("customerID", userId);
                    customer.put("userID", userId);
                    customer.put("fullname", fullName);
                    customer.put("phone", phone);

                    // Save both documents together
                    WriteBatch batch = firestore.batch();

                    batch.set(
                            firestore.collection("users")
                                    .document(userId),
                            user
                    );

                    batch.set(
                            firestore.collection("customers")
                                    .document(userId),
                            customer
                    );

                    batch.commit()
                            .addOnSuccessListener(unused -> {
                                // Automatically add welcome notification
                                addWelcomeNotification(userId);

                                Toast.makeText(this,
                                        "Account created successfully!",
                                        Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(
                                        CustomerSignupActivity.this,
                                        CustomerDashboardActivity.class
                                );

                                intent.putExtra("customerName", fullName);
                                intent.putExtra("customerEmail", email);
                                intent.putExtra("customerPhone", phone);

                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e -> {

                                createButton.setEnabled(true);

                                Toast.makeText(this,
                                        "Account created, but profile saving failed: "
                                                + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            });
                });
    }

    private void addWelcomeNotification(String userId) {
        Map<String, Object> notificationMap = new HashMap<>();
        notificationMap.put("title", "Welcome to GeoConnect");
        notificationMap.put("content", "Your customer account has been successfully created. Explore nearby service providers and book appointments easily.");
        notificationMap.put("timestamp", FieldValue.serverTimestamp());

        firestore.collection("users")
                .document(userId)
                .collection("notifications")
                .add(notificationMap);
    }
}