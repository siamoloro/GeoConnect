package com.example.geoconnect;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class ChangePasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_password);
        FreelancerUi.applyInsets(findViewById(R.id.main));
        FreelancerUi.setupBack(this);

        EditText etCurrent = findViewById(R.id.etCurrentPassword);
        EditText etNew = findViewById(R.id.etNewPassword);
        EditText etConfirm = findViewById(R.id.etConfirmPassword);
        Button btnUpdate = findViewById(R.id.btnUpdatePassword);

        btnUpdate.setOnClickListener(v -> {
            String current = etCurrent.getText().toString();
            String newPass = etNew.getText().toString();
            String confrim = etConfirm.getText().toString();

            if (current.isEmpty()) {
                etCurrent.setError("Enter your current password");
                etCurrent.requestFocus();
                return;
            }
            if (newPass.length() < 8) {
                etNew.setError("Password must be at least 8 characters");
                etNew.requestFocus();
                return;
            }
            if (!newPass.equals(confrim)) {
                etConfirm.setError("Passwords do not match");
                etConfirm.requestFocus();
                return;
            }
            //TODO: send the current and new password to the backend/API
            Toast.makeText(this, "Password updated", Toast.LENGTH_SHORT).show();
            finish();
        });

        findViewById(R.id.tvCancel).setOnClickListener(v -> finish());

    }
}