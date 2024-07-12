package com.example.tobiya_books;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class SubscriptionManager {

    private static final String PREF_NAME = "subscription_preferences";
    private static final String KEY_SUBSCRIPTION_TYPE = "subscription_type";
    private static final String KEY_SUBSCRIPTION_ID = "subscription_id";
    private static final String KEY_DAILY_COUNT = "daily_count";
    private static final String KEY_WEEKLY_COUNT = "weekly_count";
    private static final String KEY_MONTHLY_COUNT = "monthly_count";
    private static final String KEY_YEARLY_COUNT = "yearly_count";
    private static final String TAG = "SubscriptionManager";

    private static final String KEY_DAILY_NUMBER_BOOK = "dailyNumberBook";
    private static final String KEY_WEEKLY_NUMBER_BOOK = "weeklyNumberBook";
    private static final String KEY_MONTHLY_NUMBER_BOOK = "monthlyNumberBook";
    private static final String KEY_YEARLY_NUMBER_BOOK = "yearlyNumberBook";

    private static final String KEY_ADDED_BOOKS = "added_books";

    private SharedPreferences sharedPreferences;

    public SubscriptionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void updateSubscriptionPrices(double dailyNumberBook, double weeklyNumberBook, double monthlyNumberBook, double yearlyNumberBook) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(KEY_DAILY_NUMBER_BOOK, (float) dailyNumberBook);
        editor.putFloat(KEY_WEEKLY_NUMBER_BOOK, (float) weeklyNumberBook);
        editor.putFloat(KEY_MONTHLY_NUMBER_BOOK, (float) monthlyNumberBook);
        editor.putFloat(KEY_YEARLY_NUMBER_BOOK, (float) yearlyNumberBook);
        editor.apply();
    }

    public void updateSubscriptionType(String subscriptionType, String subscriptionId) {
        // Retrieve the current subscription ID from SharedPreferences
        String currentSubscriptionId = sharedPreferences.getString(KEY_SUBSCRIPTION_ID, null);

        if (currentSubscriptionId == null || !currentSubscriptionId.equals(subscriptionId)) {
            resetSubscriptionCounters(subscriptionId);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_SUBSCRIPTION_TYPE, subscriptionType);
            editor.putString(KEY_SUBSCRIPTION_ID, subscriptionId);
            editor.putInt(subscriptionId + "_" + KEY_DAILY_COUNT, 0);
            editor.putInt(subscriptionId + "_" + KEY_WEEKLY_COUNT, 0);
            editor.putInt(subscriptionId + "_" + KEY_MONTHLY_COUNT, 0);
            editor.putInt(subscriptionId + "_" + KEY_YEARLY_COUNT, 0);
            editor.apply();

            Log.d(TAG, "Updated subscription type in SharedPreferences: " + subscriptionType);
            Log.d(TAG, "Updated subscription ID in SharedPreferences: " + subscriptionId);
        } else {
            Log.d(TAG, "Subscription ID already exists in SharedPreferences: " + subscriptionId);
        }
    }

    public boolean canAddBook(Date endDate) {
        String currentSubscription = sharedPreferences.getString(KEY_SUBSCRIPTION_TYPE, "none");
        String subscriptionId = sharedPreferences.getString(KEY_SUBSCRIPTION_ID, null);
        Log.d(TAG, "Current subscription type: " + currentSubscription);
        Log.d(TAG, "Current subscription ID: " + subscriptionId);
        Date currentDate = new Date();

        if (endDate.before(currentDate) || subscriptionId == null) {
            Log.e(TAG, "Subscription expired or ID is null");
            resetSubscriptionCounters(subscriptionId); // Reset counters for expired subscription
            return false;
        }

        switch (currentSubscription.toLowerCase()) {
            case "daily":
                return checkBookCount(subscriptionId + "_" + KEY_DAILY_COUNT, KEY_DAILY_NUMBER_BOOK);
            case "weekly":
                return checkBookCount(subscriptionId + "_" + KEY_WEEKLY_COUNT, KEY_WEEKLY_NUMBER_BOOK);
            case "monthly":
                return checkBookCount(subscriptionId + "_" + KEY_MONTHLY_COUNT, KEY_MONTHLY_NUMBER_BOOK);
            case "yearly":
                return checkBookCount(subscriptionId + "_" + KEY_YEARLY_COUNT, KEY_YEARLY_NUMBER_BOOK);
            default:
                Log.e(TAG, "Unknown subscription type: " + currentSubscription);
                return false;
        }
    }

    private boolean checkBookCount(String countKey, String limitKey) {
        int currentCount = sharedPreferences.getInt(countKey, 0);
        int bookLimit = (int) sharedPreferences.getFloat(limitKey, 0);
        Log.d(TAG, "Checking book count: current count = " + currentCount + ", limit = " + bookLimit);
        return currentCount < bookLimit;
    }

    public void addBookToLibrary(String bookId) {
        String currentSubscription = sharedPreferences.getString(KEY_SUBSCRIPTION_TYPE, "none");
        String subscriptionId = sharedPreferences.getString(KEY_SUBSCRIPTION_ID, null);

        if (subscriptionId != null) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            switch (currentSubscription.toLowerCase()) {
                case "daily":
                    if (updateBookCount(editor, subscriptionId + "_" + KEY_DAILY_COUNT, bookId, KEY_DAILY_NUMBER_BOOK)) {
                        resetSubscription(subscriptionId);
                    }
                    break;
                case "weekly":
                    if (updateBookCount(editor, subscriptionId + "_" + KEY_WEEKLY_COUNT, bookId, KEY_WEEKLY_NUMBER_BOOK)) {
                        resetSubscription(subscriptionId);
                    }
                    break;
                case "monthly":
                    if (updateBookCount(editor, subscriptionId + "_" + KEY_MONTHLY_COUNT, bookId, KEY_MONTHLY_NUMBER_BOOK)) {
                        resetSubscription(subscriptionId);
                    }
                    break;
                case "yearly":
                    if (updateBookCount(editor, subscriptionId + "_" + KEY_YEARLY_COUNT, bookId, KEY_YEARLY_NUMBER_BOOK)) {
                        resetSubscription(subscriptionId);
                    }
                    break;
                default:
                    Log.e(TAG, "Unknown subscription type: " + currentSubscription);
                    break;
            }
        } else {
            Log.e(TAG, "Subscription ID is null");
        }
    }

    private boolean updateBookCount(SharedPreferences.Editor editor, String countKey, String bookId, String limitKey) {
        int currentCount = sharedPreferences.getInt(countKey, 0);
        int bookLimit = (int) sharedPreferences.getFloat(limitKey, 0);

        Log.d(TAG, "Updating book count: current count = " + currentCount + ", limit = " + bookLimit);

        if (currentCount >= bookLimit) {
            return true; // Limit reached, reset required
        }

        currentCount++; // Increment the count
        editor.putInt(countKey, currentCount);
        editor.apply(); // Ensure apply is called immediately after updating the count

        storeBookDetails(bookId);
        Log.d(TAG, "Books added for " + countKey + ": " + currentCount);
        return false; // Limit not yet reached
    }

    private void resetSubscription(String subscriptionId) {
        if (subscriptionId != null) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_SUBSCRIPTION_TYPE, null);
            editor.putString(KEY_SUBSCRIPTION_ID, null);
            resetSubscriptionCounters(subscriptionId);
            editor.apply();
            Log.d(TAG, "Subscription ID set to null for subscription: " + subscriptionId);
        } else {
            Log.e(TAG, "Subscription ID is null, cannot reset subscription");
        }
    }

    private void storeBookDetails(String bookId) {
        Set<String> addedBooks = sharedPreferences.getStringSet(KEY_ADDED_BOOKS, new HashSet<>());
        addedBooks.add(bookId);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(KEY_ADDED_BOOKS, addedBooks);
        editor.apply();
    }

    public Set<String> getAddedBooks() {
        return sharedPreferences.getStringSet(KEY_ADDED_BOOKS, new HashSet<>());
    }

    public void displayBookCounts() {
        String subscriptionId = sharedPreferences.getString(KEY_SUBSCRIPTION_ID, null);
        if (subscriptionId != null) {
            int dailyCount = sharedPreferences.getInt(subscriptionId + "_" + KEY_DAILY_COUNT, 0);
            int weeklyCount = sharedPreferences.getInt(subscriptionId + "_" + KEY_WEEKLY_COUNT, 0);
            int monthlyCount = sharedPreferences.getInt(subscriptionId + "_" + KEY_MONTHLY_COUNT, 0);
            int yearlyCount = sharedPreferences.getInt(subscriptionId + "_" + KEY_YEARLY_COUNT, 0);

            Log.d(TAG, "Daily Books Added: " + dailyCount);
            Log.d(TAG, "Weekly Books Added: " + weeklyCount);
            Log.d(TAG, "Monthly Books Added: " + monthlyCount);
            Log.d(TAG, "Yearly Books Added: " + yearlyCount);
        } else {
            Log.e(TAG, "Subscription ID is null");
        }
    }

    private void resetSubscriptionCounters(String subscriptionId) {
        if (subscriptionId != null) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(subscriptionId + "_" + KEY_DAILY_COUNT, 0);
            editor.putInt(subscriptionId + "_" + KEY_WEEKLY_COUNT, 0);
            editor.putInt(subscriptionId + "_" + KEY_MONTHLY_COUNT, 0);
            editor.putInt(subscriptionId + "_" + KEY_YEARLY_COUNT, 0);
            editor.apply();
            Log.d(TAG, "Reset counters for expired subscription ID: " + subscriptionId);
        } else {
            Log.e(TAG, "Subscription ID is null, cannot reset counters");
        }
    }
}
