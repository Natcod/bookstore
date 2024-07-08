package com.example.tobiya_books;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

public class GroupMemberAdapter extends RecyclerView.Adapter<GroupMemberAdapter.ViewHolder> {

    private static final String TAG = "GroupMemberAdapter";
    private List<GroupMember> groupMemberList;

    public GroupMemberAdapter(List<GroupMember> groupMemberList) {
        this.groupMemberList = groupMemberList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group_member, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GroupMember member = groupMemberList.get(position);
        holder.firstNameTextView.setText(member.getFirstName());
        holder.lastNameTextView.setText(member.getLastName());
        String profilePictureUrl = member.getProfilePictureUrl();

        Log.d(TAG, "Loading image: " + profilePictureUrl);

        Picasso.get()
                .load(profilePictureUrl)
                .placeholder(R.drawable.baseline_person_24) // Add a placeholder image
                .error(R.drawable.baseline_person_24) // Add an error image
                .into(holder.profileImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Image loaded successfully: " + profilePictureUrl);
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Error loading image: " + profilePictureUrl, e);
                    }
                });
    }

    @Override
    public int getItemCount() {
        return groupMemberList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView profileImageView;
        TextView firstNameTextView;
        TextView lastNameTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.image_profile_picture);
            firstNameTextView = itemView.findViewById(R.id.text_first_name);
            lastNameTextView = itemView.findViewById(R.id.text_last_name);
        }
    }
}
