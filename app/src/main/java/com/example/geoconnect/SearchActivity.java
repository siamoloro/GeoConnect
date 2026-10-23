
package com.example.geoconnect;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchActivity extends AppCompatActivity {

    // FIREBASE
    private FirebaseFirestore firestore;

    // SEARCH COMPONENTS
    private EditText searchInput;
    private EditText locationInput;
    private Spinner categorySpinner;
    private RadioGroup providerTypeGroup;
    private Button searchButton;
    private TextView backButton;

    // RESULTS COMPONENTS
    private TextView resultsCount;
    private LinearLayout providersListContainer;
    private LinearLayout emptyStateContainer;
    private TextView emptyStateTitle;
    private TextView emptyStateMessage;

    // BOTTOM NAVIGATION
    private View navHome;
    private View navMarket;
    private View navProviders;
    private View navProfile;

    // SEARCH REQUEST TRACKING
    private int searchVersion = 0;

    private static class ProviderResult {
        String id;
        String type;
        String name;
        String category;
        String service;
        String location;

        ProviderResult(DocumentSnapshot doc, String type) {
            this.id = doc.getId();
            this.type = type;
            this.name = firstString(doc,
                    "name", "businessName", "fullname");
            this.category = firstString(doc,
                    "category", "serviceCategory");
            this.service = firstString(doc,
                    "service", "serviceName");
            this.location = firstString(doc,
                    "address", "locationName", "city");
        }
    }

    private static String firstString(
            DocumentSnapshot doc, String... fields) {

        for (String field : fields) {
            Object value = doc.get(field);

            if (value instanceof String &&
                    !((String) value).trim().isEmpty()) {
                return ((String) value).trim();
            }
        }

        return "";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search);
        FreelancerUi.applyInsets(findViewById(android.R.id.content));

        firestore = FirebaseFirestore.getInstance();

        // CONNECT SEARCH COMPONENTS
        searchInput = findViewById(R.id.searchInput);
        locationInput = findViewById(R.id.locationInput);
        categorySpinner = findViewById(R.id.categorySpinner);
        providerTypeGroup = findViewById(R.id.providerTypeGroup);
        searchButton = findViewById(R.id.searchButton);
        backButton = findViewById(R.id.backButton);

        // CONNECT RESULTS COMPONENTS
        resultsCount = findViewById(R.id.resultsCount);
        providersListContainer =
                findViewById(R.id.providersListContainer);
        emptyStateContainer =
                findViewById(R.id.emptyStateContainer);
        emptyStateTitle = findViewById(R.id.emptyStateTitle);
        emptyStateMessage = findViewById(R.id.emptyStateMessage);

        // BOTTOM NAVIGATION
        navHome = findViewById(R.id.navHome);
        navMarket = findViewById(R.id.navMarket);
        navProviders = findViewById(R.id.navProviders);
        navProfile = findViewById(R.id.navProfile);

        // BACK BUTTON
        backButton.setOnClickListener(v -> finish());

        // SEARCH BUTTON
        searchButton.setOnClickListener(v -> searchProviders());

        // SEARCH FROM KEYBOARD
        searchInput.setOnEditorActionListener(
                (v, actionId, event) -> {

                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        searchProviders();
                        return true;
                    }

                    return false;
                }
        );

        // BOTTOM NAVIGATION
        navHome.setOnClickListener(v ->
                openPage(CustomerDashboardActivity.class));

        navMarket.setOnClickListener(v ->
                openPage(DiscoverActivity.class));

        navProviders.setOnClickListener(v -> {
            // Already on Search
        });

        navProfile.setOnClickListener(v ->
                openPage(CustomerProfileActivity.class));

        // OPTIONAL PROVIDER FILTER FROM PREVIOUS PAGE
        String initialFilter =
                getIntent().getStringExtra("providerFilter");

        if ("BUSINESS".equalsIgnoreCase(initialFilter)) {
            providerTypeGroup.check(R.id.businessesRadio);

        } else if ("FREELANCER".equalsIgnoreCase(initialFilter)) {
            providerTypeGroup.check(R.id.freelancersRadio);

        } else {
            providerTypeGroup.check(R.id.allProvidersRadio);
        }

        showEmptyState();
    }

    // SEARCH FIRESTORE
    private void searchProviders() {

        String query = searchInput.getText()
                .toString().trim().toLowerCase(Locale.ROOT);

        String locationQuery = locationInput.getText()
                .toString().trim().toLowerCase(Locale.ROOT);

        Object selectedCategory = categorySpinner.getSelectedItem();

        String category = selectedCategory == null
                ? ""
                : selectedCategory.toString().trim();

        int selectedType =
                providerTypeGroup.getCheckedRadioButtonId();

        boolean includeBusinesses =
                selectedType != R.id.freelancersRadio;

        boolean includeFreelancers =
                selectedType != R.id.businessesRadio;

        int version = ++searchVersion;

        searchButton.setEnabled(false);
        showState("Searching", "Loading providers...");

        List<ProviderResult> results = new ArrayList<>();

        int requestCount =
                (includeBusinesses ? 1 : 0) +
                        (includeFreelancers ? 1 : 0);

        final int[] completed = {0};
        final boolean[] failed = {false};

        Runnable finishRequest = () -> {

            completed[0]++;

            if (completed[0] == requestCount &&
                    version == searchVersion) {

                searchButton.setEnabled(true);

                displayResults(results, query,
                        locationQuery, category, failed[0]);
            }
        };

        if (includeBusinesses) {

            firestore.collection("businesses")
                    .get()
                    .addOnSuccessListener(snapshot -> {

                        if (version != searchVersion) return;

                        for (DocumentSnapshot doc :
                                snapshot.getDocuments()) {

                            results.add(new ProviderResult(
                                    doc, "Business"));
                        }

                        finishRequest.run();
                    })
                    .addOnFailureListener(e -> {

                        if (version != searchVersion) return;

                        failed[0] = true;
                        finishRequest.run();
                    });
        }

        if (includeFreelancers) {

            firestore.collection("freelancers")
                    .get()
                    .addOnSuccessListener(snapshot -> {

                        if (version != searchVersion) return;

                        for (DocumentSnapshot doc :
                                snapshot.getDocuments()) {

                            results.add(new ProviderResult(
                                    doc, "Freelancer"));
                        }

                        finishRequest.run();
                    })
                    .addOnFailureListener(e -> {

                        if (version != searchVersion) return;

                        failed[0] = true;
                        finishRequest.run();
                    });
        }
    }

    // APPLY FILTERS AND DISPLAY RESULTS
    private void displayResults(
            List<ProviderResult> providers,
            String query,
            String locationQuery,
            String category,
            boolean failed) {

        providersListContainer.removeAllViews();

        int matches = 0;

        for (ProviderResult provider : providers) {

            String searchableText =
                    (provider.name + " " +
                            provider.category + " " +
                            provider.service)
                            .toLowerCase(Locale.ROOT);

            if (!searchableText.contains(query)) {
                continue;
            }

            if (!provider.location.toLowerCase(Locale.ROOT)
                    .contains(locationQuery)) {
                continue;
            }

            if (!category.isEmpty() &&
                    !category.equalsIgnoreCase("All") &&
                    !category.equalsIgnoreCase("All Categories") &&
                    !category.equalsIgnoreCase("Select your service") &&
                    !category.equalsIgnoreCase("Any Category")) {

                String providerCategories =
                        (provider.category + " " + provider.service)
                                .toLowerCase(Locale.ROOT);

                if (!providerCategories.contains(
                        category.toLowerCase(Locale.ROOT))) {
                    continue;
                }
            }

            if (provider.name.isEmpty()) {
                continue;
            }

            addProviderCard(provider);
            matches++;
        }

        resultsCount.setText(
                matches + (matches == 1
                        ? " provider found"
                        : " providers found")
        );

        if (matches == 0) {

            showState(
                    failed ? "Search incomplete" : "No providers found",
                    failed
                            ? "Some providers could not be loaded. Please try again."
                            : "Try changing your search or filters."
            );

        } else if (failed) {

            Toast.makeText(this,
                    "Some providers could not be loaded",
                    Toast.LENGTH_SHORT).show();
        }
    }

    // CREATE A CLICKABLE PROVIDER RESULT
    private void addProviderCard(ProviderResult provider) {

        TextView card = new TextView(this);

        String details =
                provider.name + "\n" +
                        provider.type +
                        (provider.category.isEmpty()
                                ? ""
                                : "\nCategory: " + provider.category) +
                        (provider.location.isEmpty()
                                ? ""
                                : "\nLocation: " + provider.location);

        card.setText(details);
        card.setTextSize(16);
        card.setPadding(28, 24, 28, 24);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.bottomMargin = 16;
        card.setLayoutParams(params);

        card.setOnClickListener(v -> {

            Intent intent = new Intent(
                    SearchActivity.this,
                    DetailsActivity.class
            );

            intent.putExtra("providerId", provider.id);
            intent.putExtra("providerName", provider.name);
            intent.putExtra("providerCategory", provider.category);
            intent.putExtra("providerType", provider.type);

            startActivity(intent);
        });

        providersListContainer.addView(card);
    }

    // SHOW EMPTY, LOADING OR ERROR STATE
    private void showState(String title, String message) {

        providersListContainer.removeAllViews();

        // Detach from previous parent if necessary
        if (emptyStateContainer.getParent() instanceof
                android.view.ViewGroup) {

            ((android.view.ViewGroup)
                    emptyStateContainer.getParent())
                    .removeView(emptyStateContainer);
        }

        providersListContainer.addView(emptyStateContainer);

        emptyStateContainer.setVisibility(View.VISIBLE);
        emptyStateTitle.setText(title);
        emptyStateMessage.setText(message);
    }

    // INITIAL EMPTY STATE
    private void showEmptyState() {

        resultsCount.setText("");

        showState(
                "Find providers",
                "Search for businesses or freelancers using " +
                        "a name, service, category or location."
        );
    }

    // OPEN ANOTHER ACTIVITY
    private void openPage(Class<?> destination) {

        Intent intent = new Intent(this, destination);
        startActivity(intent);
        finish();
    }
}