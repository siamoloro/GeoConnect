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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SearchActivity extends AppCompatActivity {

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

    // CUSTOMER INFORMATION
    private String customerName;
    private String customerEmail;
    private String customerPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search);

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

        // CONNECT BOTTOM NAVIGATION
        navHome = findViewById(R.id.navHome);
        navMarket = findViewById(R.id.navMarket);
        navProviders = findViewById(R.id.navProviders);
        navProfile = findViewById(R.id.navProfile);

        // RECEIVE CUSTOMER INFORMATION
        Intent receivedIntent = getIntent();

        customerName =
                receivedIntent.getStringExtra("customerName");

        customerEmail =
                receivedIntent.getStringExtra("customerEmail");

        customerPhone =
                receivedIntent.getStringExtra("customerPhone");

        // INITIAL SCREEN
        showEmptyState();

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

        // HOME NAVIGATION
        navHome.setOnClickListener(v -> {
            openPage(CustomerDashboardActivity.class);
        });

        // MARKET NAVIGATION
        navMarket.setOnClickListener(v -> {
            openPage(DiscoverActivity.class);
        });

        // PROVIDERS NAVIGATION
        navProviders.setOnClickListener(v -> {
            // Already on Providers page.
        });

        // PROFILE NAVIGATION
        navProfile.setOnClickListener(v -> {
            openPage(CustomerProfileActivity.class);
        });


        String initialFilter =
                receivedIntent.getStringExtra("providerFilter");

        if ("BUSINESS".equalsIgnoreCase(initialFilter)) {

            providerTypeGroup.check(R.id.businessesRadio);

        } else if ("FREELANCER".equalsIgnoreCase(initialFilter)) {

            providerTypeGroup.check(R.id.freelancersRadio);

        } else {

            providerTypeGroup.check(R.id.allProvidersRadio);
        }
    }

    // SEARCHING FOR PROVIDERS
    private void searchProviders() {

        String searchText = searchInput
                .getText()
                .toString()
                .trim();

        String location = locationInput
                .getText()
                .toString()
                .trim();

        String category = categorySpinner
                .getSelectedItem()
                .toString();

        String providerType = "ALL";

        int selectedRadioId =
                providerTypeGroup.getCheckedRadioButtonId();

        if (selectedRadioId == R.id.businessesRadio) {

            providerType = "BUSINESS";

        } else if (selectedRadioId == R.id.freelancersRadio) {

            providerType = "FREELANCER";
        }

        // CLEAR PREVIOUS RESULTS
        providersListContainer.removeAllViews();

        // Restore the existing empty-state layout.
        // It must be added back because removeAllViews()
        // removes all child views from this container.
        providersListContainer.addView(emptyStateContainer);

        emptyStateContainer.setVisibility(View.VISIBLE);


        resultsCount.setText("");

        // Display the selected search details
        String searchSummary =
                "Search: " +
                        (searchText.isEmpty() ? "Any service" : searchText) +
                        "\nLocation: " +
                        (location.isEmpty() ? "Any location" : location) +
                        "\nCategory: " + category +
                        "\nProvider type: " + providerType;


        if (emptyStateTitle != null) {
            emptyStateTitle.setText("Search ready");
        }

        if (emptyStateMessage != null) {
            emptyStateMessage.setText(
                    searchSummary +
                            "\n\nMatching providers will appear here " +
                            "once the Java API is connected."
            );
        }
    }

    // INITIAL EMPTY STATE
    private void showEmptyState() {

        providersListContainer.setVisibility(View.VISIBLE);
        emptyStateContainer.setVisibility(View.VISIBLE);

        resultsCount.setText("");
    }

    // OPEN ANOTHER ACTIVITY
    private void openPage(Class<?> destination) {

        Intent intent = new Intent(
                SearchActivity.this,
                destination
        );

        addCustomerDetails(intent);

        startActivity(intent);
        finish();
    }

    // PASS CUSTOMER INFORMATION
    private void addCustomerDetails(Intent intent) {

        intent.putExtra("customerName", customerName);
        intent.putExtra("customerEmail", customerEmail);
        intent.putExtra("customerPhone", customerPhone);
    }
}