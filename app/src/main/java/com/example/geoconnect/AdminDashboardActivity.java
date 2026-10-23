package com.example.geoconnect;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;

import android.content.Intent;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminDashboardActivity extends AppCompatActivity {

    FirebaseFirestore db;

    TextView txtTotalUsers;
    TextView txtActiveListings;
    TextView txtVerificationRequests;
    TextView txtOpenReports;
    Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_dashboard);

        db = FirebaseFirestore.getInstance();

        txtTotalUsers =
                findViewById(R.id.txtTotalUsers);

        txtActiveListings =
                findViewById(R.id.txtActiveListings);

        txtVerificationRequests =
                findViewById(R.id.txtVerificationRequests);

        txtOpenReports =
                findViewById(R.id.txtOpenReports);

        btnLogout =
                findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(v -> {

            FirebaseAuth.getInstance().signOut();

            Intent intent =
                    new Intent(
                            AdminDashboardActivity.this,
                            AdminSigninActivity.class);

            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);

            finish();

        });

        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    txtTotalUsers.setText(
                            String.valueOf(
                                    queryDocumentSnapshots.size()));
                });

        db.collection("businesses")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    txtActiveListings.setText(
                            String.valueOf(
                                    queryDocumentSnapshots.size()));
                });

        db.collection("reports")
                .whereEqualTo(
                        "status",
                        "Pending")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    txtOpenReports.setText(
                            String.valueOf(
                                    queryDocumentSnapshots.size()));
                });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        BottomNavigationView bottomNav =
                findViewById(R.id.bottom_navigation);

        bottomNav.setSelectedItemId(R.id.nav_overview);

        bottomNav.setOnItemSelectedListener(item -> {

            if (item.getItemId() == R.id.nav_overview) {
                return true;
            }

            if (item.getItemId() == R.id.nav_users) {
                startActivity(new Intent(
                        AdminDashboardActivity.this,
                        AdminUsersActivity.class));
                return true;
            }

            if (item.getItemId() == R.id.nav_review) {
                startActivity(new Intent(
                        AdminDashboardActivity.this,
                        AdminReviewActivity.class));
                return true;
            }

            if (item.getItemId() == R.id.nav_reports) {
                startActivity(new Intent(
                        AdminDashboardActivity.this,
                        AdminReportsActivity.class));
                return true;
            }

            return false;
        });
        loadVerificationRequests();
    }
    private void loadVerificationRequests()
    {
        db.collection("businesses")
                .whereEqualTo(
                        "approvalStatus",
                        "pending")
                .get()
                .addOnSuccessListener(businessSnapshots -> {

                    int businessCount =
                            businessSnapshots.size();

                    db.collection("freelancers")
                            .whereEqualTo(
                                    "approvalStatus",
                                    "pending")
                            .get()
                            .addOnSuccessListener(
                                    freelancerSnapshots -> {

                                        int total =
                                                businessCount +
                                                        freelancerSnapshots.size();

                                        txtVerificationRequests.setText(
                                                String.valueOf(total));
                                    });
                });
    }
}