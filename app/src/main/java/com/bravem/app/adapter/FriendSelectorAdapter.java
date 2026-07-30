package com.bravem.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bravem.app.R;
import com.bravem.app.domain.model.User;
import java.util.ArrayList;
import java.util.List;

public class FriendSelectorAdapter extends RecyclerView.Adapter<FriendSelectorAdapter.ViewHolder> {

    private final List<User> friends;
    private final List<String> selectedIds = new ArrayList<>();

    public FriendSelectorAdapter(List<User> friends) {
        this.friends = friends;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_selector, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User friend = friends.get(position);
        holder.tvName.setText(friend.getFullName());
        holder.checkBox.setChecked(selectedIds.contains(friend.getUid()));

        holder.itemView.setOnClickListener(v -> {
            if (selectedIds.contains(friend.getUid())) {
                selectedIds.remove(friend.getUid());
            } else {
                selectedIds.add(friend.getUid());
            }
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    public List<String> getSelectedIds() {
        return selectedIds;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        CheckBox checkBox;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            checkBox = itemView.findViewById(R.id.checkbox);
        }
    }
}
