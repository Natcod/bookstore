package com.example.tobiya_books;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationReceiver extends BroadcastReceiver {

    private static final String TAG = "NotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Received alarm trigger");

        // Initiate fetching and displaying notifications here
        FirestoreNotificationHelper notificationHelper = new FirestoreNotificationHelper(context);
        notificationHelper.fetchAndDisplayNotifications();
    }
}
