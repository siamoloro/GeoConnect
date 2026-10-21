package com.example.geoconnect;

import android.os.Bundle;
import android.content.Intent;
import android.util.Patterns;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;

public class FreelancerSignupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_freelancer_signup);
        FreelancerUi.applyInsets(findViewById(R.id.main));
        FreelancerUi.setupBack(this);

       EditText etFullName = findViewById(R.id.etFullName);
       EditText etEmail = findViewById(R.id.etEmail);
       EditText etPhone = findViewById(R.id.etPhone);
       Spinner spCategory = findViewById(R.id.spCategory);
       EditText etPassword = findViewById(R.id.etPassword);
       CheckBox cbTerms = findViewById(R.id.cbTerms);
       Button btnCreate =  findViewById(R.id.btnCreateAccount);
       TextView tvSignIn = findViewById(R.id.tvSignIn);

       btnCreate.setOnClickListener(v -> {
           String name = etFullName.getText().toString().trim();
           String email = etEmail.getText().toString().trim();
           String phone = etPhone.getText().toString().trim();
           String password = etPassword.getText().toString();

           if (name.isEmpty()) {
               etFullName.setError("Enter your full name");
               etFullName.requestFocus();
               return;
           }
           if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
               etEmail.setError("Enter a valid email address");
               etEmail.requestFocus();
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
           if (password.length() < 8) {
               etPassword.setError("Password must be at least 9 characters");
               etPassword.requestFocus();
               return;
           }
           if (!cbTerms.isChecked()) {
               Toast.makeText(this, "Please accept the Terms of Service", Toast.LENGTH_SHORT).show();
               return;
           }
           //TODO: send name, email, phone, category and password to the backend/API
           Toast.makeText(this, "Account created", Toast.LENGTH_SHORT).show();
           startActivity(new Intent(this, SigninActivity.class));
           finish();
       });

       tvSignIn.setOnClickListener(v -> {
           startActivity(new Intent(this, SigninActivity.class));
           finish();
       });
    }
}