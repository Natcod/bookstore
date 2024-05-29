package com.example.tobiya_books;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class JoinedFragment extends Fragment implements GroupAdapter.OnGroupClickListener {

    private RecyclerView recyclerView;
    private GroupAdapter groupAdapter;
    private List<Group> groupList = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

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
        mAuth = FirebaseAuth.getInstance();

        fetchJoinedGroups();

        return view;
    }

    private void fetchJoinedGroups() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("BookClub")
                    .whereArrayContains("members", userId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            groupList.clear();
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null) {
                                for (QueryDocumentSnapshot document : querySnapshot) {
                                    Group group = document.toObject(Group.class);
                                    groupList.add(group);
                                }
                                groupAdapter.notifyDataSetChanged();
                            }
                        } else {
                            Toast.makeText(getContext(), "Error getting joined groups: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
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
        MessagesFragment messagesFragment = MessagesFragment.newInstance(groupId, userId);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, messagesFragment)
                .addToBackStack(null)
                .commit();
    }
}
