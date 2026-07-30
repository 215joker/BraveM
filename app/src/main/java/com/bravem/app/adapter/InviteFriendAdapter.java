package com.bravem.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bravem.app.R;
import com.bravem.app.domain.model.User;
import com.google.android.material.button.MaterialButton;
import java.util.List;
import java.util.Set;

public class InviteFriendAdapter extends RecyclerView.Adapter<InviteFriendAdapter.ViewHolder> {

    private final List<User> friends;
    private final Set<String> inCallUserIds;
    private final OnInviteClickListener listener;

    public interface OnInviteClickListener {
        void onInviteClick(User friend);
    }

    public InviteFriendAdapter(List<User> friends, Set<String> inCallUserIds, OnInviteClickListener listener) {
        this.friends = friends;
        this.inCallUserIds = inCallUserIds;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_invite_friend, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User friend = friends.get(position);
        holder.tvName.setText(friend.getFullName());
        
        boolean isInCall = inCallUserIds.contains(friend.getUid());
        if (isInCall) {
            holder.tvStatus.setText("In call");
            holder.tvStatus.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.success_600));
            holder.btnInvite.setVisibility(View.GONE);
        } else {
            holder.tvStatus.setText("Available");
            holder.tvStatus.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.text_secondary));
            holder.btnInvite.setVisibility(View.VISIBLE);
            holder.btnInvite.setOnClickListener(v -> listener.onInviteClick(friend));
        }
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvStatus;
        MaterialButton btnInvite;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvStatus = itemView.findViewById(R.id.tv_status);
            btnInvite = itemView.findViewById(R.id.btn_invite);
        }
    }
}
