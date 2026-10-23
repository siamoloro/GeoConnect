package com.example.geoconnect;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminReportsActivity extends AppCompatActivity {

    FirebaseFirestore db;

    TextView txtPendingCount;
    TextView txtResolvedCount;

    LinearLayout reportsContainer;

    Button btnPending;
    Button btnResolved;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_reports2);

        db = FirebaseFirestore.getInstance();

        txtPendingCount =
                findViewById(R.id.txtPendingCount);

        txtResolvedCount =
                findViewById(R.id.txtResolvedCount);

        reportsContainer =
                findViewById(R.id.reportsContainer);

        btnPending =
                findViewById(R.id.btnPending);

        btnResolved =
                findViewById(R.id.btnResolved);

        btnPending.setOnClickListener(v ->
        {
            showPendingTab();
        });

        btnResolved.setOnClickListener(v ->
        {
            showResolvedTab();
        });

        db.collection("reports")
                .whereEqualTo("status", "Pending")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    txtPendingCount.setText(
                            String.valueOf(
                                    queryDocumentSnapshots.size()));
                });

        db.collection("reports")
                .whereEqualTo("status", "Resolved")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    txtResolvedCount.setText(
                            String.valueOf(
                                    queryDocumentSnapshots.size()));
                });

        BottomNavigationView bottomNav =
                findViewById(R.id.bottomNav);

        bottomNav.setSelectedItemId(R.id.nav_reports);

        bottomNav.setOnItemSelectedListener(item -> {

            if (item.getItemId() == R.id.nav_overview) {
                startActivity(new Intent(
                        AdminReportsActivity.this,
                        AdminDashboardActivity.class));
                return true;
            }

            if (item.getItemId() == R.id.nav_users) {
                startActivity(new Intent(
                        AdminReportsActivity.this,
                        AdminUsersActivity.class));
                return true;
            }

            if (item.getItemId() == R.id.nav_review) {
                startActivity(new Intent(
                        AdminReportsActivity.this,
                        AdminReviewActivity.class));
                return true;
            }

            if (item.getItemId() == R.id.nav_reports) {
                return true;
            }

            return false;
        });

        showPendingTab();
    }

    private void loadPendingReports()
    {
        reportsContainer.removeAllViews();

        db.collection("reports")
                .whereEqualTo("status", "Pending")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    for(QueryDocumentSnapshot document :
                            queryDocumentSnapshots)
                    {
                        String email =
                                document.getString("email");

                        String reason =
                                document.getString("reason");

                        LinearLayout reportLayout =
                                new LinearLayout(this);

                        reportLayout.setOrientation(
                                LinearLayout.VERTICAL);

                        TextView tv = new TextView(this);

                        tv.setText(
                                email +
                                        "\n" +
                                        reason);

                        tv.setTextColor(
                                getResources().getColor(
                                        android.R.color.white));

                        tv.setPadding(
                                20,
                                20,
                                20,
                                20);

                        Button btnResolve =
                                new Button(this);

                        btnResolve.setText("Resolve");

                        reportLayout.addView(tv);
                        reportLayout.addView(btnResolve);

                        reportsContainer.addView(reportLayout);

                        btnResolve.setOnClickListener(v -> {

                            db.collection("reports")
                                    .document(document.getId())
                                    .update(
                                            "status",
                                            "Resolved")
                                    .addOnSuccessListener(unused -> {

                                        Toast.makeText(
                                                        this,
                                                        "Report Resolved",
                                                        Toast.LENGTH_SHORT)
                                                .show();

                                        loadPendingReports();

                                        db.collection("reports")
                                                .whereEqualTo(
                                                        "status",
                                                        "Pending")
                                                .get()
                                                .addOnSuccessListener(
                                                        snapshots ->
                                                                txtPendingCount.setText(
                                                                        String.valueOf(
                                                                                snapshots.size())));

                                        db.collection("reports")
                                                .whereEqualTo(
                                                        "status",
                                                        "Resolved")
                                                .get()
                                                .addOnSuccessListener(
                                                        snapshots ->
                                                                txtResolvedCount.setText(
                                                                        String.valueOf(
                                                                                snapshots.size())));
                                    });
                        });
                    }
                });
    }

    private void loadResolvedReports()
    {
        reportsContainer.removeAllViews();

        db.collection("reports")
                .whereEqualTo("status", "Resolved")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    for(QueryDocumentSnapshot document :
                            queryDocumentSnapshots)
                    {
                        String email =
                                document.getString("email");

                        String reason =
                                document.getString("reason");

                        TextView tv =
                                new TextView(this);

                        tv.setText(
                                email +
                                        "\n" +
                                        reason);

                        tv.setTextColor(
                                getResources().getColor(
                                        android.R.color.white));

                        tv.setPadding(
                                20,
                                20,
                                20,
                                20);

                        reportsContainer.addView(tv);
                    }
                });
    }

    private void showPendingTab()
    {
        btnPending.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.parseColor("#4CAF50")));

        btnResolved.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.parseColor("#444444")));

        loadPendingReports();
    }

    private void showResolvedTab()
    {
        btnResolved.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.parseColor("#4CAF50")));

        btnPending.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.parseColor("#444444")));

        loadResolvedReports();
    }
}