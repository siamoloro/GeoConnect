package com.example.geoconnect;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class BusinessSignupActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView txtErrorMessage;
    private EditText etBusinessName;
    private AutoCompleteTextView spCategory;
    private EditText etAbout;
    private EditText etAddress;
    private EditText etBusinessEmail;
    private EditText etContactNumber;
    private EditText etWebsite;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private MaterialButton btnRegister;


    // Standard business categories
    private static final String[] CATEGORIES = new String[]{
            "Retail & Shopping", "Food & Beverage", "Home Services",
            "Health & Beauty", "Professional Services", "Automotive", "Other"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_signup);

        btnBack = findViewById(R.id.btnBack);
        txtErrorMessage = findViewById(R.id.txtErrorMessage);
        etBusinessName = findViewById(R.id.etBusinessName);
        spCategory = findViewById(R.id.spCategory);
        etAbout = findViewById(R.id.etAbout);
        etAddress = findViewById(R.id.etAddress);
        etBusinessEmail = findViewById(R.id.etBusinessEmail);
        etContactNumber = findViewById(R.id.etContactNumber);
        etWebsite = findViewById(R.id.etWebsite);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);

        String[] categories = new String[]{
                "Retail & Shopping",
                "Food & Dining",
                "Services",
                "Health & Wellness",
                "Technology & IT",
                "Education & Training",
                "Automotive"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                categories
        );
        spCategory.setAdapter(adapter);


        btnRegister.setOnClickListener(v -> {
            ExtractData();
            Intent intent = new Intent(BusinessSignupActivity.this, BusinessDashboardActivity.class);
            startActivity(intent);
        });

    }
    private void ExtractData() {
        String businessName = etBusinessName.getText().toString().trim();
        String category = spCategory.getText().toString().trim();
        String description = etAbout.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String email = etBusinessEmail.getText().toString().trim();
        String phone = etContactNumber.getText().toString().trim();
        String website = etWebsite.getText().toString().trim(); // Optional
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
    }
}