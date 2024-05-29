package com.example.tobiya_books;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

public class Store extends Fragment {

    private RecyclerView recyclerViewBag;
    private BooksAdapter adapter;
    private List<Book> allBooks;
    private List<Book> currentBooks;
    private Button buttonAll;
    private Button buttonFree;
    private Button buttonPaid;
    private Button buttonSubscription;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_store, container, false);

        recyclerViewBag = view.findViewById(R.id.recycler_view_bag);
        recyclerViewBag.setLayoutManager(new GridLayoutManager(getContext(), 3));

        buttonAll = view.findViewById(R.id.button_all);
        buttonFree = view.findViewById(R.id.button_free);
        buttonPaid = view.findViewById(R.id.button_paid);
        buttonSubscription = view.findViewById(R.id.button_subscription);


        currentBooks = new ArrayList<>(allBooks);
        adapter = new BooksAdapter(getContext(), currentBooks, null);
        recyclerViewBag.setAdapter(adapter);

        buttonAll.setOnClickListener(v -> filterBooks("All"));
        buttonFree.setOnClickListener(v -> filterBooks("free"));
        buttonPaid.setOnClickListener(v -> filterBooks("paid"));
        buttonSubscription.setOnClickListener(v -> filterBooks("subscribed"));

        return view;
    }

    private void filterBooks(String accessType) {
        currentBooks.clear();
        if ("All".equals(accessType)) {
            currentBooks.addAll(allBooks);
        } else {
            for (Book book : allBooks) {
                if (accessType.equalsIgnoreCase(book.getAccessType())) {
                    currentBooks.add(book);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }


}