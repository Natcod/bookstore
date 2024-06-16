package com.example.tobiya_books;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Message> messageList;
    private String currentUserId;
    public MessageAdapter(List<Message> messageList, String currentUserId) {
        this.messageList = messageList;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
        return message.getSender().getId().equals(currentUserId) ? 1 : 0;
    }
    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 1) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_right, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_left, parent, false);
        }
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);
        holder.messageTextView.setText(message.getMessage());
        holder.sentDateTimeTextView.setText(formatTimestamp(message.getSentDateTime()));

        DocumentReference senderRef = message.getSender();
        if (senderRef != null) {
            senderRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String firstName = documentSnapshot.getString("firstName");
                    String lastName = documentSnapshot.getString("lastName");
                    String profilePictureUrl = documentSnapshot.getString("profilePhotoUrl");

                    holder.firstNameTextView.setText(firstName != null ? firstName : "First Name");
                    holder.lastNameTextView.setText(lastName != null ? lastName : "Last Name");

                    if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
                        Glide.with(holder.itemView.getContext())
                                .load(profilePictureUrl)
                                .circleCrop()
                                .into(holder.profileImageView);
                        holder.initialTextView.setVisibility(View.GONE);
                        holder.profileImageView.setVisibility(View.VISIBLE);
                    } else {
                        String initial = (firstName != null && !firstName.isEmpty()) ? firstName.substring(0, 1) : "?";
                        holder.initialTextView.setText(initial);
                        holder.initialTextView.setVisibility(View.VISIBLE);
                        holder.profileImageView.setVisibility(View.GONE);
                    }
                } else {
                    holder.initialTextView.setText("?");
                    holder.initialTextView.setVisibility(View.VISIBLE);
                    holder.profileImageView.setVisibility(View.GONE);
                }
            }).addOnFailureListener(e -> {
                holder.initialTextView.setText("?");
                holder.initialTextView.setVisibility(View.VISIBLE);
                holder.profileImageView.setVisibility(View.GONE);
            });
        } else {
            holder.initialTextView.setText("?");
            holder.initialTextView.setVisibility(View.VISIBLE);
            holder.profileImageView.setVisibility(View.GONE);
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
        TextView initialTextView;
        TextView firstNameTextView;
        TextView lastNameTextView;
        ImageView profileImageView;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.message_text_view);
            sentDateTimeTextView = itemView.findViewById(R.id.sent_date_time_text_view);
            initialTextView = itemView.findViewById(R.id.initial_text_view);
            firstNameTextView = itemView.findViewById(R.id.first_name_text_view);
            lastNameTextView = itemView.findViewById(R.id.last_name_text_view);
            profileImageView = itemView.findViewById(R.id.profile_image_view);
        }
    }
}
