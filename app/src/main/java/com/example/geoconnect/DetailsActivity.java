
package com.example.geoconnect;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetailsActivity extends AppCompatActivity {

    // FIREBASE
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    // PROVIDER INFORMATION
    private ImageView providerCoverImage;
    private TextView providerName;
    private TextView providerCategory;
    private TextView providerRating;
    private TextView providerDistance;
    private TextView verifiedBadge;
    private TextView providerDescription;

    // SERVICES
    private LinearLayout servicesContainer;
    private TextView noServicesText;

    // BUTTONS
    private ImageView backButton;
    private ImageView saveProviderButton;
    private Button requestBookingButton;
    private Button writeReviewButton;

    // CURRENT PROVIDER
    private String currentProviderId;
    private String currentProviderType;
    private String currentProviderName;
    private String currentProviderCategory;

    // SAVED STATE
    private boolean isSaved = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_details);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // CONNECT XML COMPONENTS
        providerCoverImage =
                findViewById(R.id.providerCoverImage);

        providerName =
                findViewById(R.id.providerName);

        providerCategory =
                findViewById(R.id.providerCategory);

        providerRating =
                findViewById(R.id.providerRating);

        providerDistance =
                findViewById(R.id.providerDistance);

        verifiedBadge =
                findViewById(R.id.verifiedBadge);

        providerDescription =
                findViewById(R.id.providerDescription);

        servicesContainer =
                findViewById(R.id.servicesContainer);

        noServicesText =
                findViewById(R.id.noServicesText);

        backButton =
                findViewById(R.id.backButton);

        saveProviderButton =
                findViewById(R.id.saveProviderButton);

        requestBookingButton =
                findViewById(R.id.requestBookingButton);

        writeReviewButton =
                findViewById(R.id.writeReviewButton);

        // RECEIVE PROVIDER ID AND TYPE
        Intent receivedIntent = getIntent();

        currentProviderId =
                receivedIntent.getStringExtra("providerId");

        currentProviderType =
                receivedIntent.getStringExtra("providerType");

        // DISABLE ACTIONS UNTIL PROVIDER IS VERIFIED
        requestBookingButton.setEnabled(false);
        saveProviderButton.setEnabled(false);

        // BACK BUTTON
        backButton.setOnClickListener(v -> finish());

        // VALIDATE PROVIDER INFORMATION
        if (currentProviderId == null ||
                currentProviderId.trim().isEmpty() ||
                (!"Business".equalsIgnoreCase(currentProviderType)
                        && !"Freelancer".equalsIgnoreCase(currentProviderType))) {

            Toast.makeText(this,
                    "Invalid provider information",
                    Toast.LENGTH_LONG).show();

            return;
        }

        // LOAD ACTUAL PROVIDER DETAILS
        loadProviderDetails();

        // CHECK WHETHER PROVIDER IS SAVED
        checkSavedProvider();

        // SAVE OR UNSAVE PROVIDER
        saveProviderButton.setOnClickListener(v -> toggleSavedProvider());

        // REQUEST BOOKING
        requestBookingButton.setOnClickListener(v -> {

            FirebaseUser user = firebaseAuth.getCurrentUser();

            if (user == null) {
                Toast.makeText(this,
                        "Please log in before booking",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            Intent bookingIntent = new Intent(
                    DetailsActivity.this,
                    BookingsActivity.class
            );

            bookingIntent.putExtra(
                    "providerId", currentProviderId);

            bookingIntent.putExtra(
                    "providerType", currentProviderType);

            bookingIntent.putExtra(
                    "providerName", currentProviderName);

            bookingIntent.putExtra(
                    "providerCategory", currentProviderCategory);

            startActivity(bookingIntent);
        });

        // WRITE REVIEW
        writeReviewButton.setOnClickListener(v -> {

            Toast.makeText(
                    this,
                    "Review page will be connected later",
                    Toast.LENGTH_SHORT
            ).show();
        });
    }

    // RETRIEVE THE SELECTED PROVIDER FROM FIRESTORE
    private void loadProviderDetails() {

        String collectionName =
                "Business".equalsIgnoreCase(currentProviderType)
                        ? "Business"
                        : "Freelancer";

        firestore.collection(collectionName)
                .document(currentProviderId)
                .get()
                .addOnSuccessListener(document -> {

                    if (!document.exists()) {

                        Toast.makeText(this,
                                "Provider not found",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    currentProviderName = firstString(
                            document,
                            "name",
                            "businessName",
                            "fullname"
                    );

                    currentProviderCategory = firstString(
                            document,
                            "category",
                            "serviceCategory",
                            "service"
                    );

                    String description = firstString(
                            document,
                            "description",
                            "bio",
                            "about"
                    );

                    providerName.setText(currentProviderName);
                    providerCategory.setText(currentProviderCategory);
                    providerDescription.setText(description);

                    // SHOW RATING ONLY IF STORED
                    Object ratingValue = document.get("rating");

                    if (ratingValue instanceof Number) {
                        providerRating.setText(
                                String.valueOf(ratingValue)
                        );
                    } else {
                        providerRating.setText("No ratings yet");
                    }

                    // SHOW ADDRESS, NOT AN INVENTED DISTANCE
                    String address = firstString(
                            document,
                            "address",
                            "city",
                            "locationName"
                    );

                    providerDistance.setText(
                            address.isEmpty()
                                    ? "Location not available"
                                    : address
                    );

                    // VERIFIED STATUS
                    boolean verified =
                            Boolean.TRUE.equals(
                                    document.getBoolean("verified")
                            );

                    verifiedBadge.setVisibility(
                            verified ? View.VISIBLE : View.GONE
                    );

                    // DISPLAY SERVICES
                    displayServices(document);

                    requestBookingButton.setEnabled(true);
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(this,
                            "Unable to load provider: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    // DISPLAY SERVICES FROM FIRESTORE
    private void displayServices(DocumentSnapshot document) {

        servicesContainer.removeAllViews();

        Object services = document.get("services");

        int count = 0;

        if (services instanceof List<?>) {

            for (Object item : (List<?>) services) {

                if (!(item instanceof String)) {
                    continue;
                }

                String serviceName = (String) item;

                TextView serviceView = new TextView(this);

                serviceView.setText(serviceName);
                serviceView.setTextSize(16);
                serviceView.setPadding(16, 12, 16, 12);

                servicesContainer.addView(serviceView);
                count++;
            }
        }

        noServicesText.setVisibility(
                count == 0 ? View.VISIBLE : View.GONE
        );
    }

    // GET FIRST AVAILABLE STRING FIELD
    private String firstString(
            DocumentSnapshot document,
            String... fields) {

        for (String field : fields) {

            Object value = document.get(field);

            if (value instanceof String &&
                    !((String) value).trim().isEmpty()) {

                return ((String) value).trim();
            }
        }

        return "";
    }

    // CHECK IF CURRENT CUSTOMER SAVED THIS PROVIDER
    private void checkSavedProvider() {

        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user == null) {
            return;
        }

        String savedId =
                currentProviderType.toLowerCase()
                        + "_" + currentProviderId;

        firestore.collection("Customer")
                .document(user.getUid())
                .collection("SavedProviders")
                .document(savedId)
                .get()
                .addOnSuccessListener(document -> {

                    isSaved = document.exists();
                    updateSavedIcon();
                    saveProviderButton.setEnabled(true);
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(this,
                            "Unable to check saved providers",
                            Toast.LENGTH_SHORT).show();
                });
    }

    // SAVE OR REMOVE PROVIDER
    private void toggleSavedProvider() {

        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user == null) {

            Toast.makeText(this,
                    "Please log in to save providers",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String savedId =
                currentProviderType.toLowerCase()
                        + "_" + currentProviderId;

        saveProviderButton.setEnabled(false);

        if (!isSaved) {

            Map<String, Object> savedProvider = new HashMap<>();

            savedProvider.put("providerId", currentProviderId);
            savedProvider.put("providerType", currentProviderType);
            savedProvider.put("savedAt",
                    com.google.firebase.firestore.FieldValue
                            .serverTimestamp());

            firestore.collection("Customer")
                    .document(user.getUid())
                    .collection("SavedProviders")
                    .document(savedId)
                    .set(savedProvider)
                    .addOnSuccessListener(unused -> {

                        isSaved = true;
                        updateSavedIcon();

                        saveProviderButton.setEnabled(true);

                        Toast.makeText(this,
                                "Provider saved",
                                Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {

                        saveProviderButton.setEnabled(true);

                        Toast.makeText(this,
                                "Unable to save provider",
                                Toast.LENGTH_SHORT).show();
                    });

        } else {

            firestore.collection("Customer")
                    .document(user.getUid())
                    .collection("SavedProviders")
                    .document(savedId)
                    .delete()
                    .addOnSuccessListener(unused -> {

                        isSaved = false;
                        updateSavedIcon();

                        saveProviderButton.setEnabled(true);

                        Toast.makeText(this,
                                "Provider removed from saved",
                                Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {

                        saveProviderButton.setEnabled(true);

                        Toast.makeText(this,
                                "Unable to remove saved provider",
                                Toast.LENGTH_SHORT).show();
                    });
        }
    }

    // UPDATE SAVE BUTTON APPEARANCE
    private void updateSavedIcon() {

        saveProviderButton.setImageResource(
                R.drawable.ic_favorite
        );

        saveProviderButton.setAlpha(
                isSaved ? 1.0f : 0.45f
        );
    }
}
