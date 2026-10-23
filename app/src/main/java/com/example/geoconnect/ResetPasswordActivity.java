package com.example.geoconnect;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private Button btnSendLink;
    private TextView tvBackToSignIn;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etEmail);
        btnSendLink = findViewById(R.id.btnSendLink);
        tvBackToSignIn = findViewById(R.id.tvBackToSignIn);

        btnSendLink.setOnClickListener(v -> sendFirebasePasswordResetEmail());

        tvBackToSignIn.setOnClickListener(v -> finish());
    }

    private void sendFirebasePasswordResetEmail() {
        String email = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        btnSendLink.setEnabled(false);
        btnSendLink.setText("Sending...");

        // Email a secure reset link
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    btnSendLink.setEnabled(true);
                    btnSendLink.setText("Send Reset Link");

                    if (task.isSuccessful()) {
                        Toast.makeText(ResetPasswordActivity.this, "Password reset link sent to your email!", Toast.LENGTH_LONG).show();

                        // Go back to Sign In
                        Intent intent = new Intent(ResetPasswordActivity.this, SigninActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(ResetPasswordActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
});
    }
}