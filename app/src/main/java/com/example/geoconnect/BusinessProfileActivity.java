package com.example.geoconnect;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class BusinessProfileActivity extends Fragment {

    private ImageView btnSettings;
    private TextView txtBusinessName, txtCategory, txtAbout, txtLocation, btnEditProfile;

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

        // Navigate to SettingsActivity on settings icon click
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), SettingsActivity.class);
            startActivity(intent);
        });

        btnEditProfile.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Opening Profile Editor...", Toast.LENGTH_SHORT).show()
        );

        return view;
    }
}