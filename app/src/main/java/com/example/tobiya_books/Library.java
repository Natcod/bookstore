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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class Library extends Fragment implements PurchaseAdapter.OnRemoveClickListener {

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private List<Book> books;
    private PurchaseAdapter adapter;
    private String currentUserId;

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
            fetchDataAndDisplay();
        } else {
            // Handle the case where currentUserId is null (e.g., show an error message)
            Log.e("LibraryFragment", "Current user ID is null");
            // You can show an error message or redirect the user to login again
        }

        return view;
    }

    private String getCurrentUserId() {
        // Retrieve current user ID from SharedPreferences
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getString("UserID", null);
    }

    private void fetchDataAndDisplay() {
        // Clear the books list to prevent duplication
        books.clear();

        db.collection("Purchase")
                .whereEqualTo("reader", db.document("Reader/" + currentUserId))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                DocumentReference ebookRef = document.getDocumentReference("ebook");
                                if (ebookRef != null) {
                                    String documentId = document.getId();
                                    fetchBookDetails(ebookRef, documentId);
                                }
                            }
                            // Check if books list is empty
                            if (books.isEmpty()) {
                                showEmptyLibraryMessage();
                            }
                        } else {
                            Log.w("LibraryFragment", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void showEmptyLibraryMessage() {
        // Assuming you have a TextView in your fragment_library.xml with id empty_library_message
        TextView emptyLibraryMessage = getView().findViewById(R.id.empty_library_message);
       if(emptyLibraryMessage != null) emptyLibraryMessage.setVisibility(View.VISIBLE);
    }

    private void fetchBookDetails(DocumentReference ebookRef, String documentId) {
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
