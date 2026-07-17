package com.bravem.app.ui.dashboard;

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
import com.bravem.app.data.PaperRepository;
import com.bravem.app.model.Notification;
import com.bravem.app.model.PastPaper;
import com.bravem.app.ui.papers.PaperViewerActivity;
import com.bravem.app.utils.UiUtils;

import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private View emptyState;
    private NotificationRepository notificationRepository;
    private PaperRepository paperRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiUtils.applyEdgeToEdge(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        Toolbar toolbar = findViewById(R.id.toolbar);
        UiUtils.handleTopInset(toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        UiUtils.handleBottomInset(findViewById(R.id.recycler_notifications));

        notificationRepository = new NotificationRepository(this);
        paperRepository = new PaperRepository(this);

        recyclerView = findViewById(R.id.recycler_notifications);
        emptyState = findViewById(R.id.empty_state);

        adapter = new NotificationAdapter(this::handleNotificationClick);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        new androidx.recyclerview.widget.ItemTouchHelper(new androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(0, androidx.recyclerview.widget.ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                Notification notification = adapter.getNotificationAt(position);
                notificationRepository.deleteNotification(notification, new DataCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        loadNotifications();
                    }
                    @Override
                    public void onError(Exception e) {
                        adapter.notifyItemChanged(position);
                    }
                });
            }
        }).attachToRecyclerView(recyclerView);

        loadNotifications();
    }

    private void loadNotifications() {
        notificationRepository.fetchAllNotifications(new DataCallback<List<Notification>>() {
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
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle(notification.getTitle())
                    .setMessage(notification.getMessage())
                    .setPositiveButton(R.string.accept, (dialog, which) -> {
                        acceptFriendRequest(notification);
                    })
                    .setNegativeButton(R.string.cancel, (dialog, which) -> {
                        dismissNotification(notification);
                    })
                    .show();
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
                }
            }
        }
    }

    private void fetchUserAndOpenChat(String userId) {
        new com.bravem.app.data.AuthRepository(this).fetchUserProfile(userId, new DataCallback<com.bravem.app.model.User>() {
            @Override
            public void onSuccess(com.bravem.app.model.User user) {
                Intent intent = new Intent(NotificationActivity.this, com.bravem.app.ui.community.ChatActivity.class);
                intent.putExtra(com.bravem.app.ui.community.ChatActivity.EXTRA_USER, user);
                startActivity(intent);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(NotificationActivity.this, "Failed to open chat", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void acceptFriendRequest(Notification notification) {
        com.bravem.app.data.CommunityRepository communityRepository = new com.bravem.app.data.CommunityRepository(this);
        communityRepository.acceptFriendRequest(notification.getRelatedId(), new DataCallback<Boolean>() {
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
        notificationRepository.deleteNotification(notification, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loadNotifications();
            }
            @Override
            public void onError(Exception e) {}
        });
    }

    private void executorFetchPaper(String paperId) {
        // Simple executor or use repository if it had getById
        // Since PaperDao has getById, let's use a temporary executor here or add to repo
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
