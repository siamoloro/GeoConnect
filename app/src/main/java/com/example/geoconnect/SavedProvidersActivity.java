package com.example.geoconnect;


import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SavedProvidersActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;

    private LinearLayout savedProvidersContainer;
    private TextView savedEmptyMessage;

    private int pendingLoads;
    private int displayedCount;
    private boolean loadFailed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_providers);

        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        savedProvidersContainer =
                findViewById(R.id.savedProvidersContainer);

        savedEmptyMessage =
                findViewById(R.id.savedEmptyMessage);

        findViewById(R.id.savedBackButton)
                .setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSavedProviders();
    }

    private void loadSavedProviders() {

        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user == null) {
            savedEmptyMessage.setText("Please log in to view saved providers.");
            savedEmptyMessage.setVisibility(View.VISIBLE);
            savedProvidersContainer.removeAllViews();
            return;
        }

        savedProvidersContainer.removeAllViews();
        savedEmptyMessage.setVisibility(View.VISIBLE);
        savedEmptyMessage.setText("Loading saved providers...");

        firestore.collection("customers")
                .document(user.getUid())
                .collection("savedProviders")
                .get()
                .addOnSuccessListener(snapshot -> {

                    pendingLoads = snapshot.size();
                    displayedCount = 0;
                    loadFailed = false;

                    if (pendingLoads == 0) {
                        savedEmptyMessage.setText(
                                "You haven't saved any providers yet."
                        );
                        return;
                    }

                    for (DocumentSnapshot saved : snapshot.getDocuments()) {

                        String providerId = saved.getString("providerId");
                        String providerType = saved.getString("providerType");

                        if (providerId == null || providerType == null ||
                                providerId.isEmpty()) {
                            finishOneLoad();
                            continue;
                        }

                        String collection;

                        if (providerType.equalsIgnoreCase("Business")) {
                            collection = "businesses";
                        } else if (providerType.equalsIgnoreCase("Freelancer")) {
                            collection = "freelancers";
                        } else {
                            finishOneLoad();
                            continue;
                        }

                        firestore.collection(collection)
                                .document(providerId)
                                .get()
                                .addOnSuccessListener(provider -> {

                                    if (provider.exists()) {
                                        addSavedCard(
                                                provider,
                                                providerType,
                                                saved.getId()
                                        );
                                        displayedCount++;
                                    }

                                    finishOneLoad();
                                })
                                .addOnFailureListener(e -> {
                                    loadFailed = true;
                                    finishOneLoad();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    savedEmptyMessage.setVisibility(View.VISIBLE);
                    savedEmptyMessage.setText(
                            "Unable to load saved providers. Please try again."
                    );
                });
    }

    private void finishOneLoad() {
        pendingLoads--;

        if (pendingLoads == 0) {
            if (displayedCount > 0) {
                savedEmptyMessage.setVisibility(View.GONE);
            } else {
                savedEmptyMessage.setVisibility(View.VISIBLE);
                savedEmptyMessage.setText(
                        loadFailed
                                ? "Unable to load some saved providers."
                                : "No available saved providers found."
                );
            }

            if (loadFailed && displayedCount > 0) {
                Toast.makeText(
                        this,
                        "Some saved providers could not be loaded",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }

    private String getValue(DocumentSnapshot doc, String field) {
        String value = doc.getString(field);
        return value == null ? "" : value;
    }

    private void addSavedCard(
            DocumentSnapshot provider,
            String type,
            String savedDocumentId) {

        String name = getValue(provider, "name");

        if (name.isEmpty()) name = getValue(provider, "businessName");
        if (name.isEmpty()) name = getValue(provider, "fullname");
        if (name.isEmpty()) name = getValue(provider, "fullName");
        if (name.isEmpty()) name = "Unnamed provider";

        String category = getValue(provider, "category");

        if (category.isEmpty()) {
            category = getValue(provider, "serviceCategory");
        }

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));

        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.WHITE);
        background.setCornerRadius(dp(14));
        background.setStroke(dp(1), Color.parseColor("#D5E9DE"));

        card.setBackground(background);
        card.setElevation(dp(2));

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.bottomMargin = dp(12);
        card.setLayoutParams(cardParams);

        TextView typeText = new TextView(this);
        typeText.setText(type.toUpperCase());
        typeText.setTextSize(12);
        typeText.setTextColor(Color.parseColor("#176B50"));
        typeText.setTypeface(null, Typeface.BOLD);
        card.addView(typeText);

        TextView nameText = new TextView(this);
        nameText.setText(name);
        nameText.setTextSize(18);
        nameText.setTextColor(Color.parseColor("#173D31"));
        nameText.setTypeface(null, Typeface.BOLD);
        card.addView(nameText);

        TextView categoryText = new TextView(this);
        categoryText.setText(
                category.isEmpty() ? "Category not specified" : category
        );
        categoryText.setTextSize(14);
        categoryText.setTextColor(Color.parseColor("#74867D"));
        card.addView(categoryText);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout.LayoutParams actionsParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        actionsParams.topMargin = dp(14);
        actions.setLayoutParams(actionsParams);

        TextView detailsButton = createAction("View Details", "#176B50");
        TextView removeButton = createAction("Remove", "#74867D");

        LinearLayout.LayoutParams detailsParams =
                new LinearLayout.LayoutParams(0, dp(42), 1);

        detailsParams.rightMargin = dp(8);
        actions.addView(detailsButton, detailsParams);

        actions.addView(
                removeButton,
                new LinearLayout.LayoutParams(0, dp(42), 1)
        );

        final String providerName = name;
        final String providerCategory = category;

        detailsButton.setOnClickListener(v -> {

            Intent intent = new Intent(
                    this,
                    DetailsActivity.class
            );

            intent.putExtra("providerId", provider.getId());
            intent.putExtra("providerType", type);
            intent.putExtra("providerName", providerName);
            intent.putExtra("providerCategory", providerCategory);

            startActivity(intent);
        });

        removeButton.setOnClickListener(v -> {

            FirebaseUser user = firebaseAuth.getCurrentUser();

            if (user == null) return;

            removeButton.setEnabled(false);

            firestore.collection("customers")
                    .document(user.getUid())
                    .collection("savedProviders")
                    .document(savedDocumentId)
                    .delete()
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(
                                this,
                                "Provider removed",
                                Toast.LENGTH_SHORT
                        ).show();

                        loadSavedProviders();
                    })
                    .addOnFailureListener(e -> {
                        removeButton.setEnabled(true);
                        Toast.makeText(
                                this,
                                "Unable to remove provider",
                                Toast.LENGTH_SHORT
                        ).show();
                    });
        });

        card.addView(actions);
        savedProvidersContainer.addView(card);
    }

    private TextView createAction(String text, String color) {

        TextView button = new TextView(this);
        button.setText(text);
        button.setTextColor(Color.WHITE);
        button.setTextSize(14);
        button.setGravity(Gravity.CENTER);

        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.parseColor(color));
        background.setCornerRadius(dp(8));

        button.setBackground(background);
        button.setClickable(true);
        button.setFocusable(true);

        return button;
    }

    private int dp(int value) {
        return (int) (
                value * getResources().getDisplayMetrics().density + 0.5f
        );
    }
}