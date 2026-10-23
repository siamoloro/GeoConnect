package com.example.geoconnect;

import android.content.Intent;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcome);
        FreelancerUi.applyInsets(findViewById(android.R.id.content));

        MaterialButton btnCreateAccount = findViewById(R.id.btnCreateAccount);
        MaterialButton btnSignIn = findViewById(R.id.btnSignIn);

        // Navigate to Signup screen when "Create an account" is clicked
        btnCreateAccount.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, SignupNavActivity.class);
            startActivity(intent);
        });

        // Navigate to Signin screen when "I already have an account" is clicked
        btnSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, SigninActivity.class);
            startActivity(intent);
        });
    }
}