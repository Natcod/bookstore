package com.example.tobiya_books;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;

public class SubscriptionRepository {
    private static final String TAG = "SubscriptionRepository";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void isReaderSubscribed(String readerId, SubscriptionCallback callback) {
        Log.d(TAG, "Checking subscription status for reader ID: " + readerId);

        db.collection("Subscription")
                .whereEqualTo("reader", db.document("Reader/" + readerId))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Query to Subscription collection was successful");
                        QuerySnapshot result = task.getResult();
                        if (result != null && !result.isEmpty()) {
                            Log.d(TAG, "Subscription documents found for reader ID: " + readerId);
                            boolean isSubscribed = false;
                            for (QueryDocumentSnapshot document : result) {
                                String approvalStatus = document.getString("approvalStatus");
                                Date endDate = document.getTimestamp("endDate").toDate();
                                Log.d(TAG, "Checking document: " + document.getId() + ", approvalStatus: " + approvalStatus + ", endDate: " + endDate);

                                if (("approved".equals(approvalStatus) || "pending".equals(approvalStatus)) && (endDate == null || endDate.after(new Date()))) {
                                    Log.d(TAG, "Subscription is approved or pending and valid");
                                    isSubscribed = true;
                                    break;
                                }
                            }
                            callback.onResult(isSubscribed);
                        } else {
                            Log.d(TAG, "No subscription documents found for reader ID: " + readerId);
                            callback.onResult(false);
                        }
                    } else {
                        Log.e(TAG, "Query to Subscription collection failed", task.getException());
                        callback.onResult(false);
                    }
                });
    }

    public interface SubscriptionCallback {
        void onResult(boolean isSubscribed);
    }
}
