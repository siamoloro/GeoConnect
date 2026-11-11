package com.example.geoconnect;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

import java.io.IOException;

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

    private FusedLocationProviderClient fusedLocationClient;
    private ActivityResultLauncher<String[]> locationPermissionLauncher;

    // CUSTOMER LOCATION
    private TextView locationValue;

    // BOTTOM NAVIGATION
    private View navHome;
    private View navMarket;
    private View navProviders;
    private View navProfile;

    // SEARCH STATE
    private final List<DocumentSnapshot> businesses = new ArrayList<>();
    private final List<DocumentSnapshot> freelancers = new ArrayList<>();

    private int searchVersion = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_dashboard);

        loadServiceCategories();
        // Connect the existing location TextView
        locationValue = findViewById(R.id.locationValue);

        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Handle location permission
        locationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                permissions -> {
                    Boolean fine = permissions.get(Manifest.permission.ACCESS_FINE_LOCATION);
                    Boolean coarse = permissions.get(Manifest.permission.ACCESS_COARSE_LOCATION);

                    if (Boolean.TRUE.equals(fine) || Boolean.TRUE.equals(coarse)) {
                        getCurrentLocation();
                    } else {
                        locationValue.setText("Location permission denied");
                    }
                }
        );

        // Request location when Dashboard opens
        checkLocationPermission();

        // INITIALIZE FIREBASE
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // CONNECT DASHBOARD COMPONENTS
        customerGreeting = findViewById(R.id.customerGreeting);
        notificationButton = findViewById(R.id.notificationButton);
        localMarketCard = findViewById(R.id.localMarketCard);
        seeAllProviders = findViewById(R.id.seeAllProviders);

        // CONNECT SEARCH COMPONENTS
        dashboardSearchInput = findViewById(R.id.dashboardSearchInput);
        dashboardSearchResults = findViewById(R.id.dashboardSearchResults);
        dashboardResultsContainer = findViewById(R.id.dashboardResultsContainer);
        dashboardContent = findViewById(R.id.dashboardContent);
        dashboardSearchEmpty = findViewById(R.id.dashboardSearchEmpty);
        dashboardSearchFilters = findViewById(R.id.dashboardSearchFilters);

        // CONNECT BOTTOM NAVIGATION
        navHome = findViewById(R.id.navHome);
        navMarket = findViewById(R.id.navMarket);
        navProviders = findViewById(R.id.navProviders);
        navProfile = findViewById(R.id.navProfile);

        // LOAD LOGGED-IN CUSTOMER
        loadCustomerDetails();

        // NOTIFICATIONS
        notificationButton.setOnClickListener(v -> openPage(NotificationsActivity.class));

        // SEARCH AS CUSTOMER TYPES
        dashboardSearchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    showNormalDashboard();
                } else {
                    searchProviders(query);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // SEARCH BUTTON ON KEYBOARD
        dashboardSearchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = dashboardSearchInput.getText().toString().trim();
                if (query.isEmpty()) {
                    showNormalDashboard();
                } else {
                    searchProviders(query);
                }
                return true;
            }
            return false;
        });

        // FILTER SEARCH RESULTS
        dashboardSearchFilters.setOnCheckedChangeListener((group, checkedId) -> {
            String query = dashboardSearchInput.getText().toString().trim();
            if (!query.isEmpty()) {
                searchProviders(query);
            }
        });

        // EXPLORE LOCAL MARKET
        localMarketCard.setOnClickListener(v -> openPage(DiscoverActivity.class));

        // SEE ALL PROVIDERS
        seeAllProviders.setOnClickListener(v -> openPage(SearchActivity.class));

        // BOTTOM NAVIGATION
        navHome.setOnClickListener(v -> clearSearch());
        navMarket.setOnClickListener(v -> openPage(DiscoverActivity.class));
        navProviders.setOnClickListener(v -> {
            Toast.makeText(CustomerDashboardActivity.this, "Providers button clicked", Toast.LENGTH_SHORT).show();
            openPage(SearchActivity.class);
        });
        navProfile.setOnClickListener(v -> openPage(CustomerProfileActivity.class));

        showNormalDashboard();
    }
    // LOAD BUSINESS AND FREELANCER CATEGORIES
    private void loadServiceCategories() {

        LinearLayout businessRow =
                findViewById(R.id.businessCategoriesRow);

        LinearLayout freelancerRow =
                findViewById(R.id.freelancerCategoriesRow);

        businessRow.removeAllViews();
        freelancerRow.removeAllViews();

        String[] businessCategories = getResources()
                .getStringArray(R.array.business_categories_array);

        String[] freelancerCategories = getResources()
                .getStringArray(R.array.service_categories);

        // BUSINESS CATEGORIES
        for (int i = 1; i < businessCategories.length; i++) {
            addCategoryCard(
                    businessRow,
                    businessCategories[i],
                    "Business"
            );
        }

        // FREELANCER CATEGORIES
        for (int i = 1; i < freelancerCategories.length; i++) {
            addCategoryCard(
                    freelancerRow,
                    freelancerCategories[i],
                    "Freelancer"
            );
        }
    }


    // CREATE HORIZONTAL CATEGORY CARDS
    private void addCategoryCard(
            LinearLayout container,
            String category,
            String providerType) {

        TextView card = new TextView(this);

        card.setText(category);
        card.setTextSize(12);
        card.setTextColor(Color.parseColor("#173D31"));
        card.setGravity(Gravity.CENTER);
        card.setMaxLines(4);

        card.setPadding(
                dp(8),
                dp(6),
                dp(8),
                dp(6)
        );

        GradientDrawable background = new GradientDrawable();

        background.setColor(Color.WHITE);
        background.setCornerRadius(dp(14));
        background.setStroke(
                dp(1),
                Color.parseColor("#D5E9DE")
        );

        card.setBackground(background);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        dp(125),
                        dp(65)
                );

        params.setMargins(0, 0, dp(8), 0);
        card.setLayoutParams(params);

        card.setClickable(true);
        card.setFocusable(true);

        // Search for providers in the selected category
        card.setOnClickListener(v -> {

            // Use the dashboard's existing search functionality
            dashboardSearchInput.setText(category);

            if ("Business".equals(providerType)) {
                dashboardSearchFilters.check(
                        R.id.searchFilterBusinesses
                );
            } else {
                dashboardSearchFilters.check(
                        R.id.searchFilterFreelancers
                );
            }

            searchProviders(category);
        });

        container.addView(card);
    }


    // CONVERT DP TO PIXELS
    private int dp(int value) {
        return (int) (
                value * getResources()
                        .getDisplayMetrics().density + 0.5f
        );
    }

    // LOAD CUSTOMER INFORMATION FROM FIRESTORE
    private void loadCustomerDetails() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser == null) {
            customerGreeting.setText("Hello, welcome");
            Toast.makeText(this, "Please log in to access your account", Toast.LENGTH_LONG).show();
            return;
        }

        String userId = firebaseUser.getUid();
        currentEmail = firebaseUser.getEmail();

        firestore.collection("customers")
                .document(userId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        currentName = document.getString("fullname");
                        currentPhone = document.getString("phone");
                        displayCustomerName();
                    } else {
                        customerGreeting.setText("Hello, welcome");
                        Toast.makeText(this, "Customer profile not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Unable to load customer profile: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    // DISPLAY CUSTOMER'S FIRST NAME
    private void displayCustomerName() {
        if (currentName != null && !currentName.trim().isEmpty()) {
            String firstName = currentName.trim().split("\\s+")[0];
            customerGreeting.setText("Hello, " + firstName);
        } else {
            customerGreeting.setText("Hello, welcome");
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

        int selectedFilter = dashboardSearchFilters.getCheckedRadioButtonId();
        boolean includeBusinesses = selectedFilter != R.id.searchFilterFreelancers;
        boolean includeFreelancers = selectedFilter != R.id.searchFilterBusinesses;

        final int expectedRequests = (includeBusinesses ? 1 : 0) + (includeFreelancers ? 1 : 0);
        final int[] completedRequests = {0};
        final boolean[] failed = {false};

        Runnable finishSearch = () -> {
            completedRequests[0]++;
            if (completedRequests[0] == expectedRequests && version == searchVersion) {
                displaySearchResults(query, failed[0]);
            }
        };

        if (includeBusinesses) {
            firestore.collection("businesses")
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
            firestore.collection("freelancers")
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
    private void displaySearchResults(String query, boolean failed) {
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
            dashboardSearchEmpty.setText(failed ? "Unable to load all providers. Please try again." : "No matching providers found.");
        } else {
            dashboardSearchEmpty.setVisibility(View.GONE);
            if (failed) {
                Toast.makeText(this, "Some providers could not be loaded", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // SEARCH PROVIDERS BY NAME, CATEGORY AND SERVICES
    private boolean matchesQuery(DocumentSnapshot document, String query) {

        StringBuilder searchText = new StringBuilder();

        // Provider names and categories
        searchText.append(safeValue(document, "name")).append(" ");
        searchText.append(safeValue(document, "businessName")).append(" ");
        searchText.append(safeValue(document, "fullname")).append(" ");
        searchText.append(safeValue(document, "fullName")).append(" ");
        searchText.append(safeValue(document, "category")).append(" ");
        searchText.append(safeValue(document, "serviceCategory")).append(" ");
        searchText.append(safeValue(document, "service")).append(" ");

        // Business or freelancer services stored as an array
        Object services = document.get("services");

        if (services instanceof List<?>) {

            for (Object item : (List<?>) services) {

                if (item instanceof String) {
                    searchText.append((String) item).append(" ");
                }
            }
        }

        return searchText.toString()
                .toLowerCase(Locale.ROOT)
                .contains(query.trim().toLowerCase(Locale.ROOT));
    }

    private String safeValue(DocumentSnapshot document, String field) {
        String value = document.getString(field);
        return value == null ? "" : value;
    }

    // CREATE A CLICKABLE SEARCH RESULT (Updated with fullName check)
    // CREATE A PROVIDER SEARCH RESULT CARD
    private void addProviderResult(DocumentSnapshot document, String type) {

        // GET PROVIDER NAME
        String name = safeValue(document, "name");

        if (name.isEmpty()) {
            name = safeValue(document, "businessName");
        }

        if (name.isEmpty()) {
            name = safeValue(document, "fullname");
        }

        if (name.isEmpty()) {
            name = safeValue(document, "fullName");
        }

        if (name.isEmpty()) {
            name = "Unnamed provider";
        }

        // GET CATEGORY
        String category = safeValue(document, "category");

        if (category.isEmpty()) {
            category = safeValue(document, "serviceCategory");
        }

        // GET LOCATION
        String location = safeValue(document, "address");

        // CREATE CARD
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));

        GradientDrawable cardBackground = new GradientDrawable();
        cardBackground.setColor(Color.WHITE);
        cardBackground.setCornerRadius(dp(16));
        cardBackground.setStroke(
                dp(1),
                Color.parseColor("#DDE5E0")
        );

        card.setBackground(cardBackground);
        card.setElevation(dp(3));

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(
                dp(16), dp(10), dp(16), dp(10)
        );

        card.setLayoutParams(cardParams);

        // PROVIDER TYPE
        TextView typeText = new TextView(this);
        typeText.setText(type.toUpperCase(Locale.ROOT));
        typeText.setTextSize(12);
        typeText.setTextColor(Color.parseColor("#315E50"));
        typeText.setTypeface(null, android.graphics.Typeface.BOLD);

        card.addView(typeText);

        // PROVIDER NAME
        TextView nameText = new TextView(this);

        String nameLabel = type.equalsIgnoreCase("Business")
                ? "Business Name: "
                : "Freelancer Name: ";

        nameText.setText(nameLabel + name);
        nameText.setTextSize(16);
        nameText.setTextColor(Color.parseColor("#173D31"));

        LinearLayout.LayoutParams nameParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        nameParams.topMargin = dp(12);
        nameText.setLayoutParams(nameParams);

        card.addView(nameText);

        // CATEGORY
        TextView categoryText = new TextView(this);
        categoryText.setText(
                "Category: " +
                        (category.isEmpty() ? "Not specified" : category)
        );

        categoryText.setTextSize(14);
        categoryText.setTextColor(Color.parseColor("#74867D"));

        LinearLayout.LayoutParams categoryParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        categoryParams.topMargin = dp(6);
        categoryText.setLayoutParams(categoryParams);

        card.addView(categoryText);

        // LOCATION (ONLY IF AVAILABLE)
        if (!location.isEmpty()) {

            TextView locationText = new TextView(this);
            locationText.setText("Location: " + location);
            locationText.setTextSize(13);
            locationText.setTextColor(Color.parseColor("#74867D"));

            LinearLayout.LayoutParams locationParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            locationParams.topMargin = dp(6);
            locationText.setLayoutParams(locationParams);

            card.addView(locationText);
        }

        // VIEW DETAILS BUTTON
        TextView detailsButton = new TextView(this);
        detailsButton.setText("View Details");
        detailsButton.setTextSize(14);
        detailsButton.setTextColor(Color.WHITE);
        detailsButton.setGravity(Gravity.CENTER);
        detailsButton.setPadding(
                dp(14), dp(10), dp(14), dp(10)
        );

        GradientDrawable buttonBackground = new GradientDrawable();
        buttonBackground.setColor(Color.parseColor("#176B50"));
        buttonBackground.setCornerRadius(dp(4));

        detailsButton.setBackground(buttonBackground);

        LinearLayout.LayoutParams buttonParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        buttonParams.topMargin = dp(16);
        detailsButton.setLayoutParams(buttonParams);

        // OPEN PROVIDER DETAILS
        final String providerName = name;
        final String providerCategory = category;

        View.OnClickListener openDetails = v -> {

            Intent intent = new Intent(
                    CustomerDashboardActivity.this,
                    DetailsActivity.class
            );

            intent.putExtra("providerId", document.getId());
            intent.putExtra("providerName", providerName);
            intent.putExtra("providerCategory", providerCategory);
            intent.putExtra("providerType", type);

            startActivity(intent);
        };

        detailsButton.setOnClickListener(openDetails);

        card.addView(detailsButton);

        // MAKE ENTIRE CARD CLICKABLE
        card.setOnClickListener(openDetails);
        card.setClickable(true);
        card.setFocusable(true);

        // ADD CARD TO SEARCH RESULTS
        dashboardResultsContainer.addView(card);
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

    // CHECK LOCATION PERMISSION
    private void checkLocationPermission() {
        boolean fineGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean coarseGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (fineGranted || coarseGranted) {
            getCurrentLocation();
        } else {
            locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    // GET CUSTOMER'S CURRENT LOCATION
    private void getCurrentLocation() {
        boolean fineGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean coarseGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (!fineGranted && !coarseGranted) {
            locationValue.setText("Location permission required");
            return;
        }

        locationValue.setText("Detecting your location...");

        int priority = fineGranted ? Priority.PRIORITY_HIGH_ACCURACY : Priority.PRIORITY_BALANCED_POWER_ACCURACY;
        CancellationTokenSource tokenSource = new CancellationTokenSource();

        fusedLocationClient.getCurrentLocation(priority, tokenSource.getToken())
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        displayLocation(location);
                    } else {
                        locationValue.setText("Location unavailable");
                    }
                })
                .addOnFailureListener(e -> locationValue.setText("Unable to detect location"));
    }

    // DISPLAY LOCATION NAME
    private void displayLocation(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(latitude, longitude, 1, new Geocoder.GeocodeListener() {
                @Override
                public void onGeocode(List<Address> addresses) {
                    runOnUiThread(() -> showAddress(addresses, latitude, longitude));
                }

                @Override
                public void onError(String errorMessage) {
                    runOnUiThread(() -> showCoordinates(latitude, longitude));
                }
            });
        } else {
            new Thread(() -> {
                List<Address> addresses = null;
                try {
                    addresses = geocoder.getFromLocation(latitude, longitude, 1);
                } catch (IOException | IllegalArgumentException e) {
                    // Fallback to coordinates
                }
                List<Address> finalAddresses = addresses;
                runOnUiThread(() -> showAddress(finalAddresses, latitude, longitude));
            }).start();
        }
    }

    // SHOW READABLE ADDRESS
    private void showAddress(List<Address> addresses, double latitude, double longitude) {
        if (addresses != null && !addresses.isEmpty()) {
            Address address = addresses.get(0);
            String area = address.getSubLocality();
            String city = address.getLocality();

            if (area != null && city != null && !area.equalsIgnoreCase(city)) {
                locationValue.setText(area + ", " + city);
            } else if (city != null) {
                locationValue.setText(city);
            } else if (address.getAdminArea() != null) {
                locationValue.setText(address.getAdminArea());
            } else {
                showCoordinates(latitude, longitude);
            }
        } else {
            showCoordinates(latitude, longitude);
        }
    }

    // FALLBACK IF ADDRESS IS UNAVAILABLE
    private void showCoordinates(double latitude, double longitude) {
        locationValue.setText(String.format(Locale.getDefault(), "%.5f, %.5f", latitude, longitude));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (locationValue != null && locationPermissionLauncher != null) {
            boolean fineGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
            boolean coarseGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

            if (fineGranted || coarseGranted) {
                getCurrentLocation();
            }
        }
    }
}