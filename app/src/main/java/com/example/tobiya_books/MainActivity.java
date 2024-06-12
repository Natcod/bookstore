package com.example.tobiya_books;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, BooksAdapter.OnBookClickListener, BookDetailFragment.MainActivityListener {

    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNavigationView;
    private FragmentManager fragmentManager;
    private Toolbar toolbar;
    private FloatingActionButton fab;
    private RecyclerView recyclerView;
    private BooksAdapter booksAdapter;
    private SharedPreferences sharedPreferences;
    private FirebaseFirestore db;

    private Dialog dialog;
    private static final String TAG = "MainActivity";
    private static final String TYPE_DAILY = "Daily";
    private static final String TYPE_WEEKLY = "Weekly";
    private static final String TYPE_MONTHLY = "Monthly";
    private static final String TYPE_YEARLY = "Yearly";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fab = findViewById(R.id.fab);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.navigation_drawer);
        navigationView.setNavigationItemSelectedListener(this);

        // Get the navigation header view
        View headerView = navigationView.getHeaderView(0);

        // Find the TextViews in the header
        TextView firstNameTextView = headerView.findViewById(R.id.firstName);
        TextView usernameTextView = headerView.findViewById(R.id.username);

        // Get firstName and username from SharedPreferences
        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String firstName = sharedPreferences.getString("FirstName", "FirstName");
        String username = sharedPreferences.getString("Username", "Username");

        // Set firstName and username in the header
        firstNameTextView.setText(firstName);
        usernameTextView.setText("@" + username);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setBackground(null);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.home) {
                openFragment(new HomeFragment());
                return true;
            } else if (itemId == R.id.store) {
                openFragment(new Store());
                return true;
            } else if (itemId == R.id.bookclub) {
                openFragment(new BookClub());
                return true;
            } else if (itemId == R.id.library) {
                openFragment(new Library());
                return true;
            }
            return false;
        });

        fragmentManager = getSupportFragmentManager();
        openFragment(new HomeFragment());

        fab.setOnClickListener(view -> showBottomDialog());

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        booksAdapter = new BooksAdapter(this, new ArrayList<>(), this);
        recyclerView.setAdapter(booksAdapter);

        db = FirebaseFirestore.getInstance();
    }

    private void openFragment(Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.nav_Profile) {
            openFragment(new Profile());
        } else if (itemId == R.id.nav_settings) {
            openFragment(new Setting());
        } else if (itemId == R.id.nav_about) {
            openFragment(new Aboutus());
        } else if (itemId == R.id.nav_logout) {
            showLogoutConfirmationDialog();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showLogoutConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to logout?");
        builder.setPositiveButton("Yes", (dialog, which) -> logout());
        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("LoggedIn", false);
        editor.putString("UserID", null);
        editor.apply();

        Intent intent = new Intent(MainActivity.this, MainActivity2.class); // Assuming MainActivity2 handles the login
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public void addPurchaseToDatabase() {
        // Implement the method to add purchase to the database
        // You can put the implementation here or call another method that handles this
        // For example:
        // db.collection("Purchase").add(purchase)...
    }

    @Override
    public void onBookClick(Book book) {
        // Open BookDetailFragment with the clicked book's details
        BookDetailFragment fragment = new BookDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("book", book);
        fragment.setArguments(bundle);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null); // Optional: Add fragment to back stack
        transaction.commit();
    }

    // Fetch the subscription prices from Firestore
    private void fetchSubscriptionPrices() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("SubscriptionDetail")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                            Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                            Double dailyPrice = document.getDouble("dailyPrice");
                            Double weeklyPrice = document.getDouble("weeklyPrice");
                            Double monthlyPrice = document.getDouble("monthlyPrice");
                            Double yearlyPrice = document.getDouble("yearlyPrice");

                            if (dailyPrice != null && weeklyPrice != null && monthlyPrice != null && yearlyPrice != null) {
                                // Update subscription prices
                                updateSubscriptionPrices(dailyPrice, weeklyPrice, monthlyPrice, yearlyPrice);
                            } else {
                                Log.d(TAG, "One or more subscription prices are null");
                            }
                        } else {
                            Log.d(TAG, "No documents found in the collection");
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                });
    }

    // Update the subscription prices in the bottom sheet dialog
    private void updateSubscriptionPrices(double dailyPrice, double weeklyPrice, double monthlyPrice, double yearlyPrice) {
        RadioButton dailyRadioButton = dialog.findViewById(R.id.radioButton_daily);
        RadioButton weeklyRadioButton = dialog.findViewById(R.id.radioButton_weekly);
        RadioButton monthlyRadioButton = dialog.findViewById(R.id.radioButton_monthly);
        RadioButton yearlyRadioButton = dialog.findViewById(R.id.radioButton_yearly);
        if (dailyRadioButton != null && weeklyRadioButton != null && monthlyRadioButton != null && yearlyRadioButton != null) {
            dailyRadioButton.setText(String.format("Daily - %.2f ETB", dailyPrice));
            weeklyRadioButton.setText(String.format("Weekly - %.2f ETB", weeklyPrice));
            monthlyRadioButton.setText(String.format("Monthly - %.2f ETB", monthlyPrice));
            yearlyRadioButton.setText(String.format("Yearly - %.2f ETB", yearlyPrice));
        } else {
            Log.e(TAG, "One or more radio buttons are null");
        }
    }

    // Show the bottom sheet dialog and fetch subscription prices
    public void showBottomDialog() {
        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottomsheetlayout);

        ImageView cancelButton = dialog.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(view -> dialog.dismiss());

        Button buyNowButton = dialog.findViewById(R.id.buyButton);
        buyNowButton.setOnClickListener(v -> {
            // Extract subscription price from the dialog
            // Assuming you have a method to retrieve the price, let's call it getSubscriptionPrice()
            double price = getSubscriptionPrice();

            // Retrieve the selected subscription type from the radio buttons
            String type = getSelectedSubscriptionType();

            // Add the subscription to the database
            addSubscriptionToDatabase(price, type);
        });

        fetchSubscriptionPrices(); // Fetch and update prices before showing the dialog

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private double getSubscriptionPrice() {
        RadioButton dailyRadioButton = dialog.findViewById(R.id.radioButton_daily);
        RadioButton weeklyRadioButton = dialog.findViewById(R.id.radioButton_weekly);
        RadioButton monthlyRadioButton = dialog.findViewById(R.id.radioButton_monthly);
        RadioButton yearlyRadioButton = dialog.findViewById(R.id.radioButton_yearly);

        if (dailyRadioButton != null && dailyRadioButton.isChecked()) {
            return extractPriceFromText(dailyRadioButton.getText().toString());
        } else if (weeklyRadioButton != null && weeklyRadioButton.isChecked()) {
            return extractPriceFromText(weeklyRadioButton.getText().toString());
        } else if (monthlyRadioButton != null && monthlyRadioButton.isChecked()) {
            return extractPriceFromText(monthlyRadioButton.getText().toString());
        } else if (yearlyRadioButton != null && yearlyRadioButton.isChecked()) {
            return extractPriceFromText(yearlyRadioButton.getText().toString());
        }

        // Return 0.0 if no radio button is checked or if there's an issue extracting the price
        return 0.0;
    }

    // Helper method to extract price from the radio button text
    private double extractPriceFromText(String text) {
        String[] parts = text.split(" - ");
        if (parts.length == 2) {
            try {
                return Double.parseDouble(parts[1].replace(" ETB", ""));
            } catch (NumberFormatException e) {
                Log.e(TAG, "Error parsing subscription price from text: " + e.getMessage());
            }
        }
        return 0.0;
    }

    private String getSelectedSubscriptionType() {
        RadioButton dailyRadioButton = dialog.findViewById(R.id.radioButton_daily);
        RadioButton weeklyRadioButton = dialog.findViewById(R.id.radioButton_weekly);
        RadioButton monthlyRadioButton = dialog.findViewById(R.id.radioButton_monthly);
        RadioButton yearlyRadioButton = dialog.findViewById(R.id.radioButton_yearly);

        if (dailyRadioButton != null && dailyRadioButton.isChecked()) {
            return TYPE_DAILY;
        } else if (weeklyRadioButton != null && weeklyRadioButton.isChecked()) {
            return TYPE_WEEKLY;
        } else if (monthlyRadioButton != null && monthlyRadioButton.isChecked()) {
            return TYPE_MONTHLY;
        } else if (yearlyRadioButton != null && yearlyRadioButton.isChecked()) {
            return TYPE_YEARLY;
        }

        return ""; // Default to empty string if no type is selected
    }

    // Add the subscription to the Firestore database
    private void addSubscriptionToDatabase(double price, String type) {
        // Retrieve the UserID from SharedPreferences
        String userID = sharedPreferences.getString("UserID", "");

        if (!userID.isEmpty()) {
            // Calculate endDate based on subscription type
            Timestamp startDate = Timestamp.now();
            Timestamp endDate = calculateEndDate(startDate, type);

            // Create a map to represent the subscription data
            HashMap<String, Object> subscriptionData = new HashMap<>();
            subscriptionData.put("startDate", startDate);
            subscriptionData.put("endDate", endDate);
            subscriptionData.put("price", price);
            subscriptionData.put("reader", FirebaseFirestore.getInstance().document("Reader/" + userID));
            subscriptionData.put("type", type);

            // Get reference to the Subscription collection
            CollectionReference subscriptionsRef = FirebaseFirestore.getInstance().collection("Subscription");

            // Add the subscription data to Firestore
            subscriptionsRef.add(subscriptionData)
                    .addOnSuccessListener(documentReference -> {
                        Log.d(TAG, "Subscription added with ID: " + documentReference.getId());
                        Toast.makeText(MainActivity.this, "Subscription added successfully!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error adding subscription", e);
                        Toast.makeText(MainActivity.this, "Failed to add subscription", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Log.e(TAG, "UserID is empty");
            Toast.makeText(MainActivity.this, "Failed to add subscription: UserID is empty", Toast.LENGTH_SHORT).show();
        }
    }

    // Calculate the end date of the subscription based on the start date and subscription type
    private Timestamp calculateEndDate(Timestamp startDate, String type) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate.toDate());

        switch (type) {
            case TYPE_DAILY:
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                break;
            case TYPE_WEEKLY:
                calendar.add(Calendar.WEEK_OF_YEAR, 1);
                break;
            case TYPE_MONTHLY:
                calendar.add(Calendar.MONTH, 1);
                break;
            case TYPE_YEARLY:
                calendar.add(Calendar.YEAR, 1);
                break;
            default:
                Log.e(TAG, "Invalid subscription type");
                break;
        }

        return new Timestamp(calendar.getTime());
    }
}
