package com.example.geoconnect;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class OpenCaseActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextInputLayout inputLayoutTitle, inputLayoutDescription;
    private TextInputEditText edtCaseTitle, edtCaseDescription;
    private MaterialButton btnSubmitCase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_case);

        // Bind Views
        toolbar = findViewById(R.id.toolbar);
        inputLayoutTitle = findViewById(R.id.inputLayoutTitle);
        inputLayoutDescription = findViewById(R.id.inputLayoutDescription);
        edtCaseTitle = findViewById(R.id.edtCaseTitle);
        edtCaseDescription = findViewById(R.id.edtCaseDescription);
        btnSubmitCase = findViewById(R.id.btnSubmitCase);

        // Toolbar Navigation Back Button
        toolbar.setNavigationOnClickListener(v -> finish());

        // Submit Case Action
        btnSubmitCase.setOnClickListener(v -> submitCase());
    }

    private void submitCase() {
        String title = edtCaseTitle.getText() != null ? edtCaseTitle.getText().toString().trim() : "";
        String description = edtCaseDescription.getText() != null ? edtCaseDescription.getText().toString().trim() : "";

        // Reset errors
        inputLayoutTitle.setError(null);
        inputLayoutDescription.setError(null);

        // Validation
        if (TextUtils.isEmpty(title)) {
            inputLayoutTitle.setError("Case title is required");
            return;
        }

        if (TextUtils.isEmpty(description)) {
            inputLayoutDescription.setError("Case description is required");
            return;
        }

        if (description.length() < 15) {
            inputLayoutDescription.setError("Please provide at least 15 characters describing the issue");
            return;
        }

        // Action placeholder
        Toast.makeText(this, "Case submitted successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }
}