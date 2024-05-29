package com.example.tobiya_books;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements BooksAdapter.OnBookClickListener {

    private static final String TAG = "HomeFragment";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private RecyclerView recyclerViewFreeBooks;
    private RecyclerView recyclerViewPaidBooks;
    private BooksAdapter freeBooksAdapter;
    private BooksAdapter paidBooksAdapter;
    private List<Book> freeBooksList = new ArrayList<>();
    private List<Book> paidBooksList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerViewFreeBooks = view.findViewById(R.id.recycler_view_free_books);
        recyclerViewPaidBooks = view.findViewById(R.id.recycler_view_paid_books);

        // Set layout manager for both RecyclerViews
        recyclerViewFreeBooks.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerViewPaidBooks.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        // Initialize adapters
        freeBooksAdapter = new BooksAdapter(getContext(), freeBooksList, this);
        paidBooksAdapter = new BooksAdapter(getContext(), paidBooksList, this);

        // Set adapters to RecyclerViews
        recyclerViewFreeBooks.setAdapter(freeBooksAdapter);
        recyclerViewPaidBooks.setAdapter(paidBooksAdapter);

        // Fetch books data
        fetchBooksData();

        return view;
    }

    private void fetchBooksData() {
        // Fetch Free Books
        db.collection("Ebook").whereEqualTo("accessType", "Free")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    freeBooksList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Book book = document.toObject(Book.class);
                        if (book != null) {
                            freeBooksList.add(book);
                        }
                    }
                    freeBooksAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching free books", e));

        // Fetch Paid Books
        db.collection("Ebook").whereEqualTo("accessType", "Paid")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    paidBooksList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Book book = document.toObject(Book.class);
                        if (book != null) {
                            paidBooksList.add(book);
                        }
                    }
                    paidBooksAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching paid books", e));
    }

    @Override
    public void onBookClick(Book book) {
        // Handle book click
        Bundle bundle = new Bundle();
        bundle.putString("title", book.getTitle());
        bundle.putString("author", book.getAuthor());
        bundle.putString("description", book.getDescription());

        // Convert Timestamp to String for publicationDate
        String publicationDate = book.getPublicationDate().toDate().toString();
        bundle.putString("publicationDate", publicationDate);

        // Check if getCoverImageName() exists or replace it with an appropriate method
        bundle.putString("coverImageUrl", book.getCoverImage());

        // Assuming price is a double, convert it to String
        bundle.putString("price", String.valueOf(book.getPrice()));

        bundle.putString("language", book.getLanguage());
        bundle.putString("accessType", book.getAccessType());

        BookDetailFragment bookDetailFragment = new BookDetailFragment();
        bookDetailFragment.setArguments(bundle);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, bookDetailFragment)
                .addToBackStack(null)
                .commit();
    }
}
