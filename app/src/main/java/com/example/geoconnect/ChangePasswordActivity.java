package com.example.geoconnect;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_password);
        FreelancerUi.applyInsets(findViewById(R.id.main));
        FreelancerUi.setupBack(this);

        mAuth = FirebaseAuth.getInstance();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        EditText etCurrent = findViewById(R.id.etCurrentPassword);
        EditText etNew = findViewById(R.id.etNewPassword);
        EditText etConfirm = findViewById(R.id.etConfirmPassword);
        Button btnUpdate = findViewById(R.id.btnUpdatePassword);

        btnUpdate.setOnClickListener(v -> {
            String current = etCurrent.getText().toString().trim();
            String newPass = etNew.getText().toString().trim();
            String confirm = etConfirm.getText().toString().trim();

            if (current.isEmpty()) {
                etCurrent.setError("Enter your current password");
                etCurrent.requestFocus();
                return;
            }
            if (newPass.length() < 6) { // Firebase requires a minimum of 6 characters
                etNew.setError("Password must be at least 6 characters");
                etNew.requestFocus();
                return;
            }
            if (!newPass.equals(confirm)) {
                etConfirm.setError("Passwords do not match");
                etConfirm.requestFocus();
                return;
            }

            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null && user.getEmail() != null) {
                // Disable button to prevent double-clicks
                btnUpdate.setEnabled(false);

                // Step 1: Re-authenticate user with current password
                AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), current);

                user.reauthenticate(credential).addOnCompleteListener(reauthTask -> {
                    if (reauthTask.isSuccessful()) {
                        // Step 2: Update to the new password
                        user.updatePassword(newPass).addOnCompleteListener(updateTask -> {
                            btnUpdate.setEnabled(true);
                            if (updateTask.isSuccessful()) {
                                Toast.makeText(ChangePasswordActivity.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(ChangePasswordActivity.this, "Failed to update password: " + updateTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        btnUpdate.setEnabled(true);
                        etCurrent.setError("Incorrect current password");
                        etCurrent.requestFocus();
                        Toast.makeText(ChangePasswordActivity.this, "Authentication failed. Check your current password.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, "No active user session found.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}