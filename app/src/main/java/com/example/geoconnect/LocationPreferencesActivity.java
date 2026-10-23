package com.example.geoconnect;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LocationPreferencesActivity extends AppCompatActivity {

    private TextView txtCurrentLocation;

    private LatLng selectedLatLng = null;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private FusedLocationProviderClient fusedLocationClient;

    // Firebase instances
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userId;

    // User type retrieved from the users collection ("type" field)
    private String userType = "business"; // Default fallback

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_preferences);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            userId = currentUser.getUid();
            // Fetch the user's type from the "users" collection, then load any existing saved location
            fetchUserTypeAndExistingLocation();
        } else {
            Toast.makeText(this, "User session not found. Please log in again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        txtCurrentLocation = findViewById(R.id.txtCurrentLocation);

        MaterialButton btnSearchLocation = findViewById(R.id.btnSearchLocation);
        MaterialButton btnCurrentLocation = findViewById(R.id.btnCurrentLocation);
        MaterialButton btnSavePreferences = findViewById(R.id.btnSavePreferences);

        // Open a native dialog to type an address (No API Key required)
        btnSearchLocation.setOnClickListener(v -> showAddressSearchDialog());

        // Fetch phone's current GPS location
        btnCurrentLocation.setOnClickListener(v -> checkLocationPermissionAndFetch());

        btnSavePreferences.setOnClickListener(v -> savePreferencesToDatabase());
    }

    /**
     * Fetches the "type" field from the "users" collection, determines the correct target collection,
     * and queries Firestore for any previously saved location to display in txtCurrentLocation.
     */
    private void fetchUserTypeAndExistingLocation() {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String type = documentSnapshot.getString("type");
                        if (type != null && !type.trim().isEmpty()) {
                            userType = type.trim().toLowerCase(Locale.ROOT);
                        }
                    }

                    // Once userType is determined, load the existing saved location
                    loadExistingLocation();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Could not verify user type. Using default.", Toast.LENGTH_SHORT).show();
                    loadExistingLocation();
                });
    }

    private void loadExistingLocation() {
        String targetCollection;
        switch (userType) {
            case "freelancer":
                targetCollection = "freelancers";
                break;
            case "customer":
                targetCollection = "customers";
                break;
            case "business":
            default:
                targetCollection = "businesses";
                break;
        }

        // Query the 'location' subcollection for the most recent entry
        db.collection(targetCollection).document(userId).collection("location")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        var doc = querySnapshot.getDocuments().get(0);
                        String address = doc.getString("address");
                        Double lat = doc.getDouble("latitude");
                        Double lng = doc.getDouble("longitude");

                        if (address != null && !address.trim().isEmpty()) {
                            txtCurrentLocation.setText(address);
                        }

                        if (lat != null && lng != null) {
                            selectedLatLng = new LatLng(lat, lng);
                        }
                    } else {
                        // Fallback check on main document if address is stored directly there
                        db.collection(targetCollection).document(userId).get()
                                .addOnSuccessListener(mainDoc -> {
                                    String directAddress = mainDoc.getString("address");
                                    if (directAddress != null && !directAddress.trim().isEmpty()) {
                                        txtCurrentLocation.setText(directAddress);
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // Fail silently or keep placeholder text if network fails
                });
    }

    private void showAddressSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Search Address");

        final EditText input = new EditText(this);
        input.setHint("Enter street address or city (e.g. Pretoria)");
        input.setPadding(40, 30, 40, 30);
        builder.setView(input);

        builder.setPositiveButton("Search", (dialog, which) -> {
            String query = input.getText().toString().trim();
            if (!query.isEmpty()) {
                searchAddressUsingGeocoder(query);
            } else {
                Toast.makeText(this, "Please enter a valid search term", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void searchAddressUsingGeocoder(String locationName) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocationName(locationName, 1);
            if (addressList != null && !addressList.isEmpty()) {
                Address address = addressList.get(0);
                selectedLatLng = new LatLng(address.getLatitude(), address.getLongitude());
                String fullAddress = address.getAddressLine(0);

                if (txtCurrentLocation != null) {
                    txtCurrentLocation.setText(fullAddress);
                }
                Toast.makeText(this, "Address found!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Location not found. Try a broader search term.", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(this, "Geocoder error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void checkLocationPermissionAndFetch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fetchDeviceGpsLocation();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void fetchDeviceGpsLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Toast.makeText(this, "Fetching phone GPS location...", Toast.LENGTH_SHORT).show();

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                selectedLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                reverseGeocodeCoordinates(selectedLatLng);
            } else {
                Toast.makeText(this, "Unable to get GPS signal. Ensure location is turned on.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void reverseGeocodeCoordinates(LatLng latLng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String fullAddress = address.getAddressLine(0);
                if (txtCurrentLocation != null) {
                    txtCurrentLocation.setText(fullAddress);
                }
                Toast.makeText(this, "Current location captured!", Toast.LENGTH_SHORT).show();
            } else {
                String fallback = String.format(Locale.getDefault(), "Lat: %.4f, Lng: %.4f", latLng.latitude, latLng.longitude);
                if (txtCurrentLocation != null) {
                    txtCurrentLocation.setText(fallback);
                }
            }
        } catch (IOException e) {
            if (txtCurrentLocation != null) {
                txtCurrentLocation.setText(String.format(Locale.getDefault(), "Lat: %.4f, Lng: %.4f", latLng.latitude, latLng.longitude));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchDeviceGpsLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void savePreferencesToDatabase() {
        String addressText = txtCurrentLocation != null ? txtCurrentLocation.getText().toString().trim() : "";

        if (addressText.isEmpty() || selectedLatLng == null) {
            Toast.makeText(this, "Please select or fetch a valid location first.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prepare the location data map
        Map<String, Object> locationData = new HashMap<>();
        locationData.put("address", addressText);
        locationData.put("latitude", selectedLatLng.latitude);
        locationData.put("longitude", selectedLatLng.longitude);
        locationData.put("timestamp", com.google.firebase.Timestamp.now());

        // Determine target collection dynamically based on the fetched user type
        String targetCollection;
        switch (userType) {
            case "freelancer":
                targetCollection = "freelancers";
                break;
            case "customer":
                targetCollection = "customers";
                break;
            case "business":
            default:
                targetCollection = "businesses";
                break;
        }

        // Save inside the appropriate collection based on user type
        db.collection(targetCollection).document(userId)
                .collection("location")
                .add(locationData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Location preferences saved successfully to " + targetCollection + "!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to save preferences: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}