package com.example.geoconnect;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class BusinessEditProfileActivity extends AppCompatActivity {

    private TextInputEditText edtBusinessName, edtCategory, edtDescription, edtPhone, edtWebsite, edtAddress;
    private MaterialButton btnSaveDetails;

    private FirebaseFirestore db;
    private String businessId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_business_edit_profile);
        FreelancerUi.applyInsets(findViewById(android.R.id.content));

        db = FirebaseFirestore.getInstance();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            businessId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            businessId = "default_business_id";
        }

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        edtBusinessName = findViewById(R.id.edtBusinessName);
        edtCategory = findViewById(R.id.edtCategory);
        edtDescription = findViewById(R.id.edtDescription);
        edtPhone = findViewById(R.id.edtPhone);
        edtWebsite = findViewById(R.id.edtWebsite);
        edtAddress = findViewById(R.id.edtAddress);

        btnSaveDetails = findViewById(R.id.btnSaveDetails);
        btnSaveDetails.setOnClickListener(v -> extractUserInputsAndSave());

        loadDataFromDatabase();
    }

    private void loadDataFromDatabase() {
        if (businessId == null) return;

        DocumentReference docRef = db.collection("businesses").document(businessId);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {

                    String dbName = document.getString("name");
                    String dbCategory = document.getString("category");
                    String dbDescription = document.getString("description");
                    String dbPhone = document.getString("phone");
                    String dbWebsite = document.getString("website");
                    String dbAddress = document.getString("address");

                    if (dbName != null) edtBusinessName.setText(dbName);
                    if (dbCategory != null) edtCategory.setText(dbCategory);
                    if (dbDescription != null) edtDescription.setText(dbDescription);
                    if (dbPhone != null) edtPhone.setText(dbPhone);
                    if (dbWebsite != null) edtWebsite.setText(dbWebsite);
                    if (dbAddress != null) edtAddress.setText(dbAddress);
                } else {
                    Toast.makeText(this, "No profile found. Creating a new one on save.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Failed to load profile: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void extractUserInputsAndSave() {
        String businessName = edtBusinessName.getText().toString().trim();
        String category     = edtCategory.getText().toString().trim();
        String description  = edtDescription.getText().toString().trim();
        String phone        = edtPhone.getText().toString().trim();
        String website      = edtWebsite.getText().toString().trim();
        String address      = edtAddress.getText().toString().trim();

        if (businessName.isEmpty()) {
            edtBusinessName.setError("Business name cannot be empty");
            edtBusinessName.requestFocus();
            return;
        }
        if (category.isEmpty()) {
            edtCategory.setError("Category cannot be empty");
            edtCategory.requestFocus();
            return;
        }
        if (description.isEmpty()) {
            edtDescription.setError("Description cannot be empty");
            edtDescription.requestFocus();
            return;
        } else if (description.length() < 10) {
            edtDescription.setError("Description is too short (min 10 characters)");
            edtDescription.requestFocus();
            return;
        }
        if (phone.isEmpty()) {
            edtPhone.setError("Phone number cannot be empty");
            edtPhone.requestFocus();
            return;
        } else if (!android.util.Patterns.PHONE.matcher(phone).matches()) {
            edtPhone.setError("Please enter a valid phone number");
            edtPhone.requestFocus();
            return;
        }
        if (!website.isEmpty() && !android.util.Patterns.WEB_URL.matcher(website).matches()) {
            edtWebsite.setError("Please enter a valid URL (e.g., https://example.com)");
            edtWebsite.requestFocus();
            return;
        }
        if (address.isEmpty()) {
            edtAddress.setError("Address cannot be empty");
            edtAddress.requestFocus();
            return;
        }

        if (businessId == null) {
            Toast.makeText(this, "Error: User session not found", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> businessData = new HashMap<>();
        businessData.put("businessName", businessName);
        businessData.put("category", category);
        businessData.put("description", description);
        businessData.put("phone", phone);
        businessData.put("website", website);
        businessData.put("address", address);

        db.collection("businesses").document(businessId)
                .set(businessData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}