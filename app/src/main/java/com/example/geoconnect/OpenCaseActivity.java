package com.example.geoconnect;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OpenCaseActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private Spinner spCaseType;
    private EditText etSubject, etDescription;
    private Button btnSubmitCase;
    private TextView tvBack, tvCancel, tvAttach;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_case);

        // Initializes Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Binds fields from your XML
        spCaseType = findViewById(R.id.spCaseType);
        etSubject = findViewById(R.id.etSubject);
        etDescription = findViewById(R.id.etDescription);
        btnSubmitCase = findViewById(R.id.btnSubmitCase);
        tvBack = findViewById(R.id.tvBack);
        tvCancel = findViewById(R.id.tvCancel);
        tvAttach = findViewById(R.id.tvAttach);

        // Back and Cancel actions
        tvBack.setOnClickListener(v -> finish());
        tvCancel.setOnClickListener(v -> finish());

        // Submits Case Button Logic
        btnSubmitCase.setOnClickListener(v -> {
            String caseType = spCaseType.getSelectedItem() != null ? spCaseType.getSelectedItem().toString() : "";
            String subject = etSubject.getText().toString().trim();
            String description = etDescription.getText().toString().trim();

            if (subject.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Gets currently logged-in user's email
            FirebaseUser currentUser = mAuth.getCurrentUser();
            String userEmail = (currentUser != null && currentUser.getEmail() != null)
                    ? currentUser.getEmail()
                    : "Anonymous";

            // Generates unique identifiers
            String caseId = UUID.randomUUID().toString();
            long timestamp = System.currentTimeMillis();

            // Constructs payload
            Map<String, Object> reportData = new HashMap<>();
            reportData.put("caseId", caseId);
            reportData.put("email", userEmail);
            reportData.put("title", "[" + caseType + "] " + subject);
            reportData.put("reason", description);
            reportData.put("status", "Pending");
            reportData.put("timestamp", timestamp);

            db.collection("reports")
                    .document(caseId)
                    .set(reportData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Case submitted successfully", Toast.LENGTH_SHORT).show();
                        finish(); // Return to previous screen
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });
    }
}