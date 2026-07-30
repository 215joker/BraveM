package com.bravem.app.ui.notifications;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bravem.app.R;
import com.bravem.app.adapter.NotificationAdapter;
import com.bravem.app.data.DataCallback;
import com.bravem.app.data.NotificationRepository;
import com.bravem.app.model.Notification;
import com.bravem.app.ui.papers.PaperViewerActivity;
import com.bravem.app.utils.UiUtils;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private NotificationAdapter adapter;
    private View emptyState;
    private NotificationRepository notificationRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiUtils.applyEdgeToEdge(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        Toolbar toolbar = findViewById(R.id.toolbar);
        UiUtils.handleTopInset(findViewById(R.id.app_bar));
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        UiUtils.handleBottomInset(findViewById(R.id.recycler_notifications));

        notificationRepository = new NotificationRepository(this);

        RecyclerView recyclerView = findViewById(R.id.recycler_notifications);
        emptyState = findViewById(R.id.empty_state);

        adapter = new NotificationAdapter(this::handleNotificationClick);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        new androidx.recyclerview.widget.ItemTouchHelper(new androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(0, androidx.recyclerview.widget.ItemTouchHelper.LEFT | androidx.recyclerview.widget.ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                Notification notification = adapter.getNotificationAt(position);
                
                // Optimistically remove from adapter
                adapter.removeNotificationAt(position);
                
                notificationRepository.deleteNotification(notification, new DataCallback<>() {
                    @Override
                    public void onSuccess(Void result) {
                        // Check if empty state needs to be shown
                        emptyState.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
                    }
                    @Override
                    public void onError(Exception e) {
                        // Re-fetch everything if deletion fails
                        loadNotifications();
                    }
                });
            }
        }).attachToRecyclerView(recyclerView);

        loadNotifications();
    }

    private void loadNotifications() {
        notificationRepository.fetchAllNotifications(new DataCallback<>() {
            @Override
            public void onSuccess(List<Notification> notifications) {
                adapter.submitList(notifications);
                emptyState.setVisibility(notifications.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(NotificationActivity.this, "Failed to load notifications", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleNotificationClick(Notification notification) {
        if ("friend_request".equals(notification.getType())) {
            com.bravem.app.utils.DialogUtils.showConfirmation(this,
                    notification.getTitle(),
                    notification.getMessage(),
                    getString(R.string.accept),
                    () -> acceptFriendRequest(notification));
        } else if ("chat_message".equals(notification.getType())) {
            dismissNotification(notification);
            if (notification.getRelatedId() != null) {
                fetchUserAndOpenChat(notification.getRelatedId());
            }
        } else {
            dismissNotification(notification);
            if (notification.getRelatedId() != null) {
                if ("new_paper".equals(notification.getType()) || "upload".equals(notification.getType())) {
                    executorFetchPaper(notification.getRelatedId());
                } else if ("video_call_invite".equals(notification.getType())) {
                    Intent intent = new Intent(this, com.bravem.app.ui.community.StudyRoomActivity.class);
                    intent.putExtra(com.bravem.app.ui.community.StudyRoomActivity.EXTRA_CHANNEL_ID, notification.getRelatedId());
                    startActivity(intent);
                }
            }
        }
    }

    private void fetchUserAndOpenChat(String userId) {
        FirebaseDatabase.getInstance().getReference("users").child(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                com.bravem.app.model.User dataUser = task.getResult().getValue(com.bravem.app.model.User.class);
                if (dataUser != null) {
                    com.bravem.app.domain.model.User user = com.bravem.app.domain.model.UserMapper.toDomain(dataUser);
                    Intent intent = new Intent(NotificationActivity.this, com.bravem.app.ui.community.ChatActivity.class);
                    intent.putExtra(com.bravem.app.ui.community.ChatActivity.EXTRA_USER, user);
                    startActivity(intent);
                }
            } else {
                Toast.makeText(NotificationActivity.this, "Failed to open chat", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void acceptFriendRequest(Notification notification) {
        com.bravem.app.data.CommunityRepository communityRepository = new com.bravem.app.data.CommunityRepository(this);
        communityRepository.acceptFriendRequest(notification.getRelatedId(), new DataCallback<>() {
            @Override
            public void onSuccess(Boolean success) {
                Toast.makeText(NotificationActivity.this, R.string.friend_request_accepted, Toast.LENGTH_SHORT).show();
                dismissNotification(notification);
            }
            @Override
            public void onError(Exception e) {
                Toast.makeText(NotificationActivity.this, "Failed to accept request", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void dismissNotification(Notification notification) {
        // Find position for optimistic removal
        int position = -1;
        for (int i = 0; i < adapter.getItemCount(); i++) {
            if (adapter.getNotificationAt(i).getId().equals(notification.getId())) {
                position = i;
                break;
            }
        }
        
        if (position != -1) {
            adapter.removeNotificationAt(position);
            emptyState.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        }

        notificationRepository.deleteNotification(notification, new DataCallback<>() {
            @Override
            public void onSuccess(Void result) {}
            @Override
            public void onError(Exception e) {
                loadNotifications();
            }
        });
    }

    private void executorFetchPaper(String paperId) {
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            com.bravem.app.model.PastPaper paper = com.bravem.app.data.local.AppDatabase.getInstance(this).paperDao().getById(paperId);
            if (paper != null) {
                runOnUiThread(() -> {
                    Intent intent = new Intent(this, PaperViewerActivity.class);
                    intent.putExtra(PaperViewerActivity.EXTRA_PAPER, paper);
                    startActivity(intent);
                });
            }
        });
    }
}
