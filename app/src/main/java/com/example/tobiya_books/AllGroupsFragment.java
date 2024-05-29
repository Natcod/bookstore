package com.example.tobiya_books;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class AllGroupsFragment extends Fragment implements GroupAdapter.OnGroupClickListener {

    private RecyclerView recyclerView;
    private GroupAdapter groupAdapter;
    private List<Group> groupList = new ArrayList<>();
    private FirebaseFirestore db;

    public AllGroupsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_groups, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_all_groups);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        groupAdapter = new GroupAdapter(groupList, this);
        recyclerView.setAdapter(groupAdapter);

        FloatingActionButton fabCreateGroup = view.findViewById(R.id.fab_create_group);
        fabCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateGroupDialog();
            }
        });

        db = FirebaseFirestore.getInstance();

        fetchGroups();

        return view;
    }

    private void fetchGroups() {
        db.collection("BookClub").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            Group group = document.toObject(Group.class);
                            if (group != null && group.getName() != null) {
                                group.setId(document.getId()); // Ensure the ID is set
                                groupList.add(group);
                            } else {
                                Log.e("FetchGroup", "Group name is null or group is null for document: " + document.getId());
                            }
                        }
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
        MessagesFragment messagesFragment = MessagesFragment.newInstance(groupId, userId);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, messagesFragment)
                .addToBackStack(null)
                .commit();
    }

}
