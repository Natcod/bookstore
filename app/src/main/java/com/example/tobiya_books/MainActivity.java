package com.example.tobiya_books;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, BooksAdapter.OnBookClickListener, BookDetailFragment.MainActivityListener {

    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNavigationView;
    private FragmentManager fragmentManager;
    private Toolbar toolbar;
    private FloatingActionButton fab;
    private SearchView searchView;
    private RecyclerView recyclerView;
    private BooksAdapter booksAdapter;
    private static final String TAG = "MainActivity";

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

        bottomNavigationView = findViewById(R.id.bottom_navigation);
       bottomNavigationView.setBackground(null);

        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
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
            }
        });

        fragmentManager = getSupportFragmentManager();
        openFragment(new HomeFragment());

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomDialog();
            }
        });

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        booksAdapter = new BooksAdapter(this, new ArrayList<>(), this);

        recyclerView.setAdapter(booksAdapter);
    }

    private void openFragment(Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    @Override
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
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
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

    private Dialog searchDialog = null; // Make it a field to check its status



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
        searchDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        searchDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        searchDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        searchDialog.getWindow().setGravity(Gravity.CENTER);
    }





    public void showBottomDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottomsheetlayout);

        ImageView cancelButton = dialog.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        Button buyNowButton = dialog.findViewById(R.id.buyButton);
        buyNowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPurchaseToDatabase();
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
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
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                redirectToMainActivity2();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void redirectToMainActivity2() {
        startActivity(new Intent(MainActivity.this, MainActivity2.class));
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

}

