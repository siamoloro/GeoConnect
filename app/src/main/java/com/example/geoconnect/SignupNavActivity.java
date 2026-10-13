package com.example.geoconnect;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

public class SignupNavActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_nav);

        ImageButton btnBack = findViewById(R.id.btnBack);
        LinearLayout cardCustomer = findViewById(R.id.cardCustomer);
        LinearLayout cardBusiness = findViewById(R.id.cardBusiness);
        LinearLayout cardFreelancer = findViewById(R.id.cardFreelancer);

        // Back button action
        btnBack.setOnClickListener(v -> finish());

        // Navigate to Customer Signup form
        cardCustomer.setOnClickListener(v -> {
            Intent intent = new Intent(SignupNavActivity.this, CustomerSignupActivity.class);
            startActivity(intent);
        });

        // Navigate to Business Signup form
        cardBusiness.setOnClickListener(v -> {
            Intent intent = new Intent(SignupNavActivity.this, BusinessSignupActivity.class);
            startActivity(intent);
        });

        // Navigate to Freelancer Signup form
        cardFreelancer.setOnClickListener(v -> {
            Intent intent = new Intent(SignupNavActivity.this, FreelancerSignupActivity.class);
            startActivity(intent);
        });
    }
}