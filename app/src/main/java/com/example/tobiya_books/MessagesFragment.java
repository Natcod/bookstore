package com.example.tobiya_books;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessagesFragment extends Fragment {

    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;
    private List<Message> messageList = new ArrayList<>();
    private FirebaseFirestore db;
    private String groupId;
    private String userId; // Added field to store the user ID
    private EditText inputMessage;
    private Button sendButton;
    private Button joinButton;
    private boolean openedFromAllGroups = false;

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

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Get the boolean indicating whether opened from AllGroupsFragment
        if (getArguments() != null) {
            groupId = getArguments().getString("groupId");
            userId = getArguments().getString("userId");
            openedFromAllGroups = getArguments().getBoolean("openedFromAllGroups", false);
        }

        // Initialize RecyclerView and adapter
        messageAdapter = new MessageAdapter(messageList, userId); // Pass userId to the adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(messageAdapter);

        // Fetch messages from Firebase Firestore
        fetchMessages();

        // Set click listener for send button
        sendButton.setOnClickListener(v -> sendMessage());

        // Set click listener for join button
        joinButton.setOnClickListener(v -> joinGroup());

        // If opened from AllGroupsFragment, disable message input and send button
        if (openedFromAllGroups) {
            inputMessage.setVisibility(View.GONE);
            sendButton.setVisibility(View.GONE);
            joinButton.setVisibility(View.VISIBLE); // Show the join button
        } else {
            joinButton.setVisibility(View.GONE); // Hide the join button
        }

        return view;
    }

    private void fetchMessages() {
        db.collection("Message")
                .whereEqualTo("bookClub", db.collection("BookClub").document(groupId))
                .orderBy("sentDateTime", Query.Direction.DESCENDING) // Order by sentDateTime in descending order
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error getting messages", error);
                        Toast.makeText(getContext(), "Error getting messages: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    messageList.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot document : value) {
                            Message message = document.toObject(Message.class);
                            messageList.add(message);
                            Log.d(TAG, "Message fetched: " + message.getMessage());
                        }
                        messageAdapter.notifyDataSetChanged();
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
        DocumentReference senderRef = db.collection("Reader").document(userId); // Use the user ID as sender ID

        Map<String, Object> messageData = new HashMap<>();
        messageData.put("bookClub", groupRef);
        messageData.put("message", messageText);
        messageData.put("sender", senderRef);
        messageData.put("sentDateTime", com.google.firebase.Timestamp.now());

        db.collection("Message").add(messageData)
                .addOnSuccessListener(documentReference -> {
                    inputMessage.setText(""); // Clear the input field after message is sent
                    Toast.makeText(getContext(), "Message sent", Toast.LENGTH_SHORT).show();
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
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error joining group: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error joining group", e);
                });
    }
}
