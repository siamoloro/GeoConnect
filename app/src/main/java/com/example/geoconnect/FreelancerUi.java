package com.example.geoconnect;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Small helper shared by the freelancer and shared screens:
 * window insets, the Back link and the bottom navigation bar.
 */
public final class FreelancerUi {

    private FreelancerUi() {}

    /** Keeps content clear of the status bar and navigation bar. */
    public static void applyInsets(View root) {
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
    }

    /** Makes the "Back" link (id tvBack) close the current screen. */
    public static void setupBack(Activity a) {
        View back = a.findViewById(R.id.tvBack);
        if (back != null) back.setOnClickListener(v -> a.finish());
    }

    /** Wires Home / Requests / Profile in the bottom bar. active = "Home", "Requests" or "Profile". */
    public static void setupNav(Activity a, String active) {
        bind(a, R.id.navHome, active, "Home", FreelancerDashboardActivity.class);
        bind(a, R.id.navRequests, active, "Requests", BookingRequestsActivity.class);
        bind(a, R.id.navProfile, active, "Profile", FreelancerProfileActivity.class);
    }

    private static void bind(Activity a, int id, String active, String name, Class<?> target) {
        View v = a.findViewById(id);
        if (v == null || name.equals(active)) return;
        v.setOnClickListener(x -> {
            Intent i = new Intent(a, target);
            if (name.equals("Home")) {
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            }
            a.startActivity(i);
            a.overridePendingTransition(0, 0);
            if (!active.equals("Home") && !name.equals("Home")) a.finish();
        });
    }
}