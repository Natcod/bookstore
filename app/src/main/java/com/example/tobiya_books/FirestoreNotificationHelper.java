package com.example.tobiya_books;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class FirestoreNotificationHelper {

    private static final String TAG = "FirestoreNotification";
    private static final String CHANNEL_ID = "firebase_channel";
    private Context context;
    private FirebaseFirestore db;
    private static final int ALARM_INTERVAL = 30 * 1000; // 30 seconds interval
    private static final int REQUEST_CODE_ALARM = 101;

    public FirestoreNotificationHelper(Context context) {
        this.context = context;
        db = FirebaseFirestore.getInstance();
        // Start listening for new notifications when the helper is instantiated
        listenForNewNotifications();
    }

    public void fetchAndDisplayNotifications() {
        db.collection("Notification")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String title = document.getString("title");
                            String message = document.getString("message");
                            displayNotification(title, message);
                        }
                    } else {
                        Log.e(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }

    public void listenForNewNotifications() {
        db.collection("Notification")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Listen failed.", e);
                        return;
                    }

                    if (snapshots != null && !snapshots.isEmpty()) {
                        for (QueryDocumentSnapshot doc : snapshots) {
                            String title = doc.getString("title");
                            String message = doc.getString("message");
                            displayNotification(title, message);
                        }
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Channel human readable title", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        try {
            notificationManager.notify((int) System.currentTimeMillis(), notificationBuilder.build());
        } catch (SecurityException e) {
            Log.e(TAG, "Failed to display notification due to security exception: " + e.getMessage());
            // Handle the exception gracefully, possibly by informing the user
        }
    }
}
