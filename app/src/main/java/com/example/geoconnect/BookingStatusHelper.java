package com.example.geoconnect;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Updates a booking's status and tells the customer about it
 * by adding a document to users/{customerID}/notifications.
 */
public final class BookingStatusHelper {

    private BookingStatusHelper() {}

    public static void update(Context ctx, String bookingId, String newStatus, Runnable onSuccess) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference ref = db.collection("booking").document(bookingId);

        ref.get().addOnSuccessListener(doc -> {
            String customerId = doc.getString("customerID");
            String service = doc.getString("service");

            ref.update("status", newStatus)
                    .addOnSuccessListener(a -> {
                        if (customerId != null && !customerId.isEmpty()) {
                            String word = newStatus.toLowerCase(Locale.ROOT);
                            Map<String, Object> note = new HashMap<>();
                            note.put("title", "Booking " + word);
                            note.put("content", "Your " + (service != null && !service.isEmpty() ? service : "service")
                                    + " booking was " + word + ".");
                            note.put("timestamp", FieldValue.serverTimestamp());
                            note.put("read", false);
                            db.collection("users").document(customerId)
                                    .collection("notifications").add(note);
                        }
                        Toast.makeText(ctx, "Booking " + newStatus.toLowerCase(Locale.ROOT),
                                Toast.LENGTH_SHORT).show();
                        if (onSuccess != null) onSuccess.run();
                    })
                    .addOnFailureListener(e -> Toast.makeText(ctx,
                            "Error updating status: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }).addOnFailureListener(e -> Toast.makeText(ctx,
                "Could not find the booking: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}