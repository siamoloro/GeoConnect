
package com.example.geoconnect;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DiscoverActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    // FIREBASE
    private FirebaseFirestore firestore;

    // GOOGLE MAPS
    private GoogleMap googleMap;

    // MARKET COMPONENTS
    private EditText marketSearchInput;
    private RadioGroup marketProviderFilters;
    private RadioGroup marketViewGroup;
    private FrameLayout marketMapContainer;
    private ScrollView marketListContainer;
    private LinearLayout marketProvidersList;
    private TextView marketListEmpty;

    // BOTTOM NAVIGATION
    private View navHome;
    private View navMarket;
    private View navProviders;
    private View navProfile;

    // PROVIDER DATA
    private final List<ProviderItem> providers = new ArrayList<>();
    private final List<ProviderItem> filteredProviders = new ArrayList<>();

    // TRACK FIREBASE REQUESTS
    private int completedRequests = 0;
    private int successfulRequests = 0;
    private boolean loadingProviders = true;

    private static class ProviderItem {

        String id;
        String name;
        String category;
        String type;
        GeoPoint location;

        ProviderItem(String id, String name, String category,
                     String type, GeoPoint location) {

            this.id = id;
            this.name = name;
            this.category = category;
            this.type = type;
            this.location = location;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_discover);
        FreelancerUi.applyInsets(findViewById(android.R.id.content));

        // INITIALIZE FIREBASE
        firestore = FirebaseFirestore.getInstance();

        // CONNECT MARKET COMPONENTS
        marketSearchInput = findViewById(R.id.marketSearchInput);

        marketProviderFilters =
                findViewById(R.id.marketProviderFilters);

        marketViewGroup =
                findViewById(R.id.marketViewGroup);

        marketMapContainer =
                findViewById(R.id.marketMapContainer);

        marketListContainer =
                findViewById(R.id.marketListContainer);

        marketProvidersList =
                findViewById(R.id.marketProvidersList);

        marketListEmpty =
                findViewById(R.id.marketListEmpty);

        // CONNECT BOTTOM NAVIGATION
        navHome = findViewById(R.id.navHome);
        navMarket = findViewById(R.id.navMarket);
        navProviders = findViewById(R.id.navProviders);
        navProfile = findViewById(R.id.navProfile);

        // INITIALIZE GOOGLE MAP
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.marketMapFragment);

        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.marketMapFragment, mapFragment)
                    .commitNow();
        }

        mapFragment.getMapAsync(this);

        // SWITCH BETWEEN MAP AND LIST
        marketViewGroup.setOnCheckedChangeListener(
                (group, checkedId) -> {

                    if (checkedId == R.id.mapViewRadio) {
                        showMapView();

                    } else if (checkedId == R.id.listViewRadio) {
                        showListView();
                    }
                }
        );

        // FILTER PROVIDERS
        marketProviderFilters.setOnCheckedChangeListener(
                (group, checkedId) -> updateMarketResults()
        );

        // SEARCH AS CUSTOMER TYPES
        marketSearchInput.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s, int start,
                            int count, int after) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s, int start,
                            int before, int count) {

                        updateMarketResults();
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                }
        );

        // SEARCH BUTTON ON KEYBOARD
        marketSearchInput.setOnEditorActionListener(
                (v, actionId, event) -> {
                    updateMarketResults();
                    return false;
                }
        );

        // BOTTOM NAVIGATION
        navHome.setOnClickListener(v ->
                openPage(CustomerDashboardActivity.class));

        navMarket.setOnClickListener(v -> {
            // Already on Market
        });

        navProviders.setOnClickListener(v ->
                openPage(SearchActivity.class));

        navProfile.setOnClickListener(v ->
                openPage(CustomerProfileActivity.class));

        // DEFAULT TO MAP VIEW
        showMapView();

        // LOAD PROVIDERS FROM FIRESTORE
        loadProviders();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;

        // INITIAL CAMERA POSITION ONLY (-25.7479, 28.2293)
        LatLng initialLocation = new LatLng(-25.7479, 28.2293);

        googleMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                        initialLocation, 11f
                )
        );

        googleMap.getUiSettings().setZoomControlsEnabled(true);

        // OPEN PROVIDER DETAILS WHEN MARKER IS CLICKED
        googleMap.setOnInfoWindowClickListener(marker -> {
            Object tag = marker.getTag();
            if (tag instanceof ProviderItem) {
                openProviderDetails((ProviderItem) tag);
            }
        });

        // Safely draw markers now that the map instance is ready
        updateMapMarkers();
    }

    // ADD PROVIDERS WITH LOCATIONS TO GOOGLE MAPS
    private void updateMapMarkers() {
        // Guard check: do nothing if the map hasn't finished initializing yet
        if (googleMap == null) return;

        googleMap.clear();

        for (ProviderItem provider : filteredProviders) {
            if (provider.location == null) {
                continue;
            }

            LatLng position = new LatLng(
                    provider.location.getLatitude(),
                    provider.location.getLongitude()
            );

            Marker marker = googleMap.addMarker(
                    new MarkerOptions()
                            .position(position)
                            .title(provider.name)
                            .snippet(provider.category)
            );

            if (marker != null) {
                marker.setTag(provider);
            }
        }
    }

    // LOAD BUSINESSES AND FREELANCERS
    private void loadProviders() {

        loadingProviders = true;
        completedRequests = 0;
        successfulRequests = 0;
        providers.clear();

        updateMarketResults();

        loadProviderCollection("businesses", "Business");
        loadProviderCollection("freelancers", "Freelancer");
    }

    private void loadProviderCollection(
            String collectionName, String type) {

        firestore.collection(collectionName)
                .get()
                .addOnSuccessListener(snapshot -> {

                    successfulRequests++;

                    for (DocumentSnapshot document :
                            snapshot.getDocuments()) {

                        String name = getFirstAvailable(
                                document,
                                "name",
                                "businessName",
                                "fullname"
                        );

                        String category = getFirstAvailable(
                                document,
                                "category",
                                "service",
                                "serviceCategory"
                        );

                        if (name.isEmpty()) {
                            continue;
                        }

                        // getGeoPoint() throws if 'location' was saved as text, so check the type first;
                        // older saves only have separate latitude/longitude numbers
                        Object loc = document.get("location");
                        GeoPoint location = loc instanceof GeoPoint ? (GeoPoint) loc : null;
                        Object lat = document.get("latitude");
                        Object lng = document.get("longitude");
                        if (location == null && lat instanceof Number && lng instanceof Number) {
                            location = new GeoPoint(((Number) lat).doubleValue(), ((Number) lng).doubleValue());
                        }

                        providers.add(new ProviderItem(
                                document.getId(),
                                name,
                                category,
                                type,
                                location
                        ));
                    }

                    finishProviderRequest();
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(this,
                            "Unable to load " + collectionName,
                            Toast.LENGTH_SHORT).show();

                    finishProviderRequest();
                });
    }

    private void finishProviderRequest() {

        completedRequests++;

        if (completedRequests == 2) {
            loadingProviders = false;
            updateMarketResults();

            if (successfulRequests == 0) {
                marketListEmpty.setText(
                        "Unable to load providers. Please try again."
                );
            }
        }
    }

    // FIND A VALUE FROM KNOWN FIELD OPTIONS
    private String getFirstAvailable(
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

    // APPLY SEARCH AND PROVIDER FILTERS
    private void updateMarketResults() {

        if (marketSearchInput == null) return;

        String query = marketSearchInput
                .getText()
                .toString()
                .trim()
                .toLowerCase(Locale.ROOT);

        int selectedType =
                marketProviderFilters.getCheckedRadioButtonId();

        filteredProviders.clear();

        for (ProviderItem provider : providers) {

            if (selectedType == R.id.marketFilterBusinesses
                    && !provider.type.equals("Business")) {
                continue;
            }

            if (selectedType == R.id.marketFilterFreelancers
                    && !provider.type.equals("Freelancer")) {
                continue;
            }

            String searchableText =
                    (provider.name + " " + provider.category)
                            .toLowerCase(Locale.ROOT);

            if (!searchableText.contains(query)) {
                continue;
            }

            filteredProviders.add(provider);
        }

        displayProviderList();
        updateMapMarkers();
    }

    // DISPLAY REAL PROVIDERS IN LIST VIEW
    private void displayProviderList() {

        marketProvidersList.removeAllViews();

        if (loadingProviders) {
            marketListEmpty.setVisibility(View.VISIBLE);
            marketListEmpty.setText("Loading providers...");
            return;
        }

        if (filteredProviders.isEmpty()) {

            marketListEmpty.setVisibility(View.VISIBLE);
            marketListEmpty.setText(
                    "No matching providers found."
            );

            return;
        }

        marketListEmpty.setVisibility(View.GONE);

        for (ProviderItem provider : filteredProviders) {

            TextView providerCard = new TextView(this);

            String details =
                    provider.name + "\n" +
                            provider.type +
                            (provider.category.isEmpty()
                                    ? ""
                                    : "\n" + provider.category);

            providerCard.setText(details);
            providerCard.setTextSize(16);
            providerCard.setPadding(28, 24, 28, 24);

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            params.bottomMargin = 16;
            providerCard.setLayoutParams(params);

            providerCard.setOnClickListener(v ->
                    openProviderDetails(provider));

            marketProvidersList.addView(providerCard);
        }
    }


    // OPEN SELECTED PROVIDER DETAILS
    private void openProviderDetails(ProviderItem provider) {

        Intent intent = new Intent(
                this, DetailsActivity.class
        );

        intent.putExtra("providerId", provider.id);
        intent.putExtra("providerName", provider.name);
        intent.putExtra("providerCategory", provider.category);
        intent.putExtra("providerType", provider.type);

        startActivity(intent);
    }

    // SHOW MAP VIEW
    private void showMapView() {

        marketMapContainer.setVisibility(View.VISIBLE);
        marketListContainer.setVisibility(View.GONE);
    }

    // SHOW LIST VIEW
    private void showListView() {

        marketMapContainer.setVisibility(View.GONE);
        marketListContainer.setVisibility(View.VISIBLE);

        updateMarketResults();
    }

    // OPEN ANOTHER PAGE
    private void openPage(Class<?> destination) {

        Intent intent = new Intent(this, destination);
        startActivity(intent);
        finish();
    }
}