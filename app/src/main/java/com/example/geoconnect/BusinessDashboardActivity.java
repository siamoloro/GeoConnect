package com.example.geoconnect;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BusinessDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_dashboard);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

        // Load Home Fragment by default when activity launches
        if (savedInstanceState == null) {
            loadFragment(new BusinessHomeFragment());
        }

        // Handle navigation tab clicks
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                selectedFragment = new BusinessHomeFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new BusinessProfileFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.navHostFragment, fragment)
                .commit();
    }
}