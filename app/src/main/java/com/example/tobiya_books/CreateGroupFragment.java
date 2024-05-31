package com.example.tobiya_books;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CreateGroupFragment extends Fragment {

    private EditText groupNameEditText;
    private Button createGroupButton;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public CreateGroupFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_group, container, false);

        // Initialize views
        groupNameEditText = view.findViewById(R.id.edit_text_group_name);
        createGroupButton = view.findViewById(R.id.button_create_group);

        // Initialize Firebase instances
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Setup click listener for create group button
        createGroupButton.setOnClickListener(v -> createGroup());

        return view;
    }

    private void createGroup() {
        String groupName = groupNameEditText.getText().toString().trim();

        if (TextUtils.isEmpty(groupName)) {
            Toast.makeText(getContext(), "Group name cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the current logged-in user's ID
        String creatorId = getCurrentUserId();

        if (creatorId == null) {
            // Handle the case where the user ID cannot be retrieved
            Toast.makeText(getContext(), "Unable to retrieve user ID.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a reference to the creator's document
        DocumentReference creatorRef = db.collection("Reader").document(creatorId);

        // Create a Map with group data
        Map<String, Object> groupData = new HashMap<>();
        groupData.put("bookClubName", groupName);
        groupData.put("creationDate", com.google.firebase.Timestamp.now());
        groupData.put("creator", creatorRef);


        // Add the new group to Firestore
        db.collection("BookClub")
                .add(groupData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Group created successfully!", Toast.LENGTH_SHORT).show();
                    // Clear input field after successful creation
                    groupNameEditText.setText("");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to create group: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private String getCurrentUserId() {
        // Retrieve the current user ID from SharedPreferences
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getString("UserID", null);
    }
}
