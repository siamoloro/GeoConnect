package com.example.geoconnect;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SecurityActivity extends AppCompatActivity {

    LinearLayout rowChangePassword, rowActiveSessions;
    Button btnDeleteAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_security);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        rowChangePassword = findViewById(R.id.rowChangePassword);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);

        toolbar.setNavigationOnClickListener(v -> finish());

        rowChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(SecurityActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        });

        btnDeleteAccount.setOnClickListener(v -> {
            // Show a confirmation dialog first to prevent accidental deletions
            new AlertDialog.Builder(this)
                    .setTitle("Delete Account")
                    .setMessage("Are you sure you want to delete your account? This action is permanent and cannot be undone.")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        // User confirmed, proceed with deletion
                        deleteUserAccount();
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
        });
    }

    private void deleteUserAccount() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "No user is currently logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Disable button to prevent multi-clicks during network call
        btnDeleteAccount.setEnabled(false);

        // 1. Fetch the user document to check their type ("customer", "business", etc.)
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String userType = documentSnapshot.getString("type");

                    // 2. Delete from the appropriate role-specific collection
                    com.google.android.gms.tasks.Task<Void> roleDeleteTask;
                    if ("customer".equals(userType)) {
                        roleDeleteTask = db.collection("customers").document(userId).delete();
                    } else if ("business".equals(userType)) {
                        roleDeleteTask = db.collection("businesses").document(userId).delete();
                    } else if ("freelancer".equals(userType)) {
                        roleDeleteTask = db.collection("freelancers").document(userId).delete();
                    } else {
                        // Fallback if no specific role matches
                        roleDeleteTask = com.google.android.gms.tasks.Tasks.forResult(null);
                    }

                    roleDeleteTask.addOnSuccessListener(aVoid -> {
                        // 3. Delete from the general 'users' collection
                        db.collection("users").document(userId).delete()
                                .addOnSuccessListener(aVoid2 -> {

                                    // 4. Finally, delete the account from Firebase Authentication
                                    currentUser.delete().addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(this, "Your account has been completely deleted.", Toast.LENGTH_LONG).show();

                                            // Redirect to SigninActivity and clear stack
                                            Intent intent = new Intent(this, SigninActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            btnDeleteAccount.setEnabled(true);
                                            Toast.makeText(this, "Failed to delete auth profile: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });

                                })
                                .addOnFailureListener(e -> {
                                    btnDeleteAccount.setEnabled(true);
                                    Toast.makeText(this, "Failed to delete user document: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });

                    }).addOnFailureListener(e -> {
                        btnDeleteAccount.setEnabled(true);
                        Toast.makeText(this, "Failed to delete profile data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

                })
                .addOnFailureListener(e -> {
                    btnDeleteAccount.setEnabled(true);
                    Toast.makeText(this, "Failed to fetch user details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}