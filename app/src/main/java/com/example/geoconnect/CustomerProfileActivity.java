
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
    private TextView memberSince;
    private TextView bookingCount;

    // PROFILE BUTTONS
    private View settingsButton;
    private View editProfileButton;
    private View myBookingsCard;
    private View savedCard;

    // BOTTOM NAVIGATION
    private View navHome;
    private View navMarket;
    private View navProviders;
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

        setContentView(R.layout.activity_customer_profile);

        // INITIALIZE FIREBASE
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // CONNECT XML COMPONENTS
        profileImage = findViewById(R.id.profileImage);
        customerName = findViewById(R.id.customerName);
        memberSince = findViewById(R.id.memberSince);
        bookingCount = findViewById(R.id.bookingCount);

        settingsButton = findViewById(R.id.settingsButton);
        editProfileButton = findViewById(R.id.editProfileButton);
        myBookingsCard = findViewById(R.id.myBookingsCard);
        savedCard = findViewById(R.id.savedCard);

        // BOTTOM NAVIGATION
        navHome = findViewById(R.id.navHome);
        navMarket = findViewById(R.id.navMarket);
        navProviders = findViewById(R.id.navProviders);
        navProfile = findViewById(R.id.navProfile);


        // PROFILE ICON
        profileImage.setImageResource(R.drawable.ic_person);


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

        //PROVIDER NAVIGATION
        navProviders.setOnClickListener(v -> {
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

        if (firebaseAuth != null) {
            loadCustomerDetails();
            loadMemberSince();
            loadBookingSummary();
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

    private void loadMemberSince() {

        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user == null || user.getMetadata() == null) {
            memberSince.setText("Member since unavailable");
            return;
        }

        long creationTime = user.getMetadata().getCreationTimestamp();

        if (creationTime <= 0) {
            memberSince.setText("Member since unavailable");
            return;
        }

        java.text.SimpleDateFormat dateFormat =
                new java.text.SimpleDateFormat(
                        "MMMM yyyy",
                        java.util.Locale.getDefault()
                );

        String formattedDate = dateFormat.format(
                new java.util.Date(creationTime)
        );

        memberSince.setText("Member since " + formattedDate);
    }
    // LOAD LOGGED-IN CUSTOMER'S TOTAL BOOKINGS
    private void loadBookingSummary() {

        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user == null) {
            bookingCount.setText("Please log in");
            return;
        }

        String customerId = user.getUid();

        bookingCount.setText("Loading bookings...");

        firestore.collection("booking")
                .whereEqualTo("customerID", customerId)
                .get()
                .addOnSuccessListener(snapshot -> {

                    int totalBookings = snapshot.size();

                    if (totalBookings == 0) {
                        bookingCount.setText("No bookings");
                    } else if (totalBookings == 1) {
                        bookingCount.setText("1 booking");
                    } else {
                        bookingCount.setText(
                                totalBookings + " bookings"
                        );
                    }

                })
                .addOnFailureListener(e -> {

                    bookingCount.setText("Bookings unavailable");

                    Toast.makeText(
                            CustomerProfileActivity.this,
                            "Unable to load bookings: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }
}
