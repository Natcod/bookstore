package com.example.tobiya_books;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllGroupsFragment extends Fragment implements GroupAdapter.OnGroupClickListener {

    private RecyclerView recyclerView;
    private GroupAdapter groupAdapter;
    private List<Group> groupList = new ArrayList<>();
    private FirebaseFirestore db;
    private String currentUserId; // Store the current user ID here

    public AllGroupsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_groups, container, false);

        // Initialize RecyclerView and adapter
        recyclerView = view.findViewById(R.id.recycler_view_all_groups);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        groupAdapter = new GroupAdapter(groupList, this);
        recyclerView.setAdapter(groupAdapter);

        // Access the MaterialButton from the layout
        MaterialButton btnCreateGroup = view.findViewById(R.id.btn_create_group);
        btnCreateGroup.setOnClickListener(v -> showCreateGroupDialog());

        // Initialize Firebase Firestore instance
        db = FirebaseFirestore.getInstance();

        // Retrieve the current user ID from SharedPreferences
        currentUserId = getCurrentUserId();

        // Fetch groups from Firestore
        fetchGroups();

        return view;
    }

    private void fetchGroups() {
        db.collection("BookClub").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        groupList.clear(); // Clear the list before adding new groups
                        for (DocumentSnapshot document : task.getResult()) {
                            Group group = document.toObject(Group.class);
                            if (group != null && group.getName() != null) {
                                group.setId(document.getId()); // Ensure the ID is set
                                groupList.add(group);
                            } else {
                                Log.e("FetchGroup", "Group name is null or group is null for document: " + document.getId());
                            }
                        }
                        // Sort the groupList based on creationDate
                        Collections.sort(groupList, (group1, group2) -> {
                            if (group1.getCreationDate() == null && group2.getCreationDate() == null) {
                                return 0; // Both dates are null, consider them equal
                            } else if (group1.getCreationDate() == null) {
                                return 1; // group1's date is null, place it after group2
                            } else if (group2.getCreationDate() == null) {
                                return -1; // group2's date is null, place it after group1
                            }
                            // Compare non-null creationDate values
                            return group2.getCreationDate().compareTo(group1.getCreationDate());
                        });
                        groupAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("FetchGroup", "Error getting documents: ", task.getException());
                    }
                });
    }

    private void showCreateGroupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Create New Group");

        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.dialog_create_group, (ViewGroup) getView(), false);
        final EditText input = viewInflated.findViewById(R.id.input_group_name);
        builder.setView(viewInflated);

        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            dialog.dismiss();
            String groupName = input.getText().toString();
            if (!groupName.isEmpty()) {
                createNewGroup(groupName);
            } else {
                Toast.makeText(getContext(), "Group name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void createNewGroup(String groupName) {
        // Ensure currentUserId is not null
        if (currentUserId == null) {
            Toast.makeText(getContext(), "Current user ID not found.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the current timestamp
        Timestamp timestamp = Timestamp.now();

        // Create a Map with the group data
        Map<String, Object> groupData = new HashMap<>();
        groupData.put("bookClubName", groupName);
        groupData.put("creationDate", timestamp);
        groupData.put("creator", db.document("/Reader/" + currentUserId)); // Assuming currentUserId is the ID of the user document

        // Add the group data to the "BookClub" collection
        db.collection("BookClub")
                .add(groupData)
                .addOnSuccessListener(documentReference -> {
                    // Log the document reference ID
                    Log.d("CreateGroup", "Document added with ID: " + documentReference.getId());

                    // Display a success message
                    Toast.makeText(getContext(), "Group created", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Log the error message
                    Log.e("CreateGroup", "Error creating group: " + e.getMessage());

                    // Display an error message
                    Toast.makeText(getContext(), "Error creating group: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private String getCurrentUserId() {
        // Retrieve current user ID from SharedPreferences
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getString("UserID", null);
    }

    @Override
    public void onGroupClick(int position) {
        Group selectedGroup = groupList.get(position);
        String groupId = selectedGroup.getId(); // Get the group ID

        // Pass both groupId and userId to openMessagesFragment
        openMessagesFragment(groupId, currentUserId);
    }

    private void openMessagesFragment(String groupId, String userId) {
        MessagesFragment messagesFragment = MessagesFragment.newInstance(groupId, userId, true); // Pass true for opened from AllGroupsFragment
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, messagesFragment)
                .addToBackStack(null)
                .commit();
    }
}
