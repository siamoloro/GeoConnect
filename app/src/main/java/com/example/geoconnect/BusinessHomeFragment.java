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

public class BusinessHomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_business_home, container, false);

        ImageView btnNotifications = view.findViewById(R.id.btnNotifications);
        TextView btnViewProfile = view.findViewById(R.id.btnViewProfile);

        btnNotifications.setOnClickListener(v ->{
                    Intent intent = new Intent(requireContext(), NotificationsActivity.class);
                    startActivity(intent);
        });

        btnViewProfile.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Opening business profile preview...", Toast.LENGTH_SHORT).show()
        );

        return view;
    }
}