package com.example.geoconnect;

import android.os.Bundle;
import android.content.Intent;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class FreelancerProfileActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // Declare all your private TextView variables here
    private TextView tvProfileName, tvProfileCategory, tvProfileBio, tvProfilePhone, tvProfileServiceArea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_freelancer_profile);
        FreelancerUi.applyInsets(findViewById(R.id.main));
        FreelancerUi.setupNav(this, "Profile");

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Bind all TextView variables to their matching IDs in your XML layout
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileCategory = findViewById(R.id.tvProfileCategory);
        tvProfileBio = findViewById(R.id.tvProfileBio);
        tvProfilePhone = findViewById(R.id.tvProfilePhone);
        tvProfileServiceArea = findViewById(R.id.tvProfileServiceArea);

        findViewById(R.id.btnEditProfile).setOnClickListener(v ->
                startActivity(new Intent(this, FreelancerEditProfileActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFreelancerProfile();
    }

    private void loadFreelancerProfile() {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        // Query Firestore 'freelancer' collection matching your ERD relation (userID foreign key)
        db.collection("freelancers")
                .whereEqualTo("userID", userId)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        var document = queryDocumentSnapshots.getDocuments().get(0);

                        String name = document.getString("name");
                        String category = document.getString("category");
                        String serviceArea = document.getString("serviceArea");
                        String bio = document.getString("description");
                        String phone = document.getString("phone");

                        // Dynamically update the UI fields safely
                        if (tvProfileName != null && name != null) {
                            tvProfileName.setText(name);
                        }
                        if (tvProfileCategory != null && category != null) {
                            tvProfileCategory.setText(category);
                        }
                        if (tvProfileServiceArea != null && serviceArea != null) {
                            tvProfileServiceArea.setText(serviceArea);
                        }
                        if (tvProfilePhone != null && phone != null) {
                            tvProfilePhone.setText(phone);
                        }
                        if (tvProfileBio != null && bio != null) {
                            tvProfileBio.setText(bio);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}