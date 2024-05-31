package com.example.tobiya_books;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    private List<Group> groupList;
    private OnGroupClickListener listener;

    public interface OnGroupClickListener {
        void onGroupClick(int position);
    }

    public GroupAdapter(List<Group> groupList, OnGroupClickListener listener) {
        this.groupList = groupList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group, parent, false);
        return new GroupViewHolder(itemView, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        Group group = groupList.get(position);
        holder.groupNameTextView.setText(group.getName());
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    static class GroupViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView groupNameTextView;
        OnGroupClickListener onGroupClickListener;

        public GroupViewHolder(@NonNull View itemView, OnGroupClickListener listener) {
            super(itemView);
            groupNameTextView = itemView.findViewById(R.id.group_name_text_view);
            this.onGroupClickListener = listener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (onGroupClickListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    onGroupClickListener.onGroupClick(position);
                }
            }
        }
    }
}
