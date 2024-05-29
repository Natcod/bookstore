package com.example.tobiya_books;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class BookListFragment extends Fragment {

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
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book_list, container, false);
        recyclerView = view.findViewById(R.id.recycler_view_books);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        books = new ArrayList<>();
        adapter = new BooksAdapter(getContext(), books, null);
        recyclerView.setAdapter(adapter);

        fetchBooks();

        return view;
    }

    private void fetchBooks() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query collectionRef = db.collection("Ebook");

        // Check if accessType is "All"
        if ("All".equals(accessType)) {
            collectionRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    books.clear();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Book book = document.toObject(Book.class);
                        books.add(book);
                        Log.d("BookListFragment", "Book fetched: " + book.getTitle());
                    }
                    adapter.notifyDataSetChanged();
                    Log.d("BookListFragment", "Books list updated, size: " + books.size());
                } else {
                    Log.e("BookListFragment", "Error loading books: ", task.getException());
                }
            });
        } else {
            // Fetch books based on access type
            collectionRef.whereEqualTo("accessType", accessType)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            books.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Book book = document.toObject(Book.class);
                                books.add(book);
                                Log.d("BookListFragment", "Book fetched: " + book.getTitle());
                            }
                            adapter.notifyDataSetChanged();
                            Log.d("BookListFragment", "Books list updated, size: " + books.size());
                        } else {
                            Log.e("BookListFragment", "Error loading books: ", task.getException());
                        }
                    });
        }
    }


}
