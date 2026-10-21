package com.example.geoconnect;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationPreferencesActivity extends AppCompatActivity {

    private TextView txtSelectedAddress, txtCurrentLocation;
    private SwitchMaterial switchPreciseLocation, switchAutoUpdate;

    private LatLng selectedLatLng = null;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_preferences);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        txtCurrentLocation = findViewById(R.id.txtCurrentLocation);
        switchPreciseLocation = findViewById(R.id.switchPreciseLocation);
        switchAutoUpdate = findViewById(R.id.switchAutoUpdate);

        MaterialButton btnSearchLocation = findViewById(R.id.btnSearchLocation);
        MaterialButton btnCurrentLocation = findViewById(R.id.btnCurrentLocation);
        MaterialButton btnSavePreferences = findViewById(R.id.btnSavePreferences);

        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        btnSearchLocation.setOnClickListener(v -> {
            // TODO: Open a Place Autocorrect search intent or dialog
            Toast.makeText(this, "Search location clicked", Toast.LENGTH_SHORT).show();
        });

        btnCurrentLocation.setOnClickListener(v -> {
            checkLocationPermissionAndFetch();
        });



        btnSavePreferences.setOnClickListener(v -> savePreferencesToDatabase());
    }

    private void updateAddressText(LatLng latLng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String fullAddress = address.getAddressLine(0);
                txtSelectedAddress.setText(fullAddress);
            } else {
                txtSelectedAddress.setText(String.format(Locale.getDefault(), "Lat: %.4f, Lng: %.4f", latLng.latitude, latLng.longitude));
            }
        } catch (IOException e) {
            txtSelectedAddress.setText("Unable to resolve address for this location");
        }
    }

    private void checkLocationPermissionAndFetch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // TODO: Use FusedLocationProviderClient to get current device GPS coordinates
            Toast.makeText(this, "Fetching current device location...", Toast.LENGTH_SHORT).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted. Fetching location...", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void savePreferencesToDatabase() {
        boolean isPreciseLocation = switchPreciseLocation.isChecked();
        boolean isAutoUpdate = switchAutoUpdate.isChecked();
        String currentLocationName = txtCurrentLocation.getText().toString();

        // TODO: Save variables (currentLocationName, selectedRadius, isPreciseLocation, isAutoUpdate) to your database / Room DAO
        Toast.makeText(this, "Preferences Saved Successfully!", Toast.LENGTH_SHORT).show();
    }
}