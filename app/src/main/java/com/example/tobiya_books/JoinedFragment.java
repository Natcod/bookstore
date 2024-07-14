package com.example.tobiya_books;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class JoinedFragment extends Fragment implements GroupAdapter.OnGroupClickListener {

    private static final String TAG = "JoinedFragment";

    private RecyclerView recyclerView;
    private GroupAdapter groupAdapter;
    private List<Group> groupList = new ArrayList<>();
    private FirebaseFirestore db;
    private TextView tvNoJoinedGroups;

    private ListenerRegistration joinedGroupsListenerRegistration;

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

        tvNoJoinedGroups = view.findViewById(R.id.tv_no_joined_groups);

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
            joinedGroupsListenerRegistration = db.collection("BookClubMember")
                    .whereEqualTo("reader", db.collection("Reader").document(userId))
                    .addSnapshotListener((querySnapshot, e) -> {
                        if (e != null) {
                            Log.e(TAG, "Error listening for changes: ", e);
                            Toast.makeText(getContext(), "Error listening for changes: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            tvNoJoinedGroups.setVisibility(View.VISIBLE);
                            tvNoJoinedGroups.setText("Error listening for changes: " + e.getMessage());
                            return;
                        }

                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            for (DocumentChange documentChange : querySnapshot.getDocumentChanges()) {
                                QueryDocumentSnapshot document = documentChange.getDocument();
                                DocumentReference bookClubRef = document.getDocumentReference("bookClub");
                                if (bookClubRef != null) {
                                    switch (documentChange.getType()) {
                                        case ADDED:
                                            handleDocumentAdded(bookClubRef, document);
                                            break;
                                        case MODIFIED:
                                            handleDocumentModified(bookClubRef, document);
                                            break;
                                        case REMOVED:
                                            handleDocumentRemoved(bookClubRef);
                                            break;
                                    }
                                }
                            }
                        } else {
                            Log.d(TAG, "Query snapshot is null or empty");
                            // Show the TextView if there are no joined groups
                            groupList.clear();
                            groupAdapter.notifyDataSetChanged();
                            tvNoJoinedGroups.setVisibility(View.VISIBLE);
                            tvNoJoinedGroups.setText("No joined groups available.");
                            Log.d(TAG, "tvNoJoinedGroups visibility set to VISIBLE due to empty query snapshot");
                        }
                    });
        } else {
            Log.d(TAG, "Context is null");
            // Show the TextView if the context is null
            tvNoJoinedGroups.setVisibility(View.VISIBLE);
            tvNoJoinedGroups.setText("Unable to fetch joined groups. Context is null.");
            Log.d(TAG, "tvNoJoinedGroups visibility set to VISIBLE due to null context");
        }
    }

    private void handleDocumentAdded(DocumentReference bookClubRef, QueryDocumentSnapshot document) {
        bookClubRef.get().addOnSuccessListener(bookClubDoc -> {
            if (bookClubDoc.exists()) {
                Group group = bookClubDoc.toObject(Group.class);
                group.setId(bookClubDoc.getId());

                // Check if joinDate is null and handle accordingly
                Timestamp joinTimestamp = document.getTimestamp("joinDate");
                if (joinTimestamp != null) {
                    group.setJoinDate(joinTimestamp.toDate());
                } else {
                    group.setJoinDate(new Date(0)); // Set a default date if joinDate is null
                }

                groupList.add(group);
                updateGroupList();
            } else {
                Log.d(TAG, "Book club document does not exist");
                tvNoJoinedGroups.setVisibility(View.VISIBLE);
                tvNoJoinedGroups.setText("No joined groups available.");
            }
        }).addOnFailureListener(e1 -> {
            Log.e(TAG, "Error fetching book club: ", e1);
            Toast.makeText(getContext(), "Error fetching book club: " + e1.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void handleDocumentModified(DocumentReference bookClubRef, QueryDocumentSnapshot document) {
        bookClubRef.get().addOnSuccessListener(bookClubDoc -> {
            if (bookClubDoc.exists()) {
                Group group = bookClubDoc.toObject(Group.class);
                group.setId(bookClubDoc.getId());

                // Check if joinDate is null and handle accordingly
                Timestamp joinTimestamp = document.getTimestamp("joinDate");
                if (joinTimestamp != null) {
                    group.setJoinDate(joinTimestamp.toDate());
                } else {
                    group.setJoinDate(new Date(0)); // Set a default date if joinDate is null
                }

                for (int i = 0; i < groupList.size(); i++) {
                    if (groupList.get(i).getId().equals(group.getId())) {
                        groupList.set(i, group);
                        break;
                    }
                }
                updateGroupList();
            } else {
                Log.d(TAG, "Book club document does not exist");
                tvNoJoinedGroups.setVisibility(View.VISIBLE);
                tvNoJoinedGroups.setText("No joined groups available.");
            }
        }).addOnFailureListener(e1 -> {
            Log.e(TAG, "Error fetching book club: ", e1);
            Toast.makeText(getContext(), "Error fetching book club: " + e1.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void handleDocumentRemoved(DocumentReference bookClubRef) {
        for (int i = 0; i < groupList.size(); i++) {
            if (groupList.get(i).getId().equals(bookClubRef.getId())) {
                groupList.remove(i);
                break;
            }
        }
        updateGroupList();
    }

    private void updateGroupList() {
        // Sort the groups based on joinDate (descending)
        groupList.sort((g1, g2) -> {
            Date t1 = g1.getJoinDate();
            Date t2 = g2.getJoinDate();
            if (t1 != null && t2 != null) {
                return t2.compareTo(t1);
            }
            return 0;
        });

        groupAdapter.notifyDataSetChanged();

        // Show or hide the TextView based on the list size
        if (groupList.isEmpty()) {
            tvNoJoinedGroups.setVisibility(View.VISIBLE);
            tvNoJoinedGroups.setText("No joined groups available.");
        } else {
            tvNoJoinedGroups.setVisibility(View.GONE);
        }
        Log.d(TAG, "tvNoJoinedGroups visibility set to: " + (groupList.isEmpty() ? "VISIBLE" : "GONE"));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (joinedGroupsListenerRegistration != null) {
            joinedGroupsListenerRegistration.remove();
            joinedGroupsListenerRegistration = null;
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
                    // Hide the TextView since a new group is added
                    tvNoJoinedGroups.setVisibility(View.GONE);
                    Log.d(TAG, "tvNoJoinedGroups visibility set to GONE after group creation");
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
