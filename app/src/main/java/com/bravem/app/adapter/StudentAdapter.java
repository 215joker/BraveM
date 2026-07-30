package com.bravem.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bravem.app.R;
import com.bravem.app.domain.model.User;

public class StudentAdapter extends ListAdapter<User, StudentAdapter.StudentViewHolder> {

    private final OnStudentActionListener listener;
    private boolean showAddFriendButton = true;

    public interface OnStudentActionListener {
        void onStudentClick(User student);
        void onAddFriendClick(User student);
    }

    public StudentAdapter(OnStudentActionListener listener) {
        this(listener, true);
    }

    public StudentAdapter(OnStudentActionListener listener, boolean showAddFriendButton) {
        super(new DiffUtil.ItemCallback<User>() {
            @Override
            public boolean areItemsTheSame(@NonNull User oldItem, @NonNull User newItem) {
                return oldItem.getUid().equals(newItem.getUid());
            }

            @Override
            public boolean areContentsTheSame(@NonNull User oldItem, @NonNull User newItem) {
                return oldItem.getFullName().equals(newItem.getFullName()) &&
                       (oldItem.getUniversity() != null && oldItem.getUniversity().equals(newItem.getUniversity())) &&
                       (oldItem.getIntake() != null && oldItem.getIntake().equals(newItem.getIntake()));
            }
        });
        this.listener = listener;
        this.showAddFriendButton = showAddFriendButton;
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        holder.bind(getItem(position), listener, showAddFriendButton);
    }

    static class StudentViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName, tvUniversity, tvDegreeIntake;
        private final ImageView imgProfile;
        private final ImageButton btnAddFriend;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvUniversity = itemView.findViewById(R.id.tv_university);
            tvDegreeIntake = itemView.findViewById(R.id.tv_degree_intake);
            imgProfile = itemView.findViewById(R.id.img_profile);
            btnAddFriend = itemView.findViewById(R.id.btn_friend_request);
        }

        public void bind(User student, OnStudentActionListener listener, boolean showAddButton) {
            tvName.setText(student.getFullName());
            tvUniversity.setText(student.getUniversity());
            String info = student.getDegreeName() + " • " + student.getIntake();
            tvDegreeIntake.setText(info);

            itemView.setOnClickListener(v -> listener.onStudentClick(student));
            btnAddFriend.setVisibility(showAddButton ? View.VISIBLE : View.GONE);
            btnAddFriend.setOnClickListener(v -> listener.onAddFriendClick(student));
        }
    }
}
