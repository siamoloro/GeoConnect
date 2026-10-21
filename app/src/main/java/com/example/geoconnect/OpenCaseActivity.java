package com.example.geoconnect;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class OpenCaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_open_case);
        FreelancerUi.applyInsets(findViewById(R.id.main));
        FreelancerUi.setupBack(this);

        Spinner spCaseType = findViewById(R.id.spCaseType);
        EditText etSubject = findViewById(R.id.etSubject);
        EditText etDescription = findViewById(R.id.etDescription);
        Button btnSubmit = findViewById(R.id.btnSubmitCase);

        findViewById(R.id.tvAttach).setOnClickListener(v -> {
            //TODO: open the file/image picker and attach the file to the case
            Toast.makeText(this, "Attachments coming soon", Toast.LENGTH_SHORT).show();
        });

        btnSubmit.setOnClickListener(v -> {
            String subject = etSubject.getText().toString().trim();
            String description = etDescription.getText().toString().trim();

            if (spCaseType.getSelectedItemPosition() == 0) {
                Toast.makeText(this, "Please select a case type", Toast.LENGTH_SHORT).show();
                return;
            }
            if (subject.isEmpty()) {
                etSubject.setError("Enter a short subject");
                etSubject.requestFocus();
                return;
            }
            if (description.length() < 10) {
                etDescription.setError("Please describe the problem (at least 10 characters)");
                etDescription.requestFocus();
                return;
            }

            //TODO: send the case type, subject, description and attachment to the backend/API
            Toast.makeText(this, "Case submitted. We will reply within 24 hours.", Toast.LENGTH_SHORT).show();
            finish();
        });

        findViewById(R.id.tvCancel).setOnClickListener(v -> finish());

    }
}