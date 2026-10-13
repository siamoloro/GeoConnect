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

    private EditText etBusinessName, etAddress, etContactNumber, etBusinessEmail, etAbout;
    private AutoCompleteTextView spCategory;
    private TextView txtErrorMessage;
    private MaterialButton btnRegister;
    private ImageButton btnBack;

    // Standard business categories
    private static final String[] CATEGORIES = new String[]{
            "Retail & Shopping", "Food & Beverage", "Home Services",
            "Health & Beauty", "Professional Services", "Automotive", "Other"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_signup);

        // Bind Views
        btnBack = findViewById(R.id.btnBack);
        etBusinessName = findViewById(R.id.etBusinessName);
        spCategory = findViewById(R.id.spCategory);
        etAddress = findViewById(R.id.etAddress);
        etContactNumber = findViewById(R.id.etContactNumber);
        etBusinessEmail = findViewById(R.id.etBusinessEmail);
        etAbout = findViewById(R.id.etAbout);
        txtErrorMessage = findViewById(R.id.txtErrorMessage);
        btnRegister = findViewById(R.id.btnRegister);

        // Populate Category Dropdown
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                CATEGORIES
        );
        spCategory.setAdapter(adapter);

        // Back button action
        btnBack.setOnClickListener(v -> finish());

        // Submission logic
        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(BusinessSignupActivity.this, BusinessDashboardActivity.class);
            startActivity(intent);
        });
    }
}