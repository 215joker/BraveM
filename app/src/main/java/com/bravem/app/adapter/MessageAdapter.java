package com.bravem.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bravem.app.R;
import com.bravem.app.model.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;

    private List<ChatMessage> messages = new ArrayList<>();
    private final String currentUserId;
    private final OnMessageClickListener listener;

    public interface OnMessageClickListener {
        void onAttachmentClick(ChatMessage message);
    }

    public MessageAdapter(String currentUserId, OnMessageClickListener listener) {
        this.currentUserId = currentUserId;
        this.listener = listener;
    }

    public void submitList(List<ChatMessage> list) {
        this.messages = new ArrayList<>(list);
        notifyDataSetChanged();
    }

    public void addMessage(ChatMessage message) {
        this.messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    @Override
    public int getItemViewType(int position) {
        if (messages.get(position).getSenderId().equals(currentUserId)) {
            return TYPE_SENT;
        } else {
            return TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        if (holder instanceof SentMessageViewHolder) {
            ((SentMessageViewHolder) holder).bind(message, listener);
        } else {
            ((ReceivedMessageViewHolder) holder).bind(message, listener);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvMessage, tvTime, tvAttachment;

        public SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvAttachment = itemView.findViewById(R.id.tv_attachment);
        }

        public void bind(ChatMessage message, OnMessageClickListener listener) {
            tvMessage.setText(message.getMessage());
            tvTime.setText(formatTime(message.getTimestamp()));
            if (message.getAttachmentPath() != null) {
                tvAttachment.setVisibility(View.VISIBLE);
                String type = message.getAttachmentType() != null ? message.getAttachmentType() : "File";
                tvAttachment.setText("\uD83D\uDCCE " + type); // Paperclip icon
                tvAttachment.setOnClickListener(v -> {
                    if (listener != null) listener.onAttachmentClick(message);
                });
            } else {
                tvAttachment.setVisibility(View.GONE);
                tvAttachment.setOnClickListener(null);
            }
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvMessage, tvTime, tvAttachment;

        public ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvAttachment = itemView.findViewById(R.id.tv_attachment);
        }

        public void bind(ChatMessage message, OnMessageClickListener listener) {
            tvMessage.setText(message.getMessage());
            tvTime.setText(formatTime(message.getTimestamp()));
            if (message.getAttachmentPath() != null) {
                tvAttachment.setVisibility(View.VISIBLE);
                String type = message.getAttachmentType() != null ? message.getAttachmentType() : "File";
                tvAttachment.setText("\uD83D\uDCCE " + type); // Paperclip icon
                tvAttachment.setOnClickListener(v -> {
                    if (listener != null) listener.onAttachmentClick(message);
                });
            } else {
                tvAttachment.setVisibility(View.GONE);
                tvAttachment.setOnClickListener(null);
            }
        }
    }

    private static String formatTime(long timestamp) {
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(timestamp));
    }
}
