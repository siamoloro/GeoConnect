
package com.example.geoconnect;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class CustomerProfileActivity extends AppCompatActivity {

    // FIREBASE
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    // PROFILE COMPONENTS
    private ImageView profileImage;
    private TextView customerName;

    // PROFILE BUTTONS
    private View settingsButton;
    private View editProfileButton;
    private View myBookingsCard;
    private View savedCard;

    // BOTTOM NAVIGATION
    private View navHome;
    private View navMarket;
    private View navFreelancers;
    private View navProfile;

    // CUSTOMER INFORMATION
    private String currentName;
    private String currentEmail;
    private String currentPhone;

    // RECEIVE UPDATED DETAILS FROM EDIT PROFILE
    private final ActivityResultLauncher<Intent> editProfileLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {

                        if (result.getResultCode() == Activity.RESULT_OK) {

                            // Reload authoritative data from Firestore
                            loadCustomerDetails();

                            Toast.makeText(
                                    this,
                                    "Profile updated",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_profile);
        FreelancerUi.applyInsets(findViewById(android.R.id.content));

        // INITIALIZE FIREBASE
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // CONNECT XML COMPONENTS
        profileImage = findViewById(R.id.profileImage);
        customerName = findViewById(R.id.customerName);

        settingsButton = findViewById(R.id.settingsButton);
        editProfileButton = findViewById(R.id.editProfileButton);
        myBookingsCard = findViewById(R.id.myBookingsCard);
        savedCard = findViewById(R.id.savedCard);

        // BOTTOM NAVIGATION
        navHome = findViewById(R.id.navHome);
        navMarket = findViewById(R.id.navMarket);
        navFreelancers = findViewById(R.id.navFreelancers);
        navProfile = findViewById(R.id.navProfile);

        // PROFILE ICON
        profileImage.setImageResource(R.drawable.ic_person);

        // LOAD CUSTOMER INFORMATION
        loadCustomerDetails();

        // SETTINGS
        settingsButton.setOnClickListener(v -> {

            Intent intent = new Intent(
                    CustomerProfileActivity.this,
                    SettingsActivity.class
            );

            startActivity(intent);
        });

        // EDIT PROFILE
        editProfileButton.setOnClickListener(v -> {

            FirebaseUser user = firebaseAuth.getCurrentUser();

            if (user == null) {
                Toast.makeText(this,
                        "Please log in first",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(
                    CustomerProfileActivity.this,
                    CustomerEditProfileActivity.class
            );

            intent.putExtra("customerName", currentName);
            intent.putExtra("customerEmail", currentEmail);
            intent.putExtra("customerPhone", currentPhone);

            editProfileLauncher.launch(intent);
        });

        // MY BOOKINGS
        myBookingsCard.setOnClickListener(v -> {

            if (firebaseAuth.getCurrentUser() == null) {
                Toast.makeText(this,
                        "Please log in first",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(
                    CustomerProfileActivity.this,
                    MyBookingActivity.class
            );

            startActivity(intent);
        });

        // SAVED PROVIDERS
        savedCard.setOnClickListener(v -> {

            Toast.makeText(
                    this,
                    "Saved Providers page coming soon",
                    Toast.LENGTH_SHORT
            ).show();
        });

        // HOME NAVIGATION
        navHome.setOnClickListener(v -> {

            Intent intent = new Intent(
                    CustomerProfileActivity.this,
                    CustomerDashboardActivity.class
            );

            startActivity(intent);
            finish();
        });

        // MARKET NAVIGATION
        navMarket.setOnClickListener(v -> {
            openPage(DiscoverActivity.class);
        });

        // FREELANCERS NAVIGATION
        navFreelancers.setOnClickListener(v -> {
            openPage(SearchActivity.class);
        });

        // PROFILE NAVIGATION
        navProfile.setOnClickListener(v -> {
            // Already on Profile
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Refresh profile after returning from another page
        if (firebaseAuth != null) {
            loadCustomerDetails();
        }
    }

    // RETRIEVE CUSTOMER DETAILS FROM FIRESTORE
    private void loadCustomerDetails() {

        FirebaseUser firebaseUser =
                firebaseAuth.getCurrentUser();

        if (firebaseUser == null) {
            currentName = null;
            currentEmail = null;
            currentPhone = null;

            displayCustomerDetails();

            Toast.makeText(this,
                    "Please log in to view your profile",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = firebaseUser.getUid();

        currentEmail = firebaseUser.getEmail();

        firestore.collection("customers")
                .document(userId)
                .get()
                .addOnSuccessListener(document -> {

                    if (document.exists()) {

                        currentName =
                                document.getString("fullname");

                        currentPhone =
                                document.getString("phone");

                        displayCustomerDetails();

                    } else {

                        currentName = null;
                        currentPhone = null;

                        displayCustomerDetails();

                        Toast.makeText(this,
                                "Customer profile not found",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(this,
                            "Unable to load profile: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    // DISPLAY CUSTOMER NAME
    private void displayCustomerDetails() {

        if (currentName != null
                && !currentName.trim().isEmpty()) {

            customerName.setText(currentName);

        } else {

            customerName.setText("My Profile");
        }
    }

    // OPEN ANOTHER ACTIVITY
    private void openPage(Class<?> destination) {

        Intent intent = new Intent(this, destination);
        startActivity(intent);
    }
}