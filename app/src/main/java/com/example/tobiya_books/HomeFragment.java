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

    private RecyclerView recyclerView;
    private BooksAdapter adapter;
    private List<Book> bookList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize book list
        bookList = new ArrayList<>();
        bookList.add(new Book("Yoratorad", "Yismake worku", "Description 1", "2022", "yoratorad"));
        bookList.add(new Book("lelasew", "Author 1", "Description 1", "2022", "lelasew"));
        bookList.add(new Book("yehabeshajebdu", "adolph", "Description 1", "2022", "yehabeshajebdu"));
        bookList.add(new Book("fikireskemekabir", "Author 1", "Description 1", "2022", "fikireskemekabir"));
        bookList.add(new Book("alemawek", "Author 1", "Description 1", "2022", "alemawek"));
        bookList.add(new Book("alemenor", "Author 1", "Description 1", "2022", "alemenor"));
        bookList.add(new Book("Title 1", "Author 1", "Description 1", "2022", "yoratorad"));
        bookList.add(new Book("Yoratorad", "Yismake worku", "Description 1", "2022", "yoratorad"));
        bookList.add(new Book("lelasew", "Author 1", "Description 1", "2022", "lelasew"));
        bookList.add(new Book("yehabeshajebdu", "adolph", "Description 1", "2022", "yehabeshajebdu"));
        bookList.add(new Book("fikireskemekabir", "Author 1", "Description 1", "2022", "fikireskemekabir"));
        bookList.add(new Book("alemawek", "Daniel Wondimagegn", "Description 1", "2022", "alemawek"));
        bookList.add(new Book("alemenor", "Daniel Wondimagegn", "Description 1", "2022", "alemenor"));
        bookList.add(new Book("yoratorad", "Yismake", "Description 1", "2022", "yoratorad"));




        // Add more books as needed

        // Set up adapter
        adapter = new BooksAdapter(getContext(), bookList, this);
        recyclerView.setAdapter(adapter);

        return view;
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
