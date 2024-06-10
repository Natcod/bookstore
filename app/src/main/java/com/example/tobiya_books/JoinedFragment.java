package com.example.tobiya_books;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class JoinedFragment extends Fragment implements GroupAdapter.OnGroupClickListener {

    private static final String TAG = "JoinedFragment";

    private RecyclerView recyclerView;
    private GroupAdapter groupAdapter;
    private List<Group> groupList = new ArrayList<>();
    private FirebaseFirestore db;

    public JoinedFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_joined, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_joined_groups);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        groupAdapter = new GroupAdapter(groupList, this); // Pass 'this' as the listener
        recyclerView.setAdapter(groupAdapter);

        db = FirebaseFirestore.getInstance();

        fetchJoinedGroups();

        return view;
    }

    private void fetchJoinedGroups() {
        // Retrieve user ID from shared preferences
        Context context = getActivity();
        if (context != null) {
            String userId = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE).getString("UserID", "defaultUserId");
            Log.d(TAG, "Fetching joined groups for user ID: " + userId);
            db.collection("BookClubMember")
                    .whereEqualTo("reader", db.collection("Reader").document(userId))
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            groupList.clear();
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null) {
                                Log.d(TAG, "Query snapshot size: " + querySnapshot.size());
                                List<Group> tempGroupList = new ArrayList<>();
                                for (QueryDocumentSnapshot document : querySnapshot) {
                                    DocumentReference bookClubRef = document.getDocumentReference("bookClub");
                                    if (bookClubRef != null) {
                                        Log.d(TAG, "Fetching book club document: " + bookClubRef.getId());
                                        bookClubRef.get().addOnSuccessListener(bookClubDoc -> {
                                            if (bookClubDoc.exists()) {
                                                Group group = bookClubDoc.toObject(Group.class);
                                                group.setId(bookClubDoc.getId());
                                                tempGroupList.add(group);
                                                Log.d(TAG, "Group added: " + group.getName());
                                                // Sort the groups based on joinDate (descending)
                                                tempGroupList.sort((g1, g2) -> {
                                                    Timestamp t1 = document.getTimestamp("joinDate");
                                                    Timestamp t2 = document.getTimestamp("joinDate");
                                                    if (t1 != null && t2 != null) {
                                                        return t2.compareTo(t1);
                                                    }
                                                    return 0;
                                                });
                                                groupList.clear();
                                                groupList.addAll(tempGroupList);
                                                groupAdapter.notifyDataSetChanged();
                                            } else {
                                                Log.d(TAG, "Book club document does not exist");
                                            }
                                        }).addOnFailureListener(e -> {
                                            Log.e(TAG, "Error fetching book club: ", e);
                                            Toast.makeText(getContext(), "Error fetching book club: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                                    }
                                }
                            } else {
                                Log.d(TAG, "Query snapshot is null");
                            }
                        } else {
                            Log.e(TAG, "Error getting joined groups: ", task.getException());
                            Toast.makeText(getContext(), "Error getting joined groups: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Log.d(TAG, "Context is null");
        }
    }


    private void createNewGroup(String groupName) {
        List<String> members = new ArrayList<>(); // Initialize with an empty list or default members
        Group newGroup = new Group(groupName, members);

        db.collection("BookClub").add(newGroup)
                .addOnSuccessListener(documentReference -> {
                    newGroup.setId(documentReference.getId());
                    groupList.add(newGroup);
                    groupAdapter.notifyDataSetChanged();
                    Toast.makeText(getContext(), "Group created", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error creating group: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onGroupClick(int position) {
        Group selectedGroup = groupList.get(position);
        String groupId = selectedGroup.getId(); // Get the group ID

        // Obtain the user ID from shared preferences
        String userId = getActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE).getString("UserID", "defaultUserId");

        // Pass both groupId and userId to openMessagesFragment
        openMessagesFragment(groupId, userId);
    }

    private void openMessagesFragment(String groupId, String userId) {
        MessagesFragment messagesFragment = MessagesFragment.newInstance(groupId, userId, false); // Pass false for opened from JoinedFragment
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, messagesFragment)
                .addToBackStack(null)
                .commit();
    }

}
