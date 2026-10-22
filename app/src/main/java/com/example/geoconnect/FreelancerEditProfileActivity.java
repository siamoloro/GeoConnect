package com.example.geoconnect;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FreelancerEditProfileActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private EditText etFullName, etPhone, etRate, etServiceArea, etBio;
    private Spinner spCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_freelancer_edit_profile);
        FreelancerUi.applyInsets(findViewById(R.id.main));
        FreelancerUi.setupBack(this);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        etFullName = findViewById(R.id.etFullName);
        etPhone = findViewById(R.id.etPhone);
        spCategory = findViewById(R.id.spCategory);
        etRate = findViewById(R.id.etRate);
        etServiceArea = findViewById(R.id.etServiceArea);
        etBio = findViewById(R.id.etBio);
        Button btnSave = findViewById(R.id.btnSaveChanges);

        // Load existing profile data from Firestore
        loadFreelancerProfile();

        findViewById(R.id.tvChangePhoto).setOnClickListener(v -> {
            Toast.makeText(this, "Photo upload coming soon", Toast.LENGTH_SHORT).show();
        });

        btnSave.setOnClickListener(v -> saveProfileToFirestore());

        findViewById(R.id.tvCancel).setOnClickListener(v -> finish());
    }

    private void loadFreelancerProfile() {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        // Query Firestore 'freelancer' collection matching the user's ID field
        db.collection("freelancer")
                .whereEqualTo("userID", userId)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        var document = queryDocumentSnapshots.getDocuments().get(0);

                        etFullName.setText(document.getString("name"));
                        etPhone.setText(document.getString("phone"));
                        etServiceArea.setText(document.getString("serviceArea"));
                        etBio.setText(document.getString("description"));

                        // Optional rate/category fields if stored in your document
                        String rateStr = document.getString("rate");
                        if (rateStr != null) etRate.setText(rateStr);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveProfileToFirestore() {
        String name = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String rate = etRate.getText().toString().trim();
        String serviceArea = etServiceArea.getText().toString().trim();
        String bio = etBio.getText().toString().trim();
        String category = spCategory.getSelectedItem() != null ? spCategory.getSelectedItem().toString() : "";

        if (name.isEmpty()) {
            etFullName.setError("Enter your full name");
            etFullName.requestFocus();
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
        if (rate.isEmpty() || !TextUtils.isDigitsOnly(rate) || Integer.parseInt(rate) <= 0) {
            etRate.setError("Enter a valid hourly rate as a number");
            etRate.requestFocus();
            return;
        }

        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        Map<String, Object> freelancerData = new HashMap<>();
        freelancerData.put("name", name);
        freelancerData.put("phone", phone);
        freelancerData.put("category", category);
        freelancerData.put("rate", rate);
        freelancerData.put("serviceArea", serviceArea);
        freelancerData.put("description", bio);
        freelancerData.put("userID", userId);

        // Update or write back to Firestore 'freelancer' collection
        db.collection("freelancer")
                .whereEqualTo("userID", userId)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Update existing document
                        String docId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        db.collection("freelancer").document(docId)
                                .update(freelancerData)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Error updating: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    } else {
                        // Create new record if it doesn't exist yet
                        db.collection("freelancer")
                                .add(freelancerData)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(this, "Profile created successfully!", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Error saving: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Database connection error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}