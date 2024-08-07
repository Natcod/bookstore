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
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

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
    private TextView tvNoGroups; // TextView for the no groups message
    private ListenerRegistration listenerRegistration;
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

        // Initialize the TextView for no groups message
        tvNoGroups = view.findViewById(R.id.tv_no_groups);

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
        listenerRegistration = db.collection("BookClub").addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Log.e("FetchGroup", "Error listening for changes: ", e);
                return;
            }

            if (snapshots != null) {
                groupList.clear(); // Clear the list before adding new groups
                for (DocumentSnapshot document : snapshots) {
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

                if (groupList.isEmpty()) {
                    tvNoGroups.setVisibility(View.VISIBLE);
                } else {
                    tvNoGroups.setVisibility(View.GONE);
                }

                groupAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }

    private void showCreateGroupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Create New Group");

        // Inflate the custom layout for the dialog
        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.dialog_create_group, (ViewGroup) getView(), false);

        // Set EditText text color to purple
        final EditText input = viewInflated.findViewById(R.id.input_group_name);

        input.setTextColor(getResources().getColor(R.color.purple)); // Set EditText text color to purple
        input.setHintTextColor(getResources().getColor(R.color.purple)); // Set EditText hint color to purple
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

        AlertDialog dialog = builder.create();

        // Customize the title text color and dialog background color
        dialog.setOnShowListener(d -> {
            TextView titleView = dialog.findViewById(android.R.id.title);
            if (titleView != null) {
                titleView.setTextColor(getResources().getColor(R.color.purple));
            }

            // Customize the button text color
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.purple));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.purple));

            // Set the background color of the dialog's root view
            ViewGroup rootView = (ViewGroup) dialog.getWindow().getDecorView();
            rootView.setBackgroundColor(getResources().getColor(R.color.var));
        });

        dialog.show();
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
        groupData.put("creator", db.collection("Reader").document(currentUserId)); // Use currentUserId directly as the creator

        // Add the group data to the "BookClub" collection
        db.collection("BookClub")
                .add(groupData)
                .addOnSuccessListener(documentReference -> {
                    // Log the document reference ID
                    Log.d("CreateGroup", "Document added with ID: " + documentReference.getId());

                    // Automatically add the user as a member of the newly created group
                    addUserToGroup(documentReference.getId());

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

    private void addUserToGroup(String groupId) {
        // Get the current timestamp
        Timestamp joinDate = Timestamp.now();

        // Create a Map with the member data
        Map<String, Object> memberData = new HashMap<>();
        memberData.put("reader", db.collection("Reader").document(currentUserId));
        memberData.put("bookClub", db.collection("BookClub").document(groupId));
        memberData.put("joinDate", joinDate);

        // Add the member data to the "BookClubMember" collection
        db.collection("BookClubMember")
                .add(memberData)
                .addOnSuccessListener(documentReference -> {
                    // Log the document reference ID
                    Log.d("AddMember", "Member added with ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    // Log the error message
                    Log.e("AddMember", "Error adding member: " + e.getMessage());

                    // Display an error message
                    Toast.makeText(getContext(), "Error adding member: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
