package com.example.geoconnect;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import android.content.Intent;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class AdminSigninActivity extends AppCompatActivity {

    FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_signin);

        firestore = FirebaseFirestore.getInstance();

        TextInputEditText etUsername = findViewById(R.id.etUsername);
        TextInputEditText etPassword = findViewById(R.id.etPassword);
        MaterialButton btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {

            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {

                Toast.makeText(
                        AdminSigninActivity.this,
                        "Please enter username and password",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("administrator")
                    .whereEqualTo("userName", username)
                    .whereEqualTo("password", password)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {

                        if (!queryDocumentSnapshots.isEmpty())
                        {
                            Toast.makeText(
                                    AdminSigninActivity.this,
                                    "Login Successful",
                                    Toast.LENGTH_SHORT
                            ).show();

                            startActivity(
                                    new Intent(
                                            AdminSigninActivity.this,
                                            AdminDashboardActivity.class));

                            finish();
                        }
                        else
                        {
                            Toast.makeText(
                                    AdminSigninActivity.this,
                                    "Invalid Username or Password",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });
        });


    }
}