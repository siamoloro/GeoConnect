package com.example.geoconnect;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FreelancerEditProfileActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private EditText etFullName, etPhone, etRate, etServiceArea, etBio;
    private Spinner spCategory;
    private TextView tvAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_freelancer_edit_profile);
        FreelancerUi.applyInsets(findViewById(R.id.main));
        FreelancerUi.setupBack(this);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        tvAvatar = findViewById(R.id.tvAvatar);
        etFullName = findViewById(R.id.etFullName);
        etPhone = findViewById(R.id.etPhone);
        spCategory = findViewById(R.id.spCategory);
        etRate = findViewById(R.id.etRate);
        etServiceArea = findViewById(R.id.etServiceArea);
        etBio = findViewById(R.id.etBio);
        Button btnSave = findViewById(R.id.btnSaveChanges);

        loadFreelancerProfile();

        findViewById(R.id.tvChangePhoto).setOnClickListener(v ->
                Toast.makeText(this, "Photo upload coming soon", Toast.LENGTH_SHORT).show());
        btnSave.setOnClickListener(v -> saveProfileToFirestore());
        findViewById(R.id.tvCancel).setOnClickListener(v -> finish());
    }

    private String str(DocumentSnapshot d, String field) {
        Object o = d.get(field);
        return o == null ? "" : o.toString();
    }

    private String initialsOf(String name) {
        if (name == null || name.trim().isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        String s = parts[0].substring(0, 1);
        if (parts.length > 1) s += parts[parts.length - 1].substring(0, 1);
        return s.toUpperCase();
    }

    private void loadFreelancerProfile() {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("freelancers")
                .whereEqualTo("userID", userId)
                .limit(1)
                .get()
                .addOnSuccessListener(snap -> {
                    if (snap.isEmpty()) return;
                    DocumentSnapshot d = snap.getDocuments().get(0);

                    String name = str(d, "fullName");
                    etFullName.setText(name);
                    tvAvatar.setText(initialsOf(name));
                    etPhone.setText(str(d, "phone"));
                    etServiceArea.setText(str(d, "serviceArea"));
                    etBio.setText(str(d, "description"));
                    etRate.setText(str(d, "rate"));

                    // Re-select the saved category so Save isn't blocked
                    String saved = str(d, "skills");
                    for (int i = 1; i < spCategory.getCount(); i++) {
                        if (spCategory.getItemAtPosition(i).toString().equals(saved)) {
                            spCategory.setSelection(i);
                            break;
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this,
                        "Failed to load profile: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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

        Map<String, Object> data = new HashMap<>();
        data.put("fullName", name);
        data.put("phone", phone);
        data.put("skills", category);
        data.put("rate", rate);
        data.put("serviceArea", serviceArea);
        data.put("description", bio);
        data.put("userID", userId);

        db.collection("freelancers")
                .whereEqualTo("userID", userId)
                .limit(1)
                .get()
                .addOnSuccessListener(snap -> {
                    if (!snap.isEmpty()) {
                        String docId = snap.getDocuments().get(0).getId();
                        db.collection("Freelancers").document(docId)
                                .update(data)
                                .addOnSuccessListener(a -> {
                                    Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> Toast.makeText(this,
                                        "Error updating: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    } else {
                        data.put("accountStatus", "Active");
                        data.put("approvalStatus", "Pending");
                        data.put("reviewStatus", "Pending");
                        data.put("availability", true);
                        db.collection("Freelancers")
                                .add(data)
                                .addOnSuccessListener(ref -> {
                                    Toast.makeText(this, "Profile created successfully!", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> Toast.makeText(this,
                                        "Error saving: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this,
                        "Database connection error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}