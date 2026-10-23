package com.example.geoconnect;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Map;

public class ReviewActivity extends AppCompatActivity {

    private ImageView businessImage;
    private TextView businessName;
    private TextView businessCategory;
    private TextView bookingStatus;
    private RatingBar reviewRatingBar;
    private TextView ratingDescription;

    private CheckBox tagCommunication;
    private CheckBox tagOnTime;
    private CheckBox tagQuality;
    private CheckBox tagFriendly;

    private EditText reviewCommentInput;
    private Button submitReviewButton;

    private String providerId;
    private String providerType;
    private String bookingId;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // CONNECT XML VIEWS
        businessImage = findViewById(R.id.businessImage);
        businessName = findViewById(R.id.businessName);
        businessCategory = findViewById(R.id.businessCategory);
        bookingStatus = findViewById(R.id.bookingStatus);

        reviewRatingBar = findViewById(R.id.reviewRatingBar);
        ratingDescription = findViewById(R.id.ratingDescription);

        tagCommunication = findViewById(R.id.tagCommunication);
        tagOnTime = findViewById(R.id.tagOnTime);
        tagQuality = findViewById(R.id.tagQuality);
        tagFriendly = findViewById(R.id.tagFriendly);

        reviewCommentInput = findViewById(R.id.reviewCommentInput);
        submitReviewButton = findViewById(R.id.submitReviewButton);

        // RECEIVE PROVIDER AND BOOKING INFORMATION
        Intent intent = getIntent();

        providerId = intent.getStringExtra("providerId");
        providerType = intent.getStringExtra("providerType");
        bookingId = intent.getStringExtra("bookingId");

        String selectedName = intent.getStringExtra("providerName");
        String selectedCategory = intent.getStringExtra("providerCategory");
        String selectedStatus = intent.getStringExtra("bookingStatus");

        if (selectedName != null && !selectedName.isEmpty()) {
            businessName.setText(selectedName);
        }

        if (selectedCategory != null && !selectedCategory.isEmpty()) {
            businessCategory.setText(selectedCategory);
        }

        if (selectedStatus != null && !selectedStatus.isEmpty()) {
            bookingStatus.setText(selectedStatus);
        } else {
            bookingStatus.setText("Booking status unavailable");
        }

        // RATING DESCRIPTION
        reviewRatingBar.setOnRatingBarChangeListener(
                (ratingBar, rating, fromUser) -> {

                    switch ((int) rating) {
                        case 1:
                            ratingDescription.setText("Poor");
                            break;

                        case 2:
                            ratingDescription.setText("Fair");
                            break;

                        case 3:
                            ratingDescription.setText("Good");
                            break;

                        case 4:
                            ratingDescription.setText("Very good");
                            break;

                        case 5:
                            ratingDescription.setText("Excellent");
                            break;

                        default:
                            ratingDescription.setText("Select your rating");
                    }
                }
        );

        // SUBMIT REVIEW
        submitReviewButton.setOnClickListener(v -> validateReview());
    }

    private void validateReview() {

        float rating = reviewRatingBar.getRating();

        String comment = reviewCommentInput
                .getText().toString().trim();

        // CHECK RATING
        if (rating == 0) {
            Toast.makeText(
                    this,
                    "Please select a star rating",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        // CHECK COMMENT
        if (comment.isEmpty()) {
            reviewCommentInput.setError(
                    "Please write a review"
            );
            reviewCommentInput.requestFocus();
            return;
        }

        // COLLECT SELECTED TAGS
        ArrayList<String> selectedTags = new ArrayList<>();

        if (tagCommunication.isChecked()) {
            selectedTags.add("Great communication");
        }

        if (tagOnTime.isChecked()) {
            selectedTags.add("On time");
        }

        if (tagQuality.isChecked()) {
            selectedTags.add("Quality work");
        }

        if (tagFriendly.isChecked()) {
            selectedTags.add("Friendly and professional");
        }

        submitReview(rating, comment, selectedTags);
    }
    private void submitReview(
            float rating,
            String comment,
            ArrayList<String> selectedTags) {

        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this,
                    "Please log in to submit a review",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (bookingId == null || bookingId.trim().isEmpty()) {
            Toast.makeText(this,
                    "Booking information is missing",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (providerId == null || providerId.trim().isEmpty()
                || providerType == null || providerType.trim().isEmpty()) {
            Toast.makeText(this,
                    "Provider information is missing",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        submitReviewButton.setEnabled(false);

        firestore.collection("booking")
                .document(bookingId)
                .get()
                .addOnSuccessListener(booking -> {

                    if (!booking.exists()
                            || !user.getUid().equals(
                            booking.getString("customerID"))
                            || !"Completed".equalsIgnoreCase(
                            booking.getString("status"))
                            || !providerId.equals(
                            booking.getString("providerId"))
                            || !providerType.equalsIgnoreCase(
                            booking.getString("providerType"))) {

                        submitReviewButton.setEnabled(true);

                        Toast.makeText(this,
                                "Only your completed bookings can be reviewed",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    Map<String, Object> review = new HashMap<>();

                    review.put("customerID", user.getUid());
                    review.put("providerId", providerId);
                    review.put("providerType", providerType);
                    review.put("bookingId", bookingId);
                    review.put("rating", rating);
                    review.put("comment", comment);
                    review.put("tags", selectedTags);
                    review.put("createdAt", FieldValue.serverTimestamp());

                    // One review document per booking
                    firestore.collection("reviews")
                            .document(bookingId)
                            .set(review)
                            .addOnSuccessListener(unused -> {

                                Toast.makeText(
                                        ReviewActivity.this,
                                        "Review submitted successfully!",
                                        Toast.LENGTH_LONG
                                ).show();

                                finish();
                            })
                            .addOnFailureListener(e -> {

                                submitReviewButton.setEnabled(true);

                                Toast.makeText(
                                        ReviewActivity.this,
                                        "Unable to submit review: " + e.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            });
                })
                .addOnFailureListener(e -> {

                    submitReviewButton.setEnabled(true);

                    Toast.makeText(this,
                            "Unable to verify booking: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}