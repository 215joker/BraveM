package com.bravem.app.adapter;

import android.net.Uri;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bravem.app.R;
import com.bravem.app.data.ChatRepository;
import com.bravem.app.domain.model.User;
import com.bravem.app.model.ChatMessage;

import java.util.ArrayList;
import java.util.List;

public class RecentChatAdapter extends RecyclerView.Adapter<RecentChatAdapter.ViewHolder> {

    private List<ChatRepository.RecentChat> chats = new ArrayList<>();
    private final OnChatClickListener listener;

    public interface OnChatClickListener {
        void onChatClick(User user);
    }

    public RecentChatAdapter(OnChatClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<ChatRepository.RecentChat> list) {
        this.chats = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recent_chat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatRepository.RecentChat chat = chats.get(position);
        holder.bind(chat, listener);
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgProfile;
        private final TextView textName, textLastMessage, textTime, textUnreadCount;
        private final View rootLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rootLayout = itemView.findViewById(R.id.chat_item_root);
            imgProfile = itemView.findViewById(R.id.img_profile);
            textName = itemView.findViewById(R.id.text_name);
            textLastMessage = itemView.findViewById(R.id.text_last_message);
            textTime = itemView.findViewById(R.id.text_time);
            textUnreadCount = itemView.findViewById(R.id.text_unread_count);
        }

        public void bind(ChatRepository.RecentChat chat, OnChatClickListener listener) {
            User user = chat.user;
            if (user == null) return;
            ChatMessage lastMsg = chat.lastMessage;

            textName.setText(user.getFullName());
            
            if (lastMsg != null) {
                textLastMessage.setText(lastMsg.getMessage() != null ? lastMsg.getMessage() : "Sent an attachment");
                textTime.setText(DateUtils.getRelativeTimeSpanString(lastMsg.getTimestamp()));
                textTime.setVisibility(View.VISIBLE);
            } else {
                textLastMessage.setText("Start a conversation");
                textTime.setVisibility(View.GONE);
            }

            if (chat.unreadCount > 0) {
                textUnreadCount.setVisibility(View.VISIBLE);
                textUnreadCount.setText(String.valueOf(chat.unreadCount));
            } else {
                textUnreadCount.setVisibility(View.GONE);
            }

            if (user.getProfilePicture() != null) {
                try {
                    String path = user.getProfilePicture();
                    if (path.startsWith("/")) {
                        imgProfile.setImageURI(Uri.fromFile(new java.io.File(path)));
                    } else {
                        imgProfile.setImageURI(Uri.parse(path));
                    }
                } catch (Exception e) {
                    imgProfile.setImageResource(R.drawable.ic_person);
                }
            } else {
                imgProfile.setImageResource(R.drawable.ic_person);
            }

            rootLayout.setOnClickListener(v -> listener.onChatClick(user));
        }
    }
}
