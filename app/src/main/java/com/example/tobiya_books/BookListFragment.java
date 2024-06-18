package com.example.tobiya_books;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.BuildConfig;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class BookListFragment extends Fragment implements BooksAdapter.OnBookClickListener {

    private static final String ARG_ACCESS_TYPE = "access_type";
    private String accessType;
    private RecyclerView recyclerView;
    private BooksAdapter adapter;
    private List<Book> books;

    public BookListFragment() {
        // Required empty public constructor
    }

    public static BookListFragment newInstance(String accessType) {
        BookListFragment fragment = new BookListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ACCESS_TYPE, accessType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            accessType = getArguments().getString(ARG_ACCESS_TYPE);
        }

        // Initialize Timber
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book_list, container, false);
        recyclerView = view.findViewById(R.id.recycler_view_books);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        books = new ArrayList<>();
        adapter = new BooksAdapter(getContext(), books, this); // Pass 'this' as the click listener
        recyclerView.setAdapter(adapter);

        fetchBooks();

        return view;
    }

    private void fetchBooks() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query collectionRef = db.collection("Ebook");

        // Check if accessType is "All"
        if ("All".equals(accessType)) {
            collectionRef .whereEqualTo("approvalStatus", "Approved").get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    books.clear();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Book book = document.toObject(Book.class);
                        books.add(book);
                        Timber.tag("BookListFragment").d("Book fetched: %s", book.getTitle());
                    }
                    adapter.notifyDataSetChanged();
                    Timber.tag("BookListFragment").d("Books list updated, size: %s", books.size());
                } else {
                    Timber.tag("BookListFragment").e(task.getException(), "Error loading books: ");
                }
            });
        } else {
            // Fetch books based on access type
            collectionRef.whereEqualTo("accessType", accessType) .whereEqualTo("approvalStatus", "Approved")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            books.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Book book = document.toObject(Book.class);
                                books.add(book);
                                Timber.tag("BookListFragment").d("Book fetched: %s", book.getTitle());
                            }
                            adapter.notifyDataSetChanged();
                            Timber.tag("BookListFragment").d("Books list updated, size: %s", books.size());
                        } else {
                            Timber.tag("BookListFragment").e(task.getException(), "Error loading books: ");
                        }
                    });
        }
    }

    // Implement the onBookClick method
    // Implement the onBookClick method
    @Override
    public void onBookClick(Book book) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query query = db.collection("Ebook").whereEqualTo("title", book.getTitle());

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult() != null) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String documentId = document.getId(); // Get the document ID
                        Log.d("BookListFragment", "Document ID: " + documentId); // Log the document ID

                        // Pass the document ID and other attributes to the next fragment
                        Bundle bundle = new Bundle();
                        bundle.putString("documentId", documentId);  // Pass the document ID
                        bundle.putString("title", book.getTitle());
                        bundle.putString("author", book.getAuthor());
                        bundle.putString("description", book.getDescription());
                        bundle.putString("publicationDate", book.getPublicationDate().toDate().toString());
                        bundle.putString("coverImageUrl", book.getCoverImage());
                        bundle.putString("language", book.getLanguage());
                        bundle.putString("price", String.valueOf(book.getPrice()));
                        bundle.putString("accessType", book.getAccessType());
                        bundle.putString("approvalStatus", book.getApprovalStatus());
                        bundle.putString("genre", book.getGenre());
                        bundle.putString("fileURL", book.getFileURL());
                        bundle.putString("publisher", book.getPublisher().getPath()); // Assuming getPublisher returns DocumentReference
                        bundle.putString("uploadDate", book.getUploadDate().toDate().toString());

                        BookDetailFragment bookDetailFragment = new BookDetailFragment();
                        bookDetailFragment.setArguments(bundle);

                        getParentFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, bookDetailFragment)
                                .addToBackStack(null)
                                .commit();
                    }
                } else {
                    Log.d("BookListFragment", "Query result is null");
                }
            } else {
                Log.d(TAG, "Error getting documents: ", task.getException());
            }
        });
    }





}
