package com.bravem.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bravem.app.R;
import com.bravem.app.model.User;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    public interface OnUserActionListener {
        void onDelete(User user);
        void onSuspend(User user);
        void onRoleChange(User user, String newRole);
    }

    private List<User> users = new ArrayList<>();
    private List<User> allUsers = new ArrayList<>();
    private final OnUserActionListener listener;

    public UserAdapter(OnUserActionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<User> list) {
        this.allUsers = new ArrayList<>(list);
        this.users = new ArrayList<>(list);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        users.clear();
        if (query.isEmpty()) {
            users.addAll(allUsers);
        } else {
            for (User u : allUsers) {
                if (u.getFullName().toLowerCase().contains(query.toLowerCase()) ||
                    u.getEmail().toLowerCase().contains(query.toLowerCase())) {
                    users.add(u);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.bind(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameView, emailView, roleBadge;
        private final ImageButton deleteButton, suspendButton;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.text_user_name);
            emailView = itemView.findViewById(R.id.text_user_email);
            roleBadge = itemView.findViewById(R.id.badge_role);
            deleteButton = itemView.findViewById(R.id.btn_delete_user);
            suspendButton = itemView.findViewById(R.id.btn_suspend_user);
        }

        public void bind(User user) {
            nameView.setText(user.getFullName());
            emailView.setText(user.getEmail());
            
            String roleText = user.getRole();
            if (user.isSuspended()) {
                roleText += " (SUSPENDED)";
                roleBadge.setBackgroundTintList(itemView.getContext().getColorStateList(R.color.error_600));
            } else {
                roleBadge.setBackgroundTintList(null);
            }
            roleBadge.setText(roleText);

            deleteButton.setOnClickListener(v -> listener.onDelete(user));
            suspendButton.setOnClickListener(v -> listener.onSuspend(user));
        }
    }
}
