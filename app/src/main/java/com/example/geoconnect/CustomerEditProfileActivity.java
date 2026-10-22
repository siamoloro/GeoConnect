
package com.example.geoconnect;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CustomerEditProfileActivity extends AppCompatActivity {

    // FIREBASE
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    // PROFILE COMPONENTS
    private ImageView customerProfileImage;

    private EditText editFirstName;
    private EditText editEmail;
    private EditText editPhone;

    private TextView changePhotoButton;
    private TextView changePasswordButton;

    private Button saveChangesButton;

    private String customerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_customer_edit_profile);

        // INITIALIZE FIREBASE
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // CONNECT XML COMPONENTS
        customerProfileImage =
                findViewById(R.id.customerProfileImage);

        changePhotoButton =
                findViewById(R.id.changePhotoButton);

        editFirstName =
                findViewById(R.id.editFirstName);

        editEmail =
                findViewById(R.id.editEmail);

        editPhone =
                findViewById(R.id.editPhone);

        changePasswordButton =
                findViewById(R.id.changePasswordButton);

        saveChangesButton =
                findViewById(R.id.saveChangesButton);

        // DISPLAY PROFILE ICON
        customerProfileImage.setImageResource(
                R.drawable.ic_person
        );

        // EMAIL IS READ-ONLY FOR NOW
        editEmail.setEnabled(false);

        // LOAD CUSTOMER INFORMATION
        loadCustomerDetails();

        // CHANGE PROFILE PHOTO
        changePhotoButton.setOnClickListener(v -> {

            Toast.makeText(
                    this,
                    "Change profile photo coming soon",
                    Toast.LENGTH_SHORT
            ).show();
        });

        // CHANGE PASSWORD
        changePasswordButton.setOnClickListener(v -> {

            Intent passwordIntent = new Intent(
                    CustomerEditProfileActivity.this,
                    ChangePasswordActivity.class
            );

            startActivity(passwordIntent);
        });

        // SAVE CHANGES
        saveChangesButton.setOnClickListener(v -> saveChanges());
    }

    // LOAD CUSTOMER INFORMATION FROM FIREBASE
    private void loadCustomerDetails() {

        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user == null) {

            Toast.makeText(
                    this,
                    "Please log in first",
                    Toast.LENGTH_LONG
            ).show();

            saveChangesButton.setEnabled(false);
            return;
        }

        customerId = user.getUid();

        String email = user.getEmail();

        editEmail.setText(email != null ? email : "");

        saveChangesButton.setEnabled(false);

        firestore.collection("Customer")
                .document(customerId)
                .get()
                .addOnSuccessListener(document -> {

                    if (!document.exists()) {

                        Toast.makeText(
                                this,
                                "Customer profile not found",
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    String name = document.getString("fullname");
                    String phone = document.getString("phone");

                    editFirstName.setText(
                            name != null ? name : ""
                    );

                    editPhone.setText(
                            phone != null ? phone : ""
                    );

                    saveChangesButton.setEnabled(true);
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "Failed to load profile: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    // VALIDATE AND SAVE CUSTOMER INFORMATION
    private void saveChanges() {

        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user == null ||
                customerId == null ||
                !customerId.equals(user.getUid())) {

            Toast.makeText(
                    this,
                    "Please log in again",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        String name = editFirstName.getText()
                .toString().trim();

        String email = editEmail.getText()
                .toString().trim();

        String phone = editPhone.getText()
                .toString().trim();

        // VALIDATE NAME
        if (name.isEmpty()) {

            editFirstName.setError(
                    "Full name is required"
            );

            editFirstName.requestFocus();
            return;
        }

        // VALIDATE EMAIL
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

            Toast.makeText(
                    this,
                    "Your account email is invalid",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // VALIDATE PHONE NUMBER
        if (!phone.matches("0[6-8][0-9]{8}")) {

            editPhone.setError(
                    "Enter a valid South African mobile number"
            );

            editPhone.requestFocus();
            return;
        }

        // PREPARE FIRESTORE UPDATES
        Map<String, Object> updates = new HashMap<>();

        updates.put("fullname", name);
        updates.put("phone", phone);

        // DISABLE BUTTON WHILE SAVING
        saveChangesButton.setEnabled(false);

        // UPDATE THE EXISTING CUSTOMER DOCUMENT
        firestore.collection("Customer")
                .document(customerId)
                .update(updates)
                .addOnSuccessListener(unused -> {

                    Toast.makeText(
                            this,
                            "Profile updated successfully!",
                            Toast.LENGTH_SHORT
                    ).show();

                    // RETURN UPDATED INFORMATION
                    Intent result = new Intent();

                    result.putExtra("customerName", name);
                    result.putExtra("customerEmail", email);
                    result.putExtra("customerPhone", phone);

                    setResult(Activity.RESULT_OK, result);

                    // RETURN TO CUSTOMER PROFILE
                    finish();
                })
                .addOnFailureListener(e -> {

                    saveChangesButton.setEnabled(true);

                    Toast.makeText(
                            this,
                            "Failed to update profile: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }
}
