package com.example.tobiya_books;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Message> messageList;
    private String currentUserId; // Store the current user's ID

    public MessageAdapter(List<Message> messageList, String currentUserId) {
        this.messageList = messageList;
        this.currentUserId = currentUserId; // Initialize currentUserId
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);
        holder.messageTextView.setText(message.getMessage());
        holder.sentDateTimeTextView.setText(formatTimestamp(message.getSentDateTime()));

        // Retrieve sender's username and set senderTextView
        DocumentReference senderRef = message.getSender();
        if (senderRef != null) {
            senderRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String senderUsername = documentSnapshot.getString("username"); // Assuming "username" is the field in Firestore containing the username
                    holder.senderTextView.setText(senderUsername);

                    String senderId = documentSnapshot.getId(); // Get sender's ID

                    // Check if the sender is the current user and align message views accordingly
                    if (senderId.equals(currentUserId)) {
                        // If sender is the current user, align message views to the right
                        holder.leftAlignmentView.setVisibility(View.GONE); // Hide the left alignment view
                        holder.messageTextView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                        holder.senderTextView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END); // Align sender to right
                        holder.sentDateTimeTextView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                    } else {
                        // If sender is not the current user, align message views to the left
                        holder.leftAlignmentView.setVisibility(View.VISIBLE); // Show the left alignment view
                        holder.messageTextView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                        holder.senderTextView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START); // Align sender to left
                        holder.sentDateTimeTextView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                    }

                } else {
                    holder.senderTextView.setText("Unknown sender");
                }
            }).addOnFailureListener(e -> {
                holder.senderTextView.setText("Error retrieving sender");
                Log.e("MessageAdapter", "Error retrieving sender", e);
            });
        } else {
            holder.senderTextView.setText("Unknown sender");
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    private String formatTimestamp(Date date) {
        if (date != null) {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(date);
        } else {
            return "Unknown time";
        }
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {

        TextView messageTextView;
        TextView sentDateTimeTextView;
        TextView senderTextView; // Added TextView for sender
        View leftAlignmentView; // View for left alignment

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.message_text_view);
            sentDateTimeTextView = itemView.findViewById(R.id.sent_date_time_text_view);
            senderTextView = itemView.findViewById(R.id.sender_text_view); // Initialize sender TextView
            leftAlignmentView = itemView.findViewById(R.id.left_alignment_view);
        }
    }
}
