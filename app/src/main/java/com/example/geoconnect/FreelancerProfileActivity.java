package com.example.geoconnect;

import android.os.Bundle;
import android.content.Intent;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class FreelancerProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_freelancer_profile);
        FreelancerUi.applyInsets(findViewById(R.id.main));
        FreelancerUi.setupNav(this, "Profile");

        findViewById(R.id.btnEditProfile).setOnClickListener(v ->
                startActivity(new Intent(this, FreelancerEditProfileActivity.class)));

    }
}