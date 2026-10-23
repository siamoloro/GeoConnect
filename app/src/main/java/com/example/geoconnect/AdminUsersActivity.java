package com.example.geoconnect;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminUsersActivity extends AppCompatActivity {

    FirebaseFirestore db;
    LinearLayout usersContainer;

    Button btnAll;
    Button btnCustomers;
    Button btnBusinesses;
    Button btnFreelancers;
    EditText etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_users2);
        FreelancerUi.applyInsets(findViewById(android.R.id.content));

        db = FirebaseFirestore.getInstance();

        BottomNavigationView bottomNav =
                findViewById(R.id.bottomNav);

        bottomNav.setSelectedItemId(R.id.nav_users);

        bottomNav.setOnItemSelectedListener(item -> {

            if (item.getItemId() == R.id.nav_overview) {
                startActivity(new Intent(
                        AdminUsersActivity.this,
                        AdminDashboardActivity.class));
                return true;
            }

            if (item.getItemId() == R.id.nav_users) {
                return true;
            }

            if (item.getItemId() == R.id.nav_review) {
                startActivity(new Intent(
                        AdminUsersActivity.this,
                        AdminReviewActivity.class));
                return true;
            }

            if (item.getItemId() == R.id.nav_reports) {
                startActivity(new Intent(
                        AdminUsersActivity.this,
                        AdminReportsActivity.class));
                return true;
            }

            return false;
        });

        usersContainer =
                findViewById(R.id.usersContainer);

        etSearch =
                findViewById(R.id.etSearch);

        btnAll =
                findViewById(R.id.btnAll);

        btnCustomers =
                findViewById(R.id.btnCustomers);

        btnBusinesses =
                findViewById(R.id.btnBusinesses);

        btnFreelancers =
                findViewById(R.id.btnFreelancers);

        loadAllUsers();

// Filter buttons
        btnAll.setOnClickListener(v -> {
            selectTab(btnAll);
            loadAllUsers();
        });

        btnCustomers.setOnClickListener(v -> {
            selectTab(btnCustomers);
            loadCustomers();
        });

        btnBusinesses.setOnClickListener(v -> {
            selectTab(btnBusinesses);
            loadBusinesses();
        });

        btnFreelancers.setOnClickListener(v -> {
            selectTab(btnFreelancers);
            loadFreelancers();
        });

        etSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count) {

                searchUsers(
                        s.toString().trim());
            }

            @Override
            public void afterTextChanged(
                    Editable s) {
            }
        });

    }

    private void loadAllUsers()
    {
        usersContainer.removeAllViews();

        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    for(QueryDocumentSnapshot document :
                            queryDocumentSnapshots)
                    {
                        String email =
                                document.getString("email");

                        String status =
                                document.getString("status");

                        String documentId =
                                document.getId();

                        addUserCard(
                                documentId,
                                email,
                                status
                        );
                    }
                });
    }

    private void loadCustomers()
    {
        usersContainer.removeAllViews();

        db.collection("users")
                .whereEqualTo("type", "customer")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    for(QueryDocumentSnapshot document :
                            queryDocumentSnapshots)
                    {
                        addUserCard(
                                document.getId(),
                                document.getString("email"),
                                document.getString("status")
                        );
                    }
                });
    }

    private void loadBusinesses()
    {
        usersContainer.removeAllViews();

        db.collection("users")
                .whereEqualTo("type", "business")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    for(QueryDocumentSnapshot document :
                            queryDocumentSnapshots)
                    {
                        addUserCard(
                                document.getId(),
                                document.getString("email"),
                                document.getString("status")
                        );
                    }
                });
    }

    private void loadFreelancers()
    {
        usersContainer.removeAllViews();

        db.collection("users")
                .whereEqualTo("type", "freelancer")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    for(QueryDocumentSnapshot document :
                            queryDocumentSnapshots)
                    {
                        addUserCard(
                                document.getId(),
                                document.getString("email"),
                                document.getString("status")
                        );
                    }
                });
    }

    private void selectTab(Button selectedButton) {

        btnAll.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.parseColor("#444444")));

        btnCustomers.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.parseColor("#444444")));

        btnBusinesses.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.parseColor("#444444")));

        btnFreelancers.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.parseColor("#444444")));

        selectedButton.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.parseColor("#4CAF50")));
    }

    private void addUserCard(String documentId,
                             String email,
                             String status) {

        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.VERTICAL);

        card.setPadding(
                20,20,20,20);

        TextView txtName =
                new TextView(this);

        txtName.setText(email);

        txtName.setTextSize(18);

        txtName.setTextColor(
                getResources().getColor(
                        android.R.color.white));

        TextView txtStatus =
                new TextView(this);

        txtStatus.setText(
                "Status: " + status);

        txtStatus.setTextColor(
                getResources().getColor(
                        android.R.color.white));

        Button btnActivate =
                new Button(this);

        btnActivate.setText(
                "Activate");

        btnActivate.setOnClickListener(v -> {

            db.collection("users")
                    .document(documentId)
                    .update(
                            "status",
                            "active")
                    .addOnSuccessListener(unused -> {

                        loadAllUsers();

                        Toast.makeText(
                                        this,
                                        "User Activated",
                                        Toast.LENGTH_SHORT)
                                .show();
                    });
        });

        Button btnSuspend =
                new Button(this);

        btnSuspend.setText(
                "Suspend");

        btnSuspend.setOnClickListener(v -> {

            db.collection("users")
                    .document(documentId)
                    .update(
                            "status",
                            "suspended")
                    .addOnSuccessListener(unused -> {

                        loadAllUsers();

                        Toast.makeText(
                                        this,
                                        "User Suspended",
                                        Toast.LENGTH_SHORT)
                                .show();
                    });
        });


        card.addView(txtName);
        card.addView(txtStatus);
        card.addView(btnActivate);
        card.addView(btnSuspend);

        usersContainer.addView(card);
    }
    private void searchUsers(String searchText)
    {
        usersContainer.removeAllViews();

        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    for (QueryDocumentSnapshot document :
                            queryDocumentSnapshots)
                    {
                        String email =
                                document.getString("email");

                        String status =
                                document.getString("status");

                        if (email != null &&
                                email.toLowerCase()
                                        .contains(
                                                searchText.toLowerCase()))
                        {
                            addUserCard(
                                    document.getId(),
                                    email,
                                    status);
                        }
                    }
                });
    }
}