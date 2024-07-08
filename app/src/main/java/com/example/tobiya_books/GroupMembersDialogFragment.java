package com.example.tobiya_books;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class GroupMembersDialogFragment extends DialogFragment {

    private static final String ARG_MEMBERS = "members";
    private List<GroupMember> members;

    public static GroupMembersDialogFragment newInstance(List<GroupMember> members) {
        GroupMembersDialogFragment fragment = new GroupMembersDialogFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_MEMBERS, new ArrayList<>(members));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            members = getArguments().getParcelableArrayList(ARG_MEMBERS);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_group_members, null);
        builder.setView(view);

        TextView header = view.findViewById(R.id.text_group_members_header);
        header.setText(String.format("Group Members (%d)", members.size()));

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_members);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new GroupMemberAdapter(members));

        Button cancelButton = view.findViewById(R.id.button_cancel);
        cancelButton.setOnClickListener(v -> dismiss());

        return builder.create();
    }

    private static class GroupMemberAdapter extends RecyclerView.Adapter<GroupMemberAdapter.ViewHolder> {
        private List<GroupMember> members;

        GroupMemberAdapter(List<GroupMember> members) {
            this.members = members;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group_member, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            GroupMember member = members.get(position);
            holder.firstName.setText(member.getFirstName());
            holder.lastName.setText(member.getLastName());
            Glide.with(holder.itemView.getContext()).load(member.getProfilePictureUrl()).into(holder.profilePicture);
        }

        @Override
        public int getItemCount() {
            return members.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView firstName;
            TextView lastName;
            ImageView profilePicture;

            ViewHolder(View itemView) {
                super(itemView);
                firstName = itemView.findViewById(R.id.text_first_name);
                lastName = itemView.findViewById(R.id.text_last_name);
                profilePicture = itemView.findViewById(R.id.image_profile_picture);
            }
        }
    }
}
