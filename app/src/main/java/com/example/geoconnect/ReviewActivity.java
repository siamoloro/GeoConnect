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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

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

        // FRONTEND ONLY - NO DATABASE SUBMISSION YET
        Toast.makeText(
                this,
                "Review validated. Submission will be available when the backend is connected.",
                Toast.LENGTH_LONG
        ).show();
    }
}