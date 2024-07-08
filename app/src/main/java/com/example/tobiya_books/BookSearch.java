package com.example.tobiya_books;

import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class BookSearch {
    private static final String TAG = "BookSearch";

    public interface SearchListener {
        void onResults(List<Book> results);
    }

    private List<Book> books;
    private FirebaseFirestore db;

    public BookSearch() {
        this.books = new ArrayList<>();
        this.db = FirebaseFirestore.getInstance();
        fetchBooksFromFirestore(null); // Fetch books on initialization
    }

    public void search(String query, SearchListener listener) {
        fetchBooksByTitle(query, listener);
    }

    public void updateBooks(List<Book> books) {
        this.books = books;
    }

    public void fetchBooksFromFirestore(SearchListener listener) {
        CollectionReference ebooksRef = db.collection("Ebook");

        ebooksRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Book> fetchedBooks = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Book book = document.toObject(Book.class);
                    fetchedBooks.add(book);
                }
                updateBooks(fetchedBooks);
                if (listener != null) {
                    listener.onResults(fetchedBooks); // Notify the listener with fetched books if provided
                }
            } else {
                Log.e(TAG, "Error fetching books from Firestore: ", task.getException());
            }
        });
    }

    private void fetchBooksByTitle(String title, SearchListener listener) {
        CollectionReference ebooksRef = db.collection("Ebook");

        ebooksRef.whereEqualTo("title", title).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Book> fetchedBooks = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Book book = document.toObject(Book.class);
                    fetchedBooks.add(book);
                }
                listener.onResults(fetchedBooks);

            } else {
                Log.e(TAG, "Error fetching books by title from Firestore: ", task.getException());
            }
        });
    }
}
