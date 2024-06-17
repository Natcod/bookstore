package com.example.tobiya_books;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
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
    private boolean initialLoad = true;

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

        // Setup LinearLayoutManager with reverse layout
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new PurchaseAdapter(getActivity(), books, this); // Pass 'this' as the removeClickListener
        recyclerView.setAdapter(adapter);

        // Fetch data and display only if the user is authenticated and their ID is obtained
        fetchAndDisplayBooks();

        return view;
    }

    private void fetchAndDisplayBooks() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String currentUserId = sharedPreferences.getString("UserID", null);

        if (currentUserId != null) {
            fetchBooksForUser(currentUserId);
        } else {
            Timber.e("Current user ID is null");
        }
    }

    private void fetchBooksForUser(String userId) {
        // Clear the books list to prevent duplication
        books.clear();

        db.collection("Purchase")
                .whereEqualTo("reader", db.document("Reader/" + userId)) // Filter by reader field
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                DocumentReference ebookRef = (DocumentReference) document.get("ebook");
                                if (ebookRef != null) {
                                    tasks.add(ebookRef.get());
                                }
                            }

                            Task<List<DocumentSnapshot>> allTasks = Tasks.whenAllSuccess(tasks);
                            allTasks.addOnCompleteListener(new OnCompleteListener<List<DocumentSnapshot>>() {
                                @Override
                                public void onComplete(@NonNull Task<List<DocumentSnapshot>> task) {
                                    if (task.isSuccessful()) {
                                        for (DocumentSnapshot document : task.getResult()) {
                                            Book book = document.toObject(Book.class);
                                            if (book != null) {
                                                book.setDocumentReferencePath(document.getId());
                                                books.add(book);
                                                Timber.d("Book fetched: %s", book.getTitle());
                                            }
                                        }
                                        adapter.notifyDataSetChanged();
                                        if (initialLoad) {
                                            recyclerView.post(() -> recyclerView.scrollToPosition(books.size() - 1));
                                            initialLoad = false;
                                        }
                                    } else {
                                        Timber.e("Error getting book details: %s", task.getException());
                                    }
                                }
                            });

                        } else {
                            Timber.e("Error getting documents: %s", task.getException());
                        }
                    }
                });
    }

    private void removeBookFromDatabase(String documentId) {
        db.collection("Purchase").document(documentId)
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Timber.d("Book deleted successfully");
                            int position = findBookPositionById(documentId);
                            if (position != -1) {
                                books.remove(position);
                                adapter.notifyItemRemoved(position);
                                recyclerView.post(() -> recyclerView.scrollToPosition(books.size() - 1));
                            }
                        } else {
                            Timber.e("Error deleting book: %s", task.getException());
                            Toast.makeText(getActivity(), "Error deleting book", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private int findBookPositionById(String documentId) {
        for (int i = 0; i < books.size(); i++) {
            Book book = books.get(i);
            if (book.getDocumentReferencePath().equals(documentId)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onRemoveClick(int position) {
        String documentId = books.get(position).getDocumentReferencePath();
        removeBookFromDatabase(documentId);
    }
}
