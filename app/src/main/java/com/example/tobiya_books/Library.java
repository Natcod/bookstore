package com.example.tobiya_books;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import timber.log.Timber;

public class Library extends Fragment implements PurchaseAdapter.OnRemoveClickListener {

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private List<Book> books;
    private PurchaseAdapter adapter;
    private String currentUserId;
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton floatingActionButton;
    private BottomAppBar bottomAppBar;
    private Toolbar toolbar;

    public Library() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        db = FirebaseFirestore.getInstance();
        books = new ArrayList<>();
        currentUserId = getCurrentUserId(); // Retrieve current user ID
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // Hide the search menu item
        MenuItem searchItem = menu.findItem(R.id.action_search);
        if (searchItem != null) {
            searchItem.setVisible(false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_books);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        adapter = new PurchaseAdapter(getActivity(), books, this); // Pass 'this' as the removeClickListener
        recyclerView.setAdapter(adapter);

        if (currentUserId != null) {
            fetchDataAndDisplay(view);
        } else {
            // Handle the case where currentUserId is null (e.g., show an error message)
            Log.e("LibraryFragment", "Current user ID is null");
            // You can show an error message or redirect the user to login again
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        checkAndSetNavigationVisibility();
    }

    private void checkAndSetNavigationVisibility() {
        bottomNavigationView = getActivity().findViewById(R.id.bottom_navigation);
        floatingActionButton = getActivity().findViewById(R.id.fab);
        bottomAppBar = getActivity().findViewById(R.id.bottomAppBar);
        toolbar = getActivity().findViewById(R.id.toolbar);

        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(View.VISIBLE);
        }

        if (floatingActionButton != null) {
            floatingActionButton.setVisibility(View.VISIBLE);
        }

        if (bottomAppBar != null) {
            bottomAppBar.setVisibility(View.VISIBLE);
        }

        if (toolbar != null) {
            toolbar.setVisibility(View.VISIBLE);
        }
    }

    private String getCurrentUserId() {
        // Retrieve current user ID from SharedPreferences
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getString("UserID", null);
    }
    private void fetchDataAndDisplay(View view) {
        // Clear the books list to prevent duplication
        books.clear();

        db.collection("Purchase")
                .whereEqualTo("reader", db.document("Reader/" + currentUserId))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            DocumentReference ebookRef = document.getDocumentReference("ebook");
                            String approvalStatus = document.getString("approvalStatus");
                            String accessType = document.getString("accessType");
                            String documentId = document.getId();

                            if ("Free".equals(accessType)) {
                                // Fetch book details directly if accessType is Free
                                fetchBookDetails(ebookRef, documentId, view);
                            } else if ("approved".equals(approvalStatus)) {
                                if ("Paid".equals(accessType)) {
                                    // Fetch book details directly if accessType is Paid
                                    fetchBookDetails(ebookRef, documentId, view);
                                } else if ("Subscription".equals(accessType)) {
                                    // Check subscription status if accessType is subscription
                                    checkSubscriptionAndFetchBook(ebookRef, documentId, view);
                                } else {
                                    // Display a toast message for other cases
                                    Toast.makeText(view.getContext(), "There is no approved Payment based Book", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                // Display a toast message for non-approved books
                                    Toast.makeText(view.getContext(), "There is no approved book", Toast.LENGTH_SHORT).show();
                            }
                        }
                        // Check if books list is empty
                        if (books.isEmpty()) {
                            showEmptyLibraryMessage(view);
                        }
                    } else {
                        Log.w("LibraryFragment", "Error getting documents: ", task.getException());
                    }
                });
    }
    private void checkSubscriptionAndFetchBook(DocumentReference ebookRef, String documentId, View view) {
        db.collection("Purchase").document(documentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String approvalStatus = documentSnapshot.getString("approvalStatus");
                    Timestamp endDate = documentSnapshot.getTimestamp("endDate");

                    Log.d("LibraryFragment", "Approval status: " + approvalStatus);
                    if ("approved".equals(approvalStatus)) {
                        if (endDate != null) {
                            Date endDateDate = endDate.toDate();
                            Date currentDate = new Date();
                            Log.d("LibraryFragment", "End date: " + endDateDate);
                            Log.d("LibraryFragment", "Current date: " + currentDate);

                            if (endDateDate.before(currentDate)) {
                                // Subscription has expired
                                documentSnapshot.getReference().update("approvalStatus", "expired")
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d("LibraryFragment", "Purchase approval status updated to 'expired' successfully.");
                                            Toast.makeText(view.getContext(), "Subscription has expired", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("LibraryFragment", "Failed to update purchase approval status to 'expired'.", e);
                                            Toast.makeText(view.getContext(), "Failed to update subscription status", Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                // Subscription is still valid, fetch book details
                                fetchBookDetails(ebookRef, documentId, view);
                            }
                        } else {
                            Log.e("LibraryFragment", "End date is null.");
                            Toast.makeText(view.getContext(), "Failed to retrieve subscription end date.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(view.getContext(), "There is no approved Subscription-based book", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("LibraryFragment", "Failed to fetch purchase document: " + documentId, e);
                    Toast.makeText(view.getContext(), "Failed to fetch purchase details", Toast.LENGTH_SHORT).show();
                });
    }
    private void showEmptyLibraryMessage(View view) {
        // Assuming you have a TextView in your fragment_library.xml with id empty_library_message
        TextView emptyLibraryMessage = view.findViewById(R.id.empty_library_message);
        if (emptyLibraryMessage != null) {
            emptyLibraryMessage.setVisibility(View.VISIBLE);
        }
    }
    private void fetchBookDetails(DocumentReference ebookRef, String documentId, View view) {
        ebookRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        Book book = document.toObject(Book.class);
                        if (book != null) {
                            book.setDocumentReferencePath(documentId); // Set the document ID to the Book object
                            books.add(book);
                            Timber.tag("LibraryFragment").d("Book fetched: " + book.getTitle());
                            adapter.notifyDataSetChanged();
                        }
                    } else {
                        Log.w("LibraryFragment", "No such document");
                    }
                } else {
                    Log.w("LibraryFragment", "Error getting book details: ", task.getException());
                }
            }
        });
    }
    // Method to remove a book from the database
    private void removeBookFromDatabase(String documentId) {
        db.collection("Purchase").document(documentId)
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("LibraryFragment", "Book deleted successfully");
                            int position = findBookPositionById(documentId);
                            if (position != -1) {
                                books.remove(position);
                                adapter.notifyItemRemoved(position);
                            }
                        } else {
                            Log.w("LibraryFragment", "Error deleting book", task.getException());
                        }
                    }
                });
    }
    // Method to find the position of the book in the list based on its document ID
    private int findBookPositionById(String documentId) {
        for (int i = 0; i < books.size(); i++) {
            Book book = books.get(i);
            if (book.getDocumentReferencePath().equals(documentId)) {
                return i;
            }
        }
        return -1; // Book not found
    }
    // Method to handle remove button click in the adapter
    @Override
    public void onRemoveClick(int position) {
        // Get the document ID of the book to remove
        String documentId = books.get(position).getDocumentReferencePath();
        // Call the method to remove the book from the database
        removeBookFromDatabase(documentId);
    }
}
