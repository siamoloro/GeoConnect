package com.example.geoconnect;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class BusinessOfferingsActivity extends AppCompatActivity implements ProductAdapter.OnProductListener {

    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private List<Product> productList;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_business_offerings);
        FreelancerUi.applyInsets(findViewById(android.R.id.content));

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            Toast.makeText(this, "User session not found. Please log in again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        FloatingActionButton fabAddProduct = findViewById(R.id.fabAddProduct);
        fabAddProduct.setOnClickListener(v -> {
            Intent intent = new Intent(BusinessOfferingsActivity.this, BusinessAddOfferingActivity.class);
            startActivity(intent);
        });

        recyclerView = findViewById(R.id.recyclerViewProducts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(productList, this);
        recyclerView.setAdapter(productAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadProductsFromDatabase();
    }

    private void loadProductsFromDatabase() {
        // Path: businesses/{userId}/offerings
        db.collection("businesses").document(userId)
                .collection("offerings")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    productList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String offeringId = document.getId();
                        String name = document.getString("name");
                        String description = document.getString("description");

                        double price = 0.0;
                        Object priceObj = document.get("price");
                        if (priceObj instanceof Number) {
                            price = ((Number) priceObj).doubleValue();
                        } else if (priceObj instanceof String) {
                            try {
                                price = Double.parseDouble((String) priceObj);
                            } catch (NumberFormatException ignored) {}
                        }

                        Product product = new Product(offeringId, name, description, price);
                        productList.add(product);
                    }
                    productAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(BusinessOfferingsActivity.this, "Failed to load offerings: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onEditClick(int position) {

    }

    @Override
    public void onDeleteClick(int position) {
        Product clickedProduct = productList.get(position);
        String offeringId = clickedProduct.getId();

        db.collection("businesses").document(userId)
                .collection("offerings").document(offeringId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    db.collection("businesses").document(userId).update("services", FieldValue.arrayRemove(clickedProduct.getName()));
                    // Look the item up again: the list may have changed while the delete was in flight
                    int index = productList.indexOf(clickedProduct);
                    if (index >= 0) {
                        productList.remove(index);
                        productAdapter.notifyItemRemoved(index);
                    }
                    Toast.makeText(BusinessOfferingsActivity.this, "Offering deleted successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(BusinessOfferingsActivity.this, "Failed to delete offering: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}