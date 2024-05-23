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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book_list, container, false);
        recyclerView = view.findViewById(R.id.recycler_view_books);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Sample data for demonstration
        books = getBooksByAccessType();

        adapter = new BooksAdapter(getContext(), books, null);
        recyclerView.setAdapter(adapter);

        return view;
    }

    private List<Book> getBooksByAccessType() {
        List<Book> allBooks = new ArrayList<>();
        // Add your sample data here
        allBooks.add(new Book("Yoratorad", "Yismake Worku", "Description 1", "2022", "yoratorad", "Amharic", "300", "paid"));
        allBooks.add(new Book("Lelasew", "Author 1", "Description 1", "2022", "lelasew", "Amharic", "200", "paid"));
        allBooks.add(new Book("Yehabeshajebdu", "Adolph", "Description 1", "2022", "yehabeshajebdu", "Amharic", "250", "Subscribed"));
        allBooks.add(new Book("Fikireskemekabir", "Author 1", "Description 1", "2022", "fikireskemekabir", "Amharic", "400", "paid"));
        allBooks.add(new Book("Alemawek", "Author 1", "Description 1", "2022", "alemawek", "Amharic", "300", "free"));
        allBooks.add(new Book("Alemenor", "Author 1", "Description 1", "2022", "alemenor", "Amharic", "600", "paid"));

        if ("All".equals(accessType)) {
            return allBooks;
        }

        List<Book> filteredBooks = new ArrayList<>();
        for (Book book : allBooks) {
            if (accessType.equals(book.getAccessType())) {
                filteredBooks.add(book);
            }
        }
        return filteredBooks;
    }
}
