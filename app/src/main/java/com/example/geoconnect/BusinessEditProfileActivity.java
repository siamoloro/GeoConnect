package com.example.geoconnect;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

public class BusinessEditProfileActivity extends AppCompatActivity{

    private TextInputEditText edtBusinessName, edtCategory, edtDescription, edtPhone, edtWebsite, edtAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_business_edit_profile);
        FreelancerUi.applyInsets(findViewById(android.R.id.content));

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        edtBusinessName = findViewById(R.id.edtBusinessName);
        edtCategory = findViewById(R.id.edtCategory);
        edtDescription = findViewById(R.id.edtDescription);
        edtPhone = findViewById(R.id.edtPhone);
        edtWebsite = findViewById(R.id.edtWebsite);
        edtAddress = findViewById(R.id.edtAddress);

        loadDataFromDatabase();
    }

    private void loadDataFromDatabase() {
        String dbName = "North & Pine Studio";
        String dbCategory = "Home styling & interiors";
        String dbDescription = "Specializing in modern minimalist interior design.";
        String dbPhone = "+27 12 345 6789";
        String dbWebsite = "https://www.northandpine.com";
        String dbAddress = "Menlyn, Pretoria";

        edtBusinessName.setText(dbName);
        edtCategory.setText(dbCategory);
        edtDescription.setText(dbDescription);
        edtPhone.setText(dbPhone);
        edtWebsite.setText(dbWebsite);
        edtAddress.setText(dbAddress);
    }

    private void extractUserInputsAndSave() {
        String businessName = edtBusinessName.getText().toString().trim();
        String category     = edtCategory.getText().toString().trim();
        String description  = edtDescription.getText().toString().trim();
        String phone        = edtPhone.getText().toString().trim();
        String website      = edtWebsite.getText().toString().trim();
        String address      = edtAddress.getText().toString().trim();
        if (businessName.isEmpty()) {
            edtBusinessName.setError("Business name cannot be empty");
            edtBusinessName.requestFocus();
            return;
        }
    }
}