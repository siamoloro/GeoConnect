package com.example.geoconnect;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class NotificationsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notifications);
        FreelancerUi.applyInsets(findViewById(R.id.main));

        findViewById(R.id.tvMarkAllRead).setOnClickListener(v -> {
            //TODO: mark the notifications as read in the backend
            Toast.makeText(this, "All notifications marked as read", Toast.LENGTH_SHORT).show();
        });
    }
}