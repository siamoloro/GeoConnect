package com.example.geoconnect;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 1. Installs the native system splash screen handler
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 2. Delay for 2 seconds, then transition to your main dashboard/signin activity
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Replace SigninActivity.class with whatever your next screen is
            Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
            startActivity(intent);
            finish(); // Close MainActivity so the user can't press back into it
        }, 2000); // 2000 milliseconds = 2 seconds
    }
}