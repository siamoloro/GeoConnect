package com.example.geoconnect;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class AddBusinessOfferingActivity extends AppCompatActivity {

    private TextInputEditText etOfferingName, etPrice, etDescription;
    private MaterialButton btnSaveOffering;
    private Object db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_business_offering);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        //db = FirebaseFirestore.getInstance();

        etOfferingName = findViewById(R.id.etOfferingName);
        etPrice = findViewById(R.id.etPrice);
        etDescription = findViewById(R.id.etDescription);
        btnSaveOffering = findViewById(R.id.btnSaveOffering);




    }
    private void validateAndSave() {
        String name = etOfferingName.getText() != null ? etOfferingName.getText().toString().trim() : "";
        String priceStr = etPrice.getText() != null ? etPrice.getText().toString().trim() : "";
        String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";

        if (name.isEmpty()) {
            etOfferingName.setError("Offering name is required");
            etOfferingName.requestFocus();
            return;
        }

        if (priceStr.isEmpty()) {
            etPrice.setError("Price is required");
            etPrice.requestFocus();
            return;
        }

        double price = Double.parseDouble(priceStr);


    }
}