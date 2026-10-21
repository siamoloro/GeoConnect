package com.example.geoconnect;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

public class BusinessOfferingsActivity extends AppCompatActivity implements ProductAdapter.OnProductListener {

    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private List<Product> productList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_business_offerings);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerViewProducts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        productList = new ArrayList<>();

        productAdapter = new ProductAdapter(productList, this);
        recyclerView.setAdapter(productAdapter);

        loadProductsFromDatabase();

    }
    private void loadProductsFromDatabase() {

        productList.clear();

        // Adding sample records (Replace this loop with your actual DB query / Room / Firebase cursor data)
        productList.add(new Product("1", "Minimalist Chair", "Comfortable wooden stool for modern interiors.", 45.00));
        productList.add(new Product("2", "Pendant Light", "Warm brass ceiling fixture.", 120.50));
        productList.add(new Product("3", "Ceramic Vase", "Handmade matte-finish decorative pot.", 25.00));

        productAdapter.notifyDataSetChanged();
    }

    @Override
    public void onEditClick(int position) {
        Product clickedProduct = productList.get(position);
        // Handle edit action for clickedProduct.getId()
    }

    @Override
    public void onDeleteClick(int position) {
        Product clickedProduct = productList.get(position);
        // Handle delete action from database and update list
        productList.remove(position);
        productAdapter.notifyItemRemoved(position);
    }
}