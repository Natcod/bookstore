package com.example.tobiya_books;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class FirestoreNotificationHelper {

    private static final String TAG = "FirestoreNotification";
    private static final String CHANNEL_ID = "firebase_channel";
    private static final int NOTIFICATION_ID = 1; // Use a constant ID to overwrite previous notification
    private Context context;
    private FirebaseFirestore db;

    public FirestoreNotificationHelper(Context context) {
        this.context = context;
        db = FirebaseFirestore.getInstance();
        createNotificationChannel();
        // Start listening for new notifications when the helper is instantiated
        listenForNewNotifications();
    }

    public void fetchAndDisplayNotifications() {
        db.collection("Notification")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        String title = document.getString("title");
                        String message = document.getString("message");
                        Log.d(TAG, "Fetched notification: " + title + " - " + message);
                        displayNotification(title, message);
                    } else {
                        Log.e(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }

    public void listenForNewNotifications() {
        db.collection("Notification")
                .orderBy("sent", Query.Direction.DESCENDING)
                .limit(1)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Listen failed.", e);
                        return;
                    }

                    if (snapshots != null && !snapshots.isEmpty()) {
                        DocumentSnapshot document = snapshots.getDocuments().get(0);
                        String title = document.getString("title");
                        String message = document.getString("message");
                        Log.d(TAG, "New notification received: " + title + " - " + message);
                        displayNotification(title, message);
                    } else {
                        Log.d(TAG, "No new notifications.");
                    }
                });
    }

    private void displayNotification(String title, String message) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.notification) // Set your notification icon
                        .setContentTitle(title)
                        .setContentText(message)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        try {
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build()); // Use a constant ID
        } catch (SecurityException e) {
            Log.e(TAG, "Failed to display notification due to security exception: " + e.getMessage());
            // Handle the exception gracefully, possibly by informing the user
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Channel human readable title";
            String description = "Channel description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
