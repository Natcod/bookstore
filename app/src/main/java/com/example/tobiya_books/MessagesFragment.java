package com.example.tobiya_books;

import android.content.Context;
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

    private static final String TAG = "MessagesFragment";

    public MessagesFragment() {
        // Required empty public constructor
    }

    public static MessagesFragment newInstance(String groupId, String userId) {
        MessagesFragment fragment = new MessagesFragment();
        Bundle args = new Bundle();
        args.putString("groupId", groupId);
        args.putString("userId", userId); // Pass the user ID as an argument
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messages, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_messages);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Get the user ID from arguments
        if (getArguments() != null) {
            groupId = getArguments().getString("groupId");
            userId = getArguments().getString("userId");
        }

        // Create the MessageAdapter with the messageList and user ID
        messageAdapter = new MessageAdapter(messageList, userId);
        recyclerView.setAdapter(messageAdapter);

        inputMessage = view.findViewById(R.id.input_message);
        sendButton = view.findViewById(R.id.send_button);

        db = FirebaseFirestore.getInstance();

        sendButton.setOnClickListener(v -> sendMessage());

        fetchMessages();

        return view;
    }

    private void fetchMessages() {
        db.collection("Message")
                .whereEqualTo("bookClub", db.collection("BookClub").document(groupId))
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
                    inputMessage.setText("");
                    Toast.makeText(getContext(), "Message sent", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error sending message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
