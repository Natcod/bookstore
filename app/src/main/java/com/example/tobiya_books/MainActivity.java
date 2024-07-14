package com.example.tobiya_books;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import android.Manifest;

import androidx.lifecycle.ViewModelProvider;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, BooksAdapter.OnBookClickListener{

    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton floatingActionButton;
    private FragmentManager fragmentManager;
    private Toolbar toolbar;
    private FloatingActionButton fab;
    private RecyclerView recyclerView;
    private BooksAdapter booksAdapter;
    private SharedPreferences sharedPreferences;
    private FirebaseFirestore db;
    private SearchView searchView;

    private Dialog dialog;
    private static final String TAG = "MainActivity";
    private static final String TYPE_DAILY = "Daily";
    private static final String TYPE_WEEKLY = "Weekly";
    private static final String TYPE_MONTHLY = "Monthly";
    private static final String TYPE_YEARLY = "Yearly";
    private static final int REQUEST_CODE_POST_NOTIFICATIONS = 1;
    private static final int REQUEST_CODE_PERMISSION = 100;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 123; // Use any unique request code
    private static final int ALARM_INTERVAL = 12 * 60 * 60 * 1000; // 12 hours in milliseconds

    private ProfileViewModel profileViewModel;

    private static final int REQUEST_CODE_ALARM = 101;
    private FirestoreNotificationHelper notificationHelper;


    private TextView firstNameTextView;
    private TextView usernameTextView;
    private ImageView imageViewProfilePhoto;
    private TextView textViewInitial;

    private SubscriptionManager subscriptionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        initializeUI();

        subscriptionManager = new SubscriptionManager(this);
        // Fetch subscription prices from Firestore
        fetchSubscriptionPrices();

        // Handle incoming intent
        Intent intent = getIntent();
        if (intent != null) {
            Log.d("MainActivity", "Intent has extra 'book_id': " + intent.hasExtra("book_id"));
            if (intent.hasExtra("book_id")) {
                String bookId = intent.getStringExtra("book_id");
                if (bookId != null) {
                    openBookDetailFragment(bookId);
                }
            }
        }

        notificationHelper = new FirestoreNotificationHelper(this);

        // Request permissions and fetch notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            } else {
                notificationHelper.fetchUnreadNotifications();
                notificationHelper.listenForNewNotifications();
            }
        } else {
            notificationHelper.fetchUnreadNotifications();
            notificationHelper.listenForNewNotifications();
        }

        // Schedule alarm
        scheduleAlarm();

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);

        // Subscribe to notifications if not subscribed
        boolean isSubscribed = sharedPreferences.getBoolean("isSubscribedToNotifications", false);
        if (!isSubscribed) {
            FirebaseMessaging.getInstance().subscribeToTopic("all")
                    .addOnCompleteListener(task -> {
                        String msg = "Subscribed to notifications";
                        if (!task.isSuccessful()) {
                            msg = "Subscription failed";
                        } else {
                            sharedPreferences.edit().putBoolean("isSubscribedToNotifications", true).apply();
                        }
                        Log.d("MainActivity", msg);
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    });
        }

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Load initial fragment if savedInstanceState is null
        if (savedInstanceState == null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, new SignupTabFragment());
            fragmentTransaction.commit();
        }

        // Setup FAB, toolbar, and drawer layout
        fab = findViewById(R.id.fab);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.navigation_drawer);
        navigationView.setNavigationItemSelectedListener(this);



        // Setup bottom navigation view
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setBackground(null);
        floatingActionButton = findViewById(R.id.fab);
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

        // Load the default fragment
        fragmentManager = getSupportFragmentManager();
        openFragment(new HomeFragment());

        // Set FAB click listener
        fab.setOnClickListener(view -> showBottomDialog());

        // Setup RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        booksAdapter = new BooksAdapter(this, new ArrayList<>(), this);
        recyclerView.setAdapter(booksAdapter);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        String userId = sharedPreferences.getString("UserID", null);
        if (userId != null) {
            Log.d("MainActivity", "UserID found: " + userId);
            fetchUserProfile(userId);
        } else {
            Log.d("MainActivity", "UserID not found in SharedPreferences");
        }

    }
    private void initializeUI() {
        // Initialization logic for UI elements
        Log.d("MainActivity", "initializeUI called");
        NavigationView navigationView = findViewById(R.id.navigation_drawer);
        navigationView.setNavigationItemSelectedListener(this);

        // Initialize the header view
        View headerView = navigationView.getHeaderView(0);
        firstNameTextView = headerView.findViewById(R.id.firstName);
        usernameTextView = headerView.findViewById(R.id.username);
        imageViewProfilePhoto = headerView.findViewById(R.id.imageViewProfilePhoto);
        textViewInitial = headerView.findViewById(R.id.initial);

        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        profileViewModel.getProfileData().observe(this, document -> {
            if (document != null) {
                String firstName = document.getString("firstName");
                String lastName = document.getString("lastName");
                String username = document.getString("username");
                String profilePhotoUrl = document.getString("profilePhotoUrl");

                Log.d("MainActivity", "Profile data updated: firstName = " + firstName + ", username = " + username + ", profilePhotoUrl = " + profilePhotoUrl);

                if (firstNameTextView != null) firstNameTextView.setText(firstName);
                if (usernameTextView != null) usernameTextView.setText("@" + username);

                if (profilePhotoUrl != null && !profilePhotoUrl.isEmpty() && imageViewProfilePhoto != null) {
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageRef = storage.getReferenceFromUrl(profilePhotoUrl);

                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        if (!uri.toString().equals(imageViewProfilePhoto.getTag())) {
                            Glide.with(MainActivity.this)
                                    .load(uri)
                                    .placeholder(R.drawable.baseline_account_circle_24)
                                    .error(R.drawable.baseline_account_circle_24)
                                    .circleCrop()
                                    .into(imageViewProfilePhoto);
                            imageViewProfilePhoto.setTag(uri.toString());
                            textViewInitial.setVisibility(View.GONE);
                        }
                    }).addOnFailureListener(exception -> {
                        Toast.makeText(MainActivity.this, "Failed to fetch profile image", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    showInitials(firstName, lastName, textViewInitial, imageViewProfilePhoto);
                }
            } else {
                Log.d("MainActivity", "Profile document is null");
                Toast.makeText(MainActivity.this, "User profile not found", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void fetchUserProfile(String userId) {
        Log.d("MainActivity", "fetchUserProfile called with userId: " + userId);
        profileViewModel.fetchUserProfile(userId);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with scheduling the alarm
                scheduleAlarm();
            } else {
                // Permission denied, handle accordingly (e.g., show message or disable functionality)
            }
        }
    }
    private void scheduleAlarm() {
        Intent intent = new Intent(this, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, REQUEST_CODE_ALARM, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            // Check if the device supports exact alarms
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Check if your app has permission to schedule exact alarms
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            SystemClock.elapsedRealtime() + ALARM_INTERVAL,
                            pendingIntent);
                } else {
                    // Handle case where app doesn't have permission to schedule exact alarms
                    // You may want to inform the user or fallback to less precise timing
                    alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            SystemClock.elapsedRealtime() + ALARM_INTERVAL,
                            pendingIntent);
                }
            } else {
                // For versions below S (API 31), use setExact without checking permission
                alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() + ALARM_INTERVAL,
                        pendingIntent);
            }
        }
    }
    private void openFragment(Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }
    // Inside MainActivity
    public void openBookDetailFragment(String bookId) {
        // Fetch the Book object using the bookId
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference bookRef = db.document("Ebook/" + bookId);
        bookRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Book book = documentSnapshot.toObject(Book.class);
                if (book != null) {
                    openBookDetailFragment(book);
                } else {
                    Log.e(TAG, "Book is null");
                    // Handle null book error
                }
            } else {
                Log.e(TAG, "Document does not exist");
                // Handle document not exist error
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error getting book", e);
            // Handle failure to fetch book
        });
    }

    private void openBookDetailFragment(Book book) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, BookDetailFragment.newInstance(book));
        fragmentTransaction.addToBackStack(null); // Optional: Adds the transaction to the back stack
        fragmentTransaction.commit();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.nav_Profile) {
            openFragment(new Profile());
        }  else if (itemId == R.id.nav_about) {
            openFragment(new Aboutus());
        } else if (itemId == R.id.nav_logout) {
            showLogoutConfirmationDialog();
        } else if (itemId == R.id.nav_share) {
            onShareClicked(item);
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
    public void onShareClicked(MenuItem item) {
        String url = "http://com.example.tobiya_book";

        // Display the link using a Snackbar with a custom duration and background color
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), url, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("Copy", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Copy the link to the clipboard
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("URL", url);
                clipboard.setPrimaryClip(clip);
                snackbar.setText("Link copied to clipboard").setDuration(Snackbar.LENGTH_SHORT).show();
            }
        });

        // Set the background color of the Snackbar
        View snackbarView = snackbar.getView();
        int backgroundColor = ContextCompat.getColor(this, R.color.var);
        snackbarView.setBackgroundColor(backgroundColor);

        snackbar.show();
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();


        searchItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                searchView.setIconified(false);
                searchView.requestFocus();
                return true;
            }
        });

        setupSearchView();


        return true;
    }
    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                searchView.setQuery("", false);
                searchView.clearFocus();
                bottomNavigationView.setVisibility(View.VISIBLE);  // Show the bottom navigation when search is done
                floatingActionButton.setVisibility(View.VISIBLE);  // Show the FAB when search is done
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    bottomNavigationView.setVisibility(View.GONE);  // Hide the bottom navigation when search is focused
                    floatingActionButton.setVisibility(View.GONE);  // Hide the FAB when search is focused
                } else {
                    bottomNavigationView.setVisibility(View.VISIBLE);  // Show the bottom navigation when search loses focus
                    floatingActionButton.setVisibility(View.VISIBLE);  // Show the FAB when search loses focus
                }
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                bottomNavigationView.setVisibility(View.VISIBLE);  // Show the bottom navigation when search is closed
                floatingActionButton.setVisibility(View.VISIBLE);  // Show the FAB when search is closed
                return false;
            }
        });
    }
    private void performSearch(String query) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String formattedQuery = query.substring(0, 1).toUpperCase() + query.substring(1).toLowerCase();
        db.collection("Ebook")
                .whereEqualTo("title", formattedQuery)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Book> books = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            books.add(document.toObject(Book.class));
                        }
                        if (!books.isEmpty()) {
                            Log.d(TAG, "Books found for query: " + formattedQuery);
                            showSearchResultsDialog(books);
                        } else {
                            Log.d(TAG, "No books found for query: " + formattedQuery);
                            showNoResultsFound();
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                        showNoResultsFound();
                    }
                });
    }

    private Dialog searchDialog = null;

    private void updateSearchResultsDialog(List<Book> results) {
        if (searchDialog != null && searchDialog.isShowing()) {
            setupDialogWithResults(searchDialog, results);
        }
    }
    private void setupDialogWithResults(Dialog dialog, List<Book> results) {
        RecyclerView searchResultsRecyclerView = dialog.findViewById(R.id.searchResultsRecyclerView);
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        BooksAdapter searchResultsAdapter = new BooksAdapter(this, results, new BooksAdapter.OnBookClickListener() {
            @Override
            public void onBookClick(Book book) {
                // Handle book click, maybe close dialog or open book details
                dialog.dismiss();
            }
        });
        searchResultsRecyclerView.setAdapter(searchResultsAdapter);
    }

    private void showNoResultsFound() {
        Toast.makeText(this, "No results found", Toast.LENGTH_SHORT).show();
    }
    private void showSearchResultsDialog(List<Book> results) {
        Dialog searchDialog = new Dialog(this);
        searchDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        searchDialog.setContentView(R.layout.dialog_search_results);

        RecyclerView searchResultsRecyclerView = searchDialog.findViewById(R.id.searchResultsRecyclerView);
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        BooksAdapter searchResultsAdapter = new BooksAdapter(this, results, new BooksAdapter.OnBookClickListener() {
            @Override
            public void onBookClick(Book book) {
                BookDetailFragment fragment = BookDetailFragment.newInstance(
                        book.getTitle(),
                        book.getAuthor(),
                        book.getDescription(),
                        book.getPublicationDate().toDate().toString(),
                        book.getCoverImage(),
                        book.getLanguage(),
                        book.getPrice(),
                        book.getAccessType(),
                        book.getFileURL(),
                        book.getUploadDate().toDate().toString()
                );

                // Replace the current fragment with BookDetailFragment
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, fragment);
                transaction.addToBackStack(null); // Optional: Add fragment to back stack
                transaction.commit();
                searchDialog.dismiss();
            }
        });
        searchResultsRecyclerView.setAdapter(searchResultsAdapter);

        searchDialog.show();
        Window window = searchDialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.TOP | Gravity.START);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setWindowAnimations(R.style.DialogAnimation);
        }


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
    private void showLogoutConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.AlertDialogCustomStyle);
        builder.setMessage("Are you sure you want to logout?");
        builder.setPositiveButton("Yes", (dialog, which) -> logout());
        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container); // Change this to your actual fragment container ID
            if (currentFragment instanceof OnBackPressedListener) {
                ((OnBackPressedListener) currentFragment).onBackPressed();
            } else {
                super.onBackPressed();
            }
        }
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
                            Double dailyNumberBook= document.getDouble("dailyNumberBook");
                            Double weeklyNumberBook= document.getDouble("weeklyNumberBook");
                            Double monthlyNumberBook= document.getDouble("monthlyNumberBook");
                            Double yearlyNumberBook= document.getDouble("yearlyNumberBook");


                            if (dailyPrice != null && weeklyPrice != null && monthlyPrice != null && yearlyPrice != null) {
                                // Update subscription prices
                                updateSubscriptionPrices(dailyPrice, weeklyPrice, monthlyPrice, yearlyPrice,dailyNumberBook,weeklyNumberBook,monthlyNumberBook,yearlyNumberBook);
                                subscriptionManager.updateSubscriptionPrices( dailyNumberBook, weeklyNumberBook, monthlyNumberBook, yearlyNumberBook);
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
    private void updateSubscriptionPrices(double dailyPrice, double weeklyPrice, double monthlyPrice, double yearlyPrice, double dailyNumberBook, double weeklyNumberBook, double monthlyNumberBook, double yearlyNumberBook) {
        // Assuming dialog is properly initialized before calling this method
        if (dialog == null) {
            Log.e(TAG, "Dialog is null. Ensure it is properly initialized.");
            return;
        }

        RadioButton dailyRadioButton = dialog.findViewById(R.id.radioButton_daily);
        RadioButton weeklyRadioButton = dialog.findViewById(R.id.radioButton_weekly);
        RadioButton monthlyRadioButton = dialog.findViewById(R.id.radioButton_monthly);
        RadioButton yearlyRadioButton = dialog.findViewById(R.id.radioButton_yearly);

        if (dailyRadioButton != null && weeklyRadioButton != null && monthlyRadioButton != null && yearlyRadioButton != null) {
            dailyRadioButton.setText(String.format("Daily - %.2f ETB With %.0f Books", dailyPrice, dailyNumberBook));
            weeklyRadioButton.setText(String.format("Weekly - %.2f ETB With %.0f Books", weeklyPrice, weeklyNumberBook));
            monthlyRadioButton.setText(String.format("Monthly - %.2f ETB With %.0f Books", monthlyPrice, monthlyNumberBook));
            yearlyRadioButton.setText(String.format("Yearly - %.2f ETB With %.0f Books", yearlyPrice, yearlyNumberBook));
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

        EditText transactionIdEditText = dialog.findViewById(R.id.transactionIdEditText);
        transactionIdEditText.setVisibility(View.GONE);

        Button buyNowButton = dialog.findViewById(R.id.SubscribeButton);
        buyNowButton.setOnClickListener(v -> {
            if (transactionIdEditText.getVisibility() == View.GONE) {
                // Show the transaction ID input field if it's not visible
                transactionIdEditText.setVisibility(View.VISIBLE);
            } else {
                // Extract subscription price from the dialog
                double price = getSubscriptionPrice();

                // Retrieve the selected subscription type from the radio buttons
                String type = getSelectedSubscriptionType();

                // Retrieve the transaction ID from the input field
                String transactionId = transactionIdEditText.getText().toString();

                // Add the subscription to the database with the transaction ID
                addSubscriptionToDatabase(price, type, transactionId);
            }
        });

        fetchSubscriptionPrices(); // Fetch and update prices before showing the dialog

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void hideTransactionIdInputField() {
        EditText transactionIdEditText = dialog.findViewById(R.id.transactionIdEditText);
        transactionIdEditText.setVisibility(View.GONE);
    }

    private void resetRadioButtons() {
        RadioGroup radioGroup = dialog.findViewById(R.id.radioGroup);
        radioGroup.clearCheck(); // Clear selection
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
    private void addSubscriptionToDatabase(double price, String type, String transactionId) {
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
            subscriptionData.put("transactionId", transactionId); // Add transaction ID
            subscriptionData.put("approvalStatus", "pending"); // Set approval status to pending

            // Get reference to the Subscription collection
            CollectionReference subscriptionsRef = FirebaseFirestore.getInstance().collection("Subscription");

            // Add the subscription data to Firestore
            subscriptionsRef.add(subscriptionData)
                    .addOnSuccessListener(documentReference -> {
                        Log.d(TAG, "Subscription added with ID: " + documentReference.getId());
                        Toast.makeText(MainActivity.this, "Subscription added successfully. After approval, your book will be ready. Happy reading!", Toast.LENGTH_SHORT).show();
                        hideTransactionIdInputField();
                        resetRadioButtons();

                        // Update the subscription type and ID in SharedPreferences
                        SubscriptionManager subscriptionManager = new SubscriptionManager(MainActivity.this);
                        subscriptionManager.updateSubscriptionType(type, documentReference.getId());
                        Log.d(TAG, "Subscription type and ID updated in SharedPreferences: " + type + ", " + documentReference.getId());
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

    private void showInitials(String firstName, String lastName, TextView textViewInitial, ImageView imageViewProfilePhoto) {
        if (firstName != null && !firstName.isEmpty()) {
       if (textViewInitial != null)    textViewInitial.setText(String.valueOf(firstName.charAt(0)).toUpperCase());
        } else if (lastName != null && !lastName.isEmpty()) {
            textViewInitial.setText(String.valueOf(lastName.charAt(0)).toUpperCase());
        } else {
            textViewInitial.setText("");
        }
        if(textViewInitial !=null)   textViewInitial.setVisibility(View.VISIBLE);
        if(imageViewProfilePhoto !=null)     imageViewProfilePhoto.setVisibility(View.GONE);
    }

}
