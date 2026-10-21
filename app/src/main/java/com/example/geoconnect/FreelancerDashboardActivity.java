package com.example.geoconnect;

import android.os.Bundle;
import android.content.Intent;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.view.ViewCompat;

public class FreelancerDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_freelancer_dashboard);
        FreelancerUi.applyInsets(findViewById(R.id.main));
        FreelancerUi.setupNav(this, "Home");

        findViewById(R.id.tvAlerts).setOnClickListener(v ->
                startActivity(new Intent(this, NotificationsActivity.class)));
        findViewById(R.id.tvSeeAll).setOnClickListener(v ->
                startActivity(new Intent(this, BookingRequestsActivity.class)));

        SwitchCompat swAvailable = findViewById(R.id.swAvailable);
        swAvailable.setOnCheckedChangeListener((button, isChecked) -> {
            //TODO: save the availability status to the backend
            Toast.makeText(this,
                    isChecked ? "You are now available for bookings" : "You are now unavailable",
                    Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnAccept1).setOnClickListener(v -> {
            //TODO: tell the backend this request was accepted
            Toast.makeText(this, "Request accepted", Toast.LENGTH_SHORT).show();
        });
        findViewById(R.id.btnDecline1).setOnClickListener(v -> {
            //TODO: tell the backend this request was declined
            Toast.makeText(this, "Request declined", Toast.LENGTH_SHORT).show();
        });
    }
}