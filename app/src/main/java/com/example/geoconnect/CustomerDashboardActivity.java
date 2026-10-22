
package com.example.geoconnect;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CustomerDashboardActivity extends AppCompatActivity {

    // FIREBASE
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    // CUSTOMER INFORMATION
    private String currentName;
    private String currentEmail;
    private String currentPhone;

    // DASHBOARD COMPONENTS
    private TextView customerGreeting;
    private View notificationButton;
    private View localMarketCard;
    private View seeAllProviders;

    // SEARCH COMPONENTS
    private EditText dashboardSearchInput;
    private LinearLayout dashboardSearchResults;
    private LinearLayout dashboardResultsContainer;
    private LinearLayout dashboardContent;
    private TextView dashboardSearchEmpty;
    private RadioGroup dashboardSearchFilters;

    // BOTTOM NAVIGATION
    private View navHome;
    private View navMarket;
    private View navProviders;
    private View navProfile;

    // SEARCH STATE
    private final List<DocumentSnapshot> businesses =
            new ArrayList<>();

    private final List<DocumentSnapshot> freelancers =
            new ArrayList<>();

    private int searchVersion = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_customer_dashboard);

        // INITIALIZE FIREBASE
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // CONNECT DASHBOARD COMPONENTS
        customerGreeting = findViewById(R.id.customerGreeting);
        notificationButton = findViewById(R.id.notificationButton);
        localMarketCard = findViewById(R.id.localMarketCard);
        seeAllProviders = findViewById(R.id.seeAllProviders);

        // CONNECT SEARCH COMPONENTS
        dashboardSearchInput =
                findViewById(R.id.dashboardSearchInput);

        dashboardSearchResults =
                findViewById(R.id.dashboardSearchResults);

        dashboardResultsContainer =
                findViewById(R.id.dashboardResultsContainer);

        dashboardContent =
                findViewById(R.id.dashboardContent);

        dashboardSearchEmpty =
                findViewById(R.id.dashboardSearchEmpty);

        dashboardSearchFilters =
                findViewById(R.id.dashboardSearchFilters);

        // CONNECT BOTTOM NAVIGATION
        navHome = findViewById(R.id.navHome);
        navMarket = findViewById(R.id.navMarket);
        navProviders = findViewById(R.id.navProviders);
        navProfile = findViewById(R.id.navProfile);

        // LOAD LOGGED-IN CUSTOMER
        loadCustomerDetails();

        // NOTIFICATIONS
        notificationButton.setOnClickListener(v -> {
            openPage(NotificationsActivity.class);
        });

        // SEARCH AS CUSTOMER TYPES
        dashboardSearchInput.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count) {

                        String query = s.toString().trim();

                        if (query.isEmpty()) {
                            showNormalDashboard();
                        } else {
                            searchProviders(query);
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                }
        );

        // SEARCH BUTTON ON KEYBOARD
        dashboardSearchInput.setOnEditorActionListener(
                (v, actionId, event) -> {

                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                        String query = dashboardSearchInput
                                .getText()
                                .toString()
                                .trim();

                        if (query.isEmpty()) {
                            showNormalDashboard();
                        } else {
                            searchProviders(query);
                        }

                        return true;
                    }

                    return false;
                }
        );

        // FILTER SEARCH RESULTS
        dashboardSearchFilters.setOnCheckedChangeListener(
                (group, checkedId) -> {

                    String query = dashboardSearchInput
                            .getText()
                            .toString()
                            .trim();

                    if (!query.isEmpty()) {
                        searchProviders(query);
                    }
                }
        );

        // EXPLORE LOCAL MARKET
        localMarketCard.setOnClickListener(v ->
                openPage(DiscoverActivity.class));

        // SEE ALL PROVIDERS
        seeAllProviders.setOnClickListener(v ->
                openPage(SearchActivity.class));

        // BOTTOM NAVIGATION
        navHome.setOnClickListener(v -> clearSearch());

        navMarket.setOnClickListener(v ->
                openPage(DiscoverActivity.class));

        navProviders.setOnClickListener(v ->
                openPage(SearchActivity.class));

        navProfile.setOnClickListener(v ->
                openPage(CustomerProfileActivity.class));

        showNormalDashboard();
    }

    // LOAD CUSTOMER INFORMATION FROM FIRESTORE
    private void loadCustomerDetails() {

        FirebaseUser firebaseUser =
                firebaseAuth.getCurrentUser();

        if (firebaseUser == null) {
            customerGreeting.setText("Hello, welcome!");

            Toast.makeText(this,
                    "Please log in to access your account",
                    Toast.LENGTH_LONG).show();
            return;
        }

        String userId = firebaseUser.getUid();

        currentEmail = firebaseUser.getEmail();

        firestore.collection("Customer")
                .document(userId)
                .get()
                .addOnSuccessListener(document -> {

                    if (document.exists()) {

                        currentName =
                                document.getString("fullname");

                        currentPhone =
                                document.getString("phone");

                        displayCustomerName();

                    } else {
                        customerGreeting.setText("Hello, welcome!");

                        Toast.makeText(this,
                                "Customer profile not found",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(this,
                            "Unable to load customer profile: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    // DISPLAY CUSTOMER'S FIRST NAME
    private void displayCustomerName() {

        if (currentName != null &&
                !currentName.trim().isEmpty()) {

            String firstName =
                    currentName.trim().split("\\s+")[0];

            customerGreeting.setText(
                    "Hello, " + firstName + "!"
            );

        } else {
            customerGreeting.setText("Hello, welcome!");
        }
    }

    // SEARCH BUSINESSES AND FREELANCERS
    private void searchProviders(String query) {

        dashboardContent.setVisibility(View.GONE);
        dashboardSearchResults.setVisibility(View.VISIBLE);

        dashboardResultsContainer.removeAllViews();

        dashboardSearchEmpty.setVisibility(View.VISIBLE);
        dashboardSearchEmpty.setText("Searching providers...");

        int version = ++searchVersion;

        businesses.clear();
        freelancers.clear();

        int selectedFilter =
                dashboardSearchFilters.getCheckedRadioButtonId();

        boolean includeBusinesses =
                selectedFilter != R.id.searchFilterFreelancers;

        boolean includeFreelancers =
                selectedFilter != R.id.searchFilterBusinesses;

        final int expectedRequests =
                (includeBusinesses ? 1 : 0)
                        + (includeFreelancers ? 1 : 0);

        final int[] completedRequests = {0};
        final boolean[] failed = {false};

        Runnable finishSearch = () -> {

            completedRequests[0]++;

            if (completedRequests[0] == expectedRequests
                    && version == searchVersion) {

                displaySearchResults(query, failed[0]);
            }
        };

        if (includeBusinesses) {

            firestore.collection("Business")
                    .get()
                    .addOnSuccessListener(snapshot -> {

                        if (version != searchVersion) return;

                        businesses.addAll(snapshot.getDocuments());
                        finishSearch.run();
                    })
                    .addOnFailureListener(e -> {

                        if (version != searchVersion) return;

                        failed[0] = true;
                        finishSearch.run();
                    });
        }

        if (includeFreelancers) {

            firestore.collection("Freelancer")
                    .get()
                    .addOnSuccessListener(snapshot -> {

                        if (version != searchVersion) return;

                        freelancers.addAll(snapshot.getDocuments());
                        finishSearch.run();
                    })
                    .addOnFailureListener(e -> {

                        if (version != searchVersion) return;

                        failed[0] = true;
                        finishSearch.run();
                    });
        }
    }

    // DISPLAY MATCHING PROVIDERS
    private void displaySearchResults(
            String query, boolean failed) {

        dashboardResultsContainer.removeAllViews();

        int matches = 0;

        for (DocumentSnapshot business : businesses) {

            if (matchesQuery(business, query)) {
                addProviderResult(business, "Business");
                matches++;
            }
        }

        for (DocumentSnapshot freelancer : freelancers) {

            if (matchesQuery(freelancer, query)) {
                addProviderResult(freelancer, "Freelancer");
                matches++;
            }
        }

        if (matches == 0) {

            dashboardSearchEmpty.setVisibility(View.VISIBLE);

            dashboardSearchEmpty.setText(
                    failed
                            ? "Unable to load all providers. Please try again."
                            : "No matching providers found."
            );

        } else {

            dashboardSearchEmpty.setVisibility(View.GONE);

            if (failed) {
                Toast.makeText(this,
                        "Some providers could not be loaded",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    // CHECK WHETHER A PROVIDER MATCHES THE SEARCH
    private boolean matchesQuery(
            DocumentSnapshot document, String query) {

        String searchText = (
                safeValue(document, "name") + " " +
                        safeValue(document, "businessName") + " " +
                        safeValue(document, "fullname") + " " +
                        safeValue(document, "category") + " " +
                        safeValue(document, "service")
        ).toLowerCase(Locale.ROOT);

        return searchText.contains(
                query.toLowerCase(Locale.ROOT)
        );
    }

    private String safeValue(
            DocumentSnapshot document, String field) {

        String value = document.getString(field);
        return value == null ? "" : value;
    }

    // CREATE A CLICKABLE SEARCH RESULT
    private void addProviderResult(
            DocumentSnapshot document, String type) {

        String name = safeValue(document, "name");

        if (name.isEmpty()) {
            name = safeValue(document, "businessName");
        }

        if (name.isEmpty()) {
            name = safeValue(document, "fullname");
        }

        if (name.isEmpty()) {
            name = "Unnamed provider";
        }

        String category = safeValue(document, "category");

        TextView resultView = new TextView(this);

        resultView.setText(
                name + "\n" + type +
                        (category.isEmpty() ? "" : " • " + category)
        );

        resultView.setTextSize(16);
        resultView.setPadding(24, 20, 24, 20);
        resultView.setClickable(true);

        final String providerName = name;

        resultView.setOnClickListener(v -> {

            Intent intent = new Intent(
                    this, DetailsActivity.class
            );

            intent.putExtra("providerId", document.getId());
            intent.putExtra("providerName", providerName);
            intent.putExtra("providerCategory", category);
            intent.putExtra("providerType", type);

            startActivity(intent);
        });

        dashboardResultsContainer.addView(resultView);
    }

    // SHOW NORMAL DASHBOARD
    private void showNormalDashboard() {

        searchVersion++;

        dashboardContent.setVisibility(View.VISIBLE);
        dashboardSearchResults.setVisibility(View.GONE);

        dashboardResultsContainer.removeAllViews();
        dashboardSearchEmpty.setVisibility(View.GONE);
    }

    // CLEAR SEARCH
    private void clearSearch() {

        dashboardSearchInput.setText("");
        showNormalDashboard();
    }

    // OPEN ANOTHER ACTIVITY
    private void openPage(Class<?> destination) {

        Intent intent = new Intent(this, destination);

        intent.putExtra("customerName", currentName);
        intent.putExtra("customerEmail", currentEmail);
        intent.putExtra("customerPhone", currentPhone);

        startActivity(intent);
    }
}
