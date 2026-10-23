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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class BusinessAddOfferingActivity extends AppCompatActivity {

    private TextInputEditText etOfferingName, etPrice, etDescription;
    private MaterialButton btnSaveOffering;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_business_add_offering);
        FreelancerUi.applyInsets(findViewById(android.R.id.content));

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Initializes Firebase instances
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

        // Initializes Views
        etOfferingName = findViewById(R.id.etOfferingName);
        etPrice = findViewById(R.id.etPrice);
        etDescription = findViewById(R.id.etDescription);
        btnSaveOffering = findViewById(R.id.btnSaveOffering);

        if (btnSaveOffering != null) {
            btnSaveOffering.setOnClickListener(v -> validateAndSave());
        }
    }

    private void validateAndSave() {
        String name = etOfferingName.getText() != null ? etOfferingName.getText().toString().trim() : "";
        String priceStr = etPrice.getText() != null ? etPrice.getText().toString().trim() : "";
        String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";

        // User input validation checks
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

        // Prevents multiple submissions
        btnSaveOffering.setEnabled(false);

        // Prepares data map
        Map<String, Object> offeringMap = new HashMap<>();
        offeringMap.put("name", name);
        offeringMap.put("price", price);
        offeringMap.put("description", description);
        offeringMap.put("created_at", Timestamp.now());

        // Saves to Firestore subcollection
        db.collection("businesses").document(userId)
                .collection("offerings")
                .add(offeringMap)
                .addOnSuccessListener(documentReference -> {
                    // Booking and Details screens read a 'services' list on the business doc, so keep it in sync
                    db.collection("businesses").document(userId).update("services", FieldValue.arrayUnion(name));
                    Toast.makeText(BusinessAddOfferingActivity.this, "Offering added successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Return back to the previous screen
                })
                .addOnFailureListener(e -> {
                    btnSaveOffering.setEnabled(true);
                    Toast.makeText(BusinessAddOfferingActivity.this, "Failed to save offering: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}