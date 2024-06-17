package com.example.tobiya_books;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessagesFragment extends Fragment {

    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;
    private List<Message> messageList = new ArrayList<>();
    private FirebaseFirestore db;
    private String groupId;
    private String userId;
    private EditText inputMessage;
    private Button sendButton;
    private Button joinButton;
    private ImageButton backButton;
    private ImageButton deleteButton;
    private boolean openedFromAllGroups = false;
    private boolean isMember = false;

    private static final String TAG = "MessagesFragment";

    public MessagesFragment() {
        // Required empty public constructor
    }

    public static MessagesFragment newInstance(String groupId, String userId, boolean openedFromAllGroups) {
        MessagesFragment fragment = new MessagesFragment();
        Bundle args = new Bundle();
        args.putString("groupId", groupId);
        args.putString("userId", userId);
        args.putBoolean("openedFromAllGroups", openedFromAllGroups);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messages, container, false);

        // Initialize views
        recyclerView = view.findViewById(R.id.recycler_view_messages);
        inputMessage = view.findViewById(R.id.input_message);
        sendButton = view.findViewById(R.id.send_button);
        joinButton = view.findViewById(R.id.join_button);
        backButton = view.findViewById(R.id.back_button);
        deleteButton = view.findViewById(R.id.delete_button);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Get arguments
        if (getArguments() != null) {
            groupId = getArguments().getString("groupId");
            userId = getArguments().getString("userId");
            openedFromAllGroups = getArguments().getBoolean("openedFromAllGroups", false);
        }

        // Initialize RecyclerView and adapter
        messageAdapter = new MessageAdapter(messageList, userId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(messageAdapter);

        // Add scroll listener to load older messages on scroll up
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && layoutManager.findFirstCompletelyVisibleItemPosition() == 0) {
                    // Load more messages when scrolled to the top
                    loadOlderMessages();
                }
            }
        });

        // Check membership status
        checkMembershipStatus();

        // Set click listener for send button
        sendButton.setOnClickListener(v -> sendMessage());

        // Set click listener for join button
        joinButton.setOnClickListener(v -> joinGroup());

        // Set click listener for back button
        backButton.setOnClickListener(v -> getActivity().onBackPressed());

        // Set click listener for delete button
        deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog());

        return view;
    }

    private void fetchMessages() {
        db.collection("Message")
                .whereEqualTo("bookClub", db.collection("BookClub").document(groupId))
                .orderBy("sentDateTime")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error getting messages", error);
                        Toast.makeText(getContext(), "Error getting messages: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        List<Message> newMessages = new ArrayList<>();
                        for (QueryDocumentSnapshot document : value) {
                            Message message = document.toObject(Message.class);
                            newMessages.add(message);
                            Log.d(TAG, "Message fetched: " + message.getMessage());
                        }

                        // Sort messages in chronological order
                        Collections.sort(newMessages, (m1, m2) -> m1.getSentDateTime().compareTo(m2.getSentDateTime()));

                        // Check if the new messages list is different from the current one
                        if (!newMessages.equals(messageList)) {
                            messageList.clear();
                            messageList.addAll(newMessages);
                            messageAdapter.notifyDataSetChanged();

                            // Scroll to the bottom to show the latest messages
                            recyclerView.scrollToPosition(messageList.size() - 1);
                        }
                    }
                });
    }

    private void sendMessage() {
        String messageText = inputMessage.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) {
            Toast.makeText(getContext(), "Message cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference groupRef = db.collection("BookClub").document(groupId);
        DocumentReference senderRef = db.collection("Reader").document(userId);

        Map<String, Object> messageData = new HashMap<>();
        messageData.put("bookClub", groupRef);
        messageData.put("message", messageText);
        messageData.put("sender", senderRef);
        messageData.put("sentDateTime", com.google.firebase.Timestamp.now());

        db.collection("Message").add(messageData)
                .addOnSuccessListener(documentReference -> {
                    inputMessage.getText().clear();
                    Toast.makeText(getContext(), "Message sent", Toast.LENGTH_SHORT).show();
                    // Scroll to the bottom to show the latest message
                    recyclerView.scrollToPosition(messageList.size() - 1);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error sending message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void joinGroup() {
        DocumentReference groupRef = db.collection("BookClub").document(groupId);
        DocumentReference readerRef = db.collection("Reader").document(userId);

        Map<String, Object> memberData = new HashMap<>();
        memberData.put("bookClub", groupRef);
        memberData.put("joinDate", com.google.firebase.Timestamp.now());
        memberData.put("reader", readerRef);

        db.collection("BookClubMember").add(memberData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Joined group successfully", Toast.LENGTH_SHORT).show();
                    isMember = true;
                    updateUI();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error joining group: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error joining group", e);
                });
    }

    private void checkMembershipStatus() {
        db.collection("BookClubMember")
                .whereEqualTo("bookClub", db.collection("BookClub").document(groupId))
                .whereEqualTo("reader", db.collection("Reader").document(userId))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    isMember = !queryDocumentSnapshots.isEmpty();
                    updateUI();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking membership status", e);
                    Toast.makeText(getContext(), "Error checking membership status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUI() {
        if (isMember) {
            joinButton.setVisibility(View.GONE);
            inputMessage.setVisibility(View.VISIBLE);
            sendButton.setVisibility(View.VISIBLE);
            deleteButton.setVisibility(View.VISIBLE);
            fetchMessages();
        } else {
            if (openedFromAllGroups) {
                joinButton.setVisibility(View.VISIBLE);
                inputMessage.setVisibility(View.GONE);
                sendButton.setVisibility(View.GONE);
                deleteButton.setVisibility(View.GONE);
            } else {
                joinButton.setVisibility(View.GONE);
                inputMessage.setVisibility(View.VISIBLE);
                sendButton.setVisibility(View.VISIBLE);
                deleteButton.setVisibility(View.GONE);
                fetchMessages();
            }
        }
    }

    private void loadOlderMessages() {
        // Implement logic to load older messages, possibly using Firestore's pagination features
        // For simplicity, this is a placeholder
        Log.d(TAG, "Load older messages...");
        // Here you would typically load older messages by querying Firestore with appropriate limits and offsets
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Remove from Group")
                .setMessage("Are you sure you want to remove yourself from this group?")
                .setPositiveButton("Yes", (dialog, which) -> removeUserFromGroup())
                .setNegativeButton("No", null)
                .show();
    }

    private void removeUserFromGroup() {
        db.collection("BookClubMember")
                .whereEqualTo("bookClub", db.collection("BookClub").document(groupId))
                .whereEqualTo("reader", db.collection("Reader").document(userId))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        db.collection("BookClubMember").document(document.getId()).delete()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getContext(), "Removed from group successfully", Toast.LENGTH_SHORT).show();
                                    getActivity().onBackPressed();  // Go back to the previous screen
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error removing user from group", e);
                                    Toast.makeText(getContext(), "Error removing user from group: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching membership document", e);
                    Toast.makeText(getContext(), "Error removing user from group: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
