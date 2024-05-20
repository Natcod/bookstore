package com.example.tobiya_books;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements BooksAdapter.OnBookClickListener {

    private RecyclerView recyclerViewNewArrivals;
    private RecyclerView recyclerViewBestSellers;
    private RecyclerView recyclerViewFreeBooks;
    private BooksAdapter adapter;
    private List<Book> newArrivalsList;
    private List<Book> bestSellersList;
    private List<Book> freeBooksList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize RecyclerViews
        recyclerViewNewArrivals = view.findViewById(R.id.recycler_view_new_arrivals);
        recyclerViewBestSellers = view.findViewById(R.id.recycler_view_best_sellers);
        recyclerViewFreeBooks = view.findViewById(R.id.recycler_view_free_books);

        // Initialize book lists
        newArrivalsList = new ArrayList<>();
        bestSellersList = new ArrayList<>();
        freeBooksList = new ArrayList<>();

        // Sample data for demonstration
// Sample data for demonstration
        newArrivalsList.add(new Book("Yoratorad", "Yismake Worku", "Description 1", "2022", "yoratorad"));
        newArrivalsList.add(new Book("Lelasew", "Author 1", "Description 1", "2022", "lelasew"));
        newArrivalsList.add(new Book("Yehabeshajebdu", "Adolph", "Description 1", "2022", "yehabeshajebdu"));
        newArrivalsList.add(new Book("Fikireskemekabir", "Author 1", "Description 1", "2022", "fikireskemekabir"));
        newArrivalsList.add(new Book("Alemawek", "Author 1", "Description 1", "2022", "alemawek"));
        newArrivalsList.add(new Book("Alemenor", "Author 1", "Description 1", "2022", "alemenor"));
// Repeat the same data for Best Sellers and Free Books sections
        bestSellersList.addAll(newArrivalsList);
        freeBooksList.addAll(newArrivalsList);


        // Set up adapters and layout managers for each RecyclerView
        setupRecyclerView(recyclerViewNewArrivals, newArrivalsList);
        setupRecyclerView(recyclerViewBestSellers, bestSellersList);
        setupRecyclerView(recyclerViewFreeBooks, freeBooksList);

        return view;
    }

    private void setupRecyclerView(RecyclerView recyclerView, List<Book> bookList) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        adapter = new BooksAdapter(getContext(), bookList, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onBookClick(Book book) {
        // Handle book click
        Bundle bundle = new Bundle();
        bundle.putString("title", book.getTitle());
        bundle.putString("author", book.getAuthor());
        bundle.putString("description", book.getDescription());
        bundle.putString("publicationDate", book.getPublicationDate());
        bundle.putString("coverImageUrl", book.getCoverImageName());

        BookDetailFragment bookDetailFragment = new BookDetailFragment();
        bookDetailFragment.setArguments(bundle);

        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, bookDetailFragment)
                .addToBackStack(null)
                .commit();
    }
}
