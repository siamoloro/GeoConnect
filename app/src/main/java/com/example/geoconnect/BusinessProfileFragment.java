package com.example.geoconnect;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class BusinessProfileFragment extends Fragment {

    ImageView btnSettings, btnEditProfile, btnEditHours, btnEditLocation, btnEditOfferings;
    TextView txtBusinessName, txtCategory, txtAbout, txtLocation;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_business_profile, container, false);

        btnSettings = view.findViewById(R.id.btnSettings);
        txtBusinessName = view.findViewById(R.id.txtBusinessName);
        txtCategory = view.findViewById(R.id.txtCategory);
        txtAbout = view.findViewById(R.id.txtAbout);
        txtLocation = view.findViewById(R.id.txtLocation);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnEditHours = view.findViewById(R.id.btnEditHours);
        btnEditLocation = view.findViewById(R.id.btnEditLocation);
        btnEditOfferings = view.findViewById(R.id.btnEditOfferings);

        // Navigate to SettingsActivity on settings icon click
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), SettingsActivity.class);
            startActivity(intent);
        });

        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), BusinessEditProfileActivity.class);
            startActivity(intent);
        });

        btnEditHours.setOnClickListener(v->{
            Intent intent = new Intent(requireContext(), OperationalHoursActivity.class);
            startActivity(intent);
        });

        btnEditLocation.setOnClickListener(v->{
            //
        });

        btnEditOfferings.setOnClickListener(v->{
            Intent intent = new Intent(requireContext(), BusinessOfferingsActivity.class);
            startActivity(intent);
        });

        return view;
    }
}