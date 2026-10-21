package com.example.geoconnect;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class FreelancerEditProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_freelancer_edit_profile);
        FreelancerUi.applyInsets(findViewById(R.id.main));
        FreelancerUi.setupBack(this);

        EditText etFullName = findViewById(R.id.etFullName);
        EditText etPhone = findViewById(R.id.etPhone);
        Spinner spCategory = findViewById(R.id.spCategory);
        EditText etRate = findViewById(R.id.etRate);
        EditText etServiceArea = findViewById(R.id.etServiceArea);
        EditText etBio = findViewById(R.id.etBio);
        Button btnSave = findViewById(R.id.btnSaveChanges);

        //TODO: load the freelancer's saved details from the backend (simple values for now)
        etFullName.setText("Alex Khumalo");
        etPhone.setText("+27 00 000 0000");
        spCategory.setSelection(1);
        etRate.setText("450");
        etServiceArea.setText("Johannesburg, within 20km");
        etBio.setText("Portrait and event photographer with 4 years of experience.");

        findViewById(R.id.tvChangePhoto).setOnClickListener(v -> {
            //TODO: open the image picker and upload the photo
            Toast.makeText(this, "Photo upload coming soon", Toast.LENGTH_SHORT).show();
        });
        btnSave.setOnClickListener(v -> {
            String name = etFullName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String rate = etRate.getText().toString().trim();

            if (name.isEmpty()) {
                etFullName.setError("Enter your full name");
                etFullName.requestFocus();
                return;
            }
            if (phone.length() < 9) {
                etPhone.setError("Enter a valid phone number");
                etPhone.requestFocus();
                return;
            }
            if (spCategory.getSelectedItemPosition() == 0) {
                Toast.makeText(this, "Please select your service category", Toast.LENGTH_SHORT).show();
                return;
            }
            if (rate.isEmpty() || !TextUtils.isDigitsOnly(rate) || Integer.parseInt(rate) <= 0) {
                etRate.setError("Enter a valid hourly rate as a number");
                etRate.requestFocus();
                return;
            }
            //TODO: send the updated profile to the backend/API
            Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
            finish();
        });

        findViewById(R.id.tvCancel).setOnClickListener(v -> finish());
    }
}