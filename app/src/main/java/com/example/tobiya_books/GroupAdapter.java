package com.example.tobiya_books;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    private List<Group> groupList;
    private OnGroupClickListener onGroupClickListener;

    public GroupAdapter(List<Group> groupList, OnGroupClickListener onGroupClickListener) {
        this.groupList = groupList;
        this.onGroupClickListener = onGroupClickListener;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group, parent, false);
        return new GroupViewHolder(view, onGroupClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        Group group = groupList.get(position);
        if (group != null && group.getName() != null) {
            Log.d("GroupAdapter", "Binding group name: " + group.getName());
            holder.groupNameTextView.setText(group.getName());
        } else {
            Log.e("GroupAdapter", "Group or group name is null at position: " + position);
        }
    }




    @Override
    public int getItemCount() {
        return groupList.size();
    }

    public static class GroupViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView groupNameTextView;
        OnGroupClickListener onGroupClickListener;

        public GroupViewHolder(@NonNull View itemView, OnGroupClickListener onGroupClickListener) {
            super(itemView);
            groupNameTextView = itemView.findViewById(R.id.group_name_text_view);
            this.onGroupClickListener = onGroupClickListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onGroupClickListener.onGroupClick(getAdapterPosition());
        }
    }

    public interface OnGroupClickListener {
        void onGroupClick(int position);
    }
}
