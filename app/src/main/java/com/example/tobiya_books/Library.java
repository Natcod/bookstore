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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class Library extends Fragment {

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private List<Book> books;
    private PurchaseAdapter adapter;

    public Library() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        books = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_books);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        adapter = new PurchaseAdapter(getActivity(), books);
        recyclerView.setAdapter(adapter);

        fetchDataAndDisplay();

        return view;
    }

    private void fetchDataAndDisplay() {
        db.collection("Purchase")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                DocumentReference ebookRef = document.getDocumentReference("ebook");
                                if (ebookRef != null) {
                                    fetchBookDetails(ebookRef);
                                }
                            }
                        } else {
                            Log.w("LibraryFragment", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void fetchBookDetails(DocumentReference ebookRef) {
        ebookRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        Book book = document.toObject(Book.class);
                        if (book != null) {
                            books.add(book);
                            Log.d("LibraryFragment", "Book fetched: " + book.getTitle());
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
}
