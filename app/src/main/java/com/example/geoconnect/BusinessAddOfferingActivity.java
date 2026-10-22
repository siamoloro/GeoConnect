package com.example.geoconnect;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class BusinessAddOfferingActivity extends AppCompatActivity {

    private TextInputEditText etOfferingName, etPrice, etDescription;
    private MaterialButton btnSaveOffering;

    // Firebase instances
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_business_add_offering);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            Toast.makeText(this, "User session not found. Please log in again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Views
        etOfferingName = findViewById(R.id.etOfferingName);
        etPrice = findViewById(R.id.etPrice);
        etDescription = findViewById(R.id.etDescription);
        btnSaveOffering = findViewById(R.id.btnSaveOffering);

        // Setup click listener for saving offering
        if (btnSaveOffering != null) {
            btnSaveOffering.setOnClickListener(v -> validateAndSave());
        }
    }

    private void validateAndSave() {
        String name = etOfferingName.getText() != null ? etOfferingName.getText().toString().trim() : "";
        String priceStr = etPrice.getText() != null ? etPrice.getText().toString().trim() : "";
        String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";

        // Validation checks
        if (TextUtils.isEmpty(name)) {
            etOfferingName.setError("Offering name is required");
            etOfferingName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(priceStr)) {
            etPrice.setError("Price is required");
            etPrice.requestFocus();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            etPrice.setError("Please enter a valid price");
            etPrice.requestFocus();
            return;
        }

        // Disable button to prevent multiple submissions
        btnSaveOffering.setEnabled(false);

        // Prepare data map
        Map<String, Object> offeringMap = new HashMap<>();
        offeringMap.put("name", name);
        offeringMap.put("price", price);
        offeringMap.put("description", description);
        offeringMap.put("created_at", Timestamp.now());

        // Save to Firestore subcollection: businesses/{userId}/offerings/{autoId}
        db.collection("businesses").document(userId)
                .collection("offerings")
                .add(offeringMap)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(BusinessAddOfferingActivity.this, "Offering added successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Return back to the previous screen
                })
                .addOnFailureListener(e -> {
                    btnSaveOffering.setEnabled(true);
                    Toast.makeText(BusinessAddOfferingActivity.this, "Failed to save offering: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}