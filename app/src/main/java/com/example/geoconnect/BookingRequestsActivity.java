package com.example.geoconnect;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.content.ContextCompat;


public class BookingRequestsActivity extends AppCompatActivity {

    private final int[] acceptIds = {R.id.btnAccept1, R.id.btnAccept2, R.id.btnAccept3};
    private final int[] declineIds = {R.id.btnDecline1, R.id.btnDecline2, R.id.btnDecline3};
    private final String[] customers = {"Sam Nkosi", "Thandi Mokoena", "Pieter van Wyk"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_booking_requests);
        FreelancerUi.applyInsets(findViewById(R.id.main));
        FreelancerUi.setupNav(this, "Requests");

        for (int i = 0; i < customers.length; i++) {
             final String customer = customers[i];
             findViewById(acceptIds[i]).setOnClickListener(v -> {
                 //TODO: tell the backend this booking was accepted
                 Toast.makeText(this, "Accepted booking from " + customer, Toast.LENGTH_SHORT).show();
             });
             findViewById(declineIds[i]).setOnClickListener(v -> {
                 //TODO: tell the backend this booking was declined
                 Toast.makeText(this, "Decline booking from " + customer, Toast.LENGTH_SHORT).show();
             });
        }
        TextView tabPending = findViewById(R.id.tabPending);
        TextView tabAccepted = findViewById(R.id.tabAccepted);
        TextView tabCompleted = findViewById(R.id.tabCompleted);
        TextView[] tabs = {tabPending, tabAccepted, tabCompleted};
        for (TextView tab : tabs) {
            //TODO: load the bookings for the selected tab from the backend
            tab.setOnClickListener(v -> selectTab(tab, tabs));
        }

    }
    private void selectTab(TextView selected, TextView[] all) {
        for (TextView t : all) {
            boolean on = t == selected;
            t.setBackgroundResource(on ? R.drawable.bg_chip_active : R.drawable.bg_chip);
            t.setTextColor(ContextCompat.getColor(this, on ? R.color.glitter : R.color.forest_green));

        }
    }
}