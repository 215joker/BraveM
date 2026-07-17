package com.bravem.app.adapter;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bravem.app.R;
import com.bravem.app.model.Notification;
import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<Notification> notifications = new ArrayList<>();
    private final OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    public NotificationAdapter(OnNotificationClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Notification> newList) {
        this.notifications = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.title.setText(notification.getTitle());
        holder.message.setText(notification.getMessage());
        holder.time.setText(DateUtils.getRelativeTimeSpanString(notification.getTimestamp()));
        holder.unreadIndicator.setVisibility(notification.isRead() ? View.GONE : View.VISIBLE);
        holder.itemView.setOnClickListener(v -> listener.onNotificationClick(notification));
    }

    public Notification getNotificationAt(int position) {
        return notifications.get(position);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, message, time;
        View unreadIndicator;

        ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.text_notification_title);
            message = itemView.findViewById(R.id.text_notification_message);
            time = itemView.findViewById(R.id.text_notification_time);
            unreadIndicator = itemView.findViewById(R.id.unread_indicator);
        }
    }
}
