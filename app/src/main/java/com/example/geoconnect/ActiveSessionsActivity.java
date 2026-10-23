package com.example.geoconnect;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ActiveSessionsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private final List<Session> sessions = new ArrayList<>();
    private SessionAdapter adapter;
    private String currentSessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_active_sessions);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        SharedPreferences prefs = getSharedPreferences("geoconnect", MODE_PRIVATE);
        currentSessionId = prefs.getString("sessionId", "");

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        RecyclerView rv = findViewById(R.id.rvSessions);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SessionAdapter(sessions, currentSessionId, this::revoke);
        rv.setAdapter(adapter);

        findViewById(R.id.btnRevokeAll).setOnClickListener(v -> revokeAllOthers());

        loadSessions();
    }

    private void loadSessions() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("sessions")
                .whereEqualTo("userID", uid)
                .get()
                .addOnSuccessListener(snap -> {
                    sessions.clear();
                    for (QueryDocumentSnapshot d : snap) {
                        sessions.add(new Session(
                                d.getId(),
                                d.getString("deviceName") != null ? d.getString("deviceName") : "Unknown device",
                                d.getString("locationInfo") != null ? d.getString("locationInfo") : ""));
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load sessions: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void revoke(Session s) {
        db.collection("sessions").document(s.getId()).delete()
                .addOnSuccessListener(a -> {
                    Toast.makeText(this, "Session revoked", Toast.LENGTH_SHORT).show();
                    loadSessions();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void revokeAllOthers() {
        for (Session s : new ArrayList<>(sessions)) {
            if (!s.getId().equals(currentSessionId)) {
                db.collection("sessions").document(s.getId()).delete();
            }
        }
        Toast.makeText(this, "Other devices logged out", Toast.LENGTH_SHORT).show();
        loadSessions();
    }
}