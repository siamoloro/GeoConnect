package com.example.geoconnect;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminReviewActivity extends AppCompatActivity {

    FirebaseFirestore db;
    LinearLayout reviewContainer;
    private String currentTab = "pending";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_review2);
        FreelancerUi.applyInsets(findViewById(android.R.id.content));

        db = FirebaseFirestore.getInstance();

        reviewContainer =
                findViewById(R.id.reviewContainer);

        Button btnPending =
                findViewById(R.id.btnPending);

        Button btnApproved =
                findViewById(R.id.btnResolved);

        Button btnRejected =
                findViewById(R.id.btnArchived);

        btnPending.setOnClickListener(v -> {

            loadPendingApplications();
            setActiveTab(btnPending);

        });

        loadPendingApplications();
        setActiveTab(btnPending);


        btnApproved.setOnClickListener(v -> {

            loadApprovedApplications();
            setActiveTab(btnApproved);

        });


        btnRejected.setOnClickListener(v -> {

            loadRejectedApplications();
            setActiveTab(btnRejected);

        });



        BottomNavigationView bottomNav =
                findViewById(R.id.bottomNav);

        bottomNav.setSelectedItemId(R.id.nav_review);

        bottomNav.setOnItemSelectedListener(item -> {

            if (item.getItemId() == R.id.nav_overview) {
                startActivity(new Intent(
                        AdminReviewActivity.this,
                        AdminDashboardActivity.class));
                return true;
            }

            if (item.getItemId() == R.id.nav_users) {
                startActivity(new Intent(
                        AdminReviewActivity.this,
                        AdminUsersActivity.class));
                return true;
            }

            if (item.getItemId() == R.id.nav_review) {
                return true;
            }

            if (item.getItemId() == R.id.nav_reports) {
                startActivity(new Intent(
                        AdminReviewActivity.this,
                        AdminReportsActivity.class));
                return true;
            }

            return false;
        });
    }

    private void setActiveTab(Button activeButton)
    {
        Button btnPending =
                findViewById(R.id.btnPending);

        Button btnApproved =
                findViewById(R.id.btnResolved);

        Button btnRejected =
                findViewById(R.id.btnArchived);

        btnPending.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.parseColor("#444444")));

        btnApproved.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.parseColor("#444444")));

        btnRejected.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.parseColor("#444444")));

        // Active button turns green
        activeButton.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.parseColor("#4CAF50")));
    }


    private void loadPendingApplications()
    {
        currentTab = "pending";

        reviewContainer.removeAllViews();

        db.collection("businesses")
                .whereEqualTo(
                        "approvalStatus",
                        "pending")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    for(QueryDocumentSnapshot document :
                            queryDocumentSnapshots)
                    {
                        addApplicationCard(
                                document.getId(),
                                document.getString("name"),
                                "Business",
                                true
                        );
                    }
                });

        db.collection("freelancers")
                .whereEqualTo(
                        "approvalStatus",
                        "pending")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    for(QueryDocumentSnapshot document :
                            queryDocumentSnapshots)
                    {
                        addApplicationCard(
                                document.getId(),
                                document.getString("fullName"),
                                "Freelancer",
                                false
                        );
                    }
                });
    }

    private void addApplicationCard(
            String documentId,
            String name,
            String type,
            boolean isBusiness) {
        LayoutInflater inflater =
                LayoutInflater.from(this);

        android.view.View card =
                inflater.inflate(
                        R.layout.item_review_card,
                        reviewContainer,
                        false);

        TextView txtName =
                card.findViewById(R.id.txtName);

        TextView txtType =
                card.findViewById(R.id.txtType);

        Button btnApprove =
                card.findViewById(R.id.btnApprove);

        Button btnReject =
                card.findViewById(R.id.btnReject);

        if(currentTab.equals("approved"))
        {
            btnApprove.setVisibility(android.view.View.GONE);
        }
        else if(currentTab.equals("rejected"))
        {
            btnReject.setVisibility(android.view.View.GONE);
        }

        txtName.setText(name);
        txtType.setText(type);

        btnApprove.setOnClickListener(v -> {

            String collection =
                    isBusiness
                            ? "businesses"
                            : "freelancers";

            db.collection(collection)
                    .document(documentId)
                    .update(
                            "approvalStatus",
                            "approved",
                            "reviewStatus",
                            "reviewed"
                    )
                    .addOnSuccessListener(unused -> {

                        Toast.makeText(
                                this,
                                "Approved",
                                Toast.LENGTH_SHORT
                        ).show();

                        loadPendingApplications();
                    });

        });

        btnReject.setOnClickListener(v -> {

            String collection =
                    isBusiness
                            ? "businesses"
                            : "freelancers";

            db.collection(collection)
                    .document(documentId)
                    .update(
                            "approvalStatus",
                            "rejected",
                            "reviewStatus",
                            "reviewed"
                    )
                    .addOnSuccessListener(unused -> {

                        Toast.makeText(
                                this,
                                "Rejected",
                                Toast.LENGTH_SHORT
                        ).show();

                        loadPendingApplications();
                    });
            });
            reviewContainer.addView(card);
        }

    private void loadApprovedApplications()
    {
        currentTab = "approved";
        reviewContainer.removeAllViews();

        db.collection("businesses")
                .whereEqualTo(
                        "approvalStatus",
                        "approved")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    for(QueryDocumentSnapshot document :
                            queryDocumentSnapshots)
                    {
                        addApplicationCard(
                                document.getId(),
                                document.getString("name"),
                                "Business",
                                true
                        );
                    }
                });

        db.collection("freelancers")
                .whereEqualTo(
                        "approvalStatus",
                        "approved")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    for(QueryDocumentSnapshot document :
                            queryDocumentSnapshots)
                    {
                        addApplicationCard(
                                document.getId(),
                                document.getString("fullName"),
                                "Freelancer",
                                false
                        );
                    }
                });
        }

    private void loadRejectedApplications()
    {
        currentTab = "rejected";
        reviewContainer.removeAllViews();

        db.collection("businesses")
                .whereEqualTo(
                        "approvalStatus",
                        "rejected")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    for(QueryDocumentSnapshot document :
                            queryDocumentSnapshots)
                    {
                        addApplicationCard(
                                document.getId(),
                                document.getString("name"),
                                "Business",
                                true
                        );
                    }
                });

        db.collection("freelancers")
                .whereEqualTo(
                        "approvalStatus",
                        "rejected")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    for(QueryDocumentSnapshot document :
                            queryDocumentSnapshots)
                    {
                        addApplicationCard(
                                document.getId(),
                                document.getString("fullName"),
                                "Freelancer",
                                false
                        );
                    }
                });
    }




}



