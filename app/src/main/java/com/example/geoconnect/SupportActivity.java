package com.example.geoconnect;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.content.Intent;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class SupportActivity extends AppCompatActivity {

    TextView btnOpenCase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_support);

        btnOpenCase = findViewById(R.id.btnOpenCase);

        btnOpenCase.setOnClickListener(v -> {
            Intent intent = new Intent(SupportActivity.this, OpenCaseActivity.class);
            startActivity(intent);
        });
        FreelancerUi.applyInsets(findViewById(R.id.main));
        FreelancerUi.setupBack(this);

        findViewById(R.id.btnOpenCase).setOnClickListener(v ->
                startActivity(new Intent(this, OpenCaseActivity.class)));

    }
}