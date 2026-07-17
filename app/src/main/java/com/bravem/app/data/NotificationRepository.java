package com.bravem.app.data;

import android.content.Context;
import com.bravem.app.data.local.AppDatabase;
import com.bravem.app.data.local.NotificationDao;
import com.bravem.app.model.Notification;
import com.bravem.app.utils.SessionManager;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationRepository {
    private final NotificationDao notificationDao;
    private final SessionManager sessionManager;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());

    public NotificationRepository(Context context) {
        this.notificationDao = AppDatabase.getInstance(context).notificationDao();
        this.sessionManager = new SessionManager(context);
    }

    public void addNotification(String title, String message, String type, String recipientId, String relatedId) {
        executor.execute(() -> {
            // Check for redundancy
            List<Notification> existing = notificationDao.getAll(recipientId);
            for (Notification n : existing) {
                if (n.getTitle().equals(title) && n.getMessage().equals(message)) {
                    return; // Avoid repeated notifications
                }
            }

            Notification notification = new Notification(
                    java.util.UUID.randomUUID().toString(),
                    title, message, type, recipientId, relatedId,
                    System.currentTimeMillis()
            );
            notificationDao.insert(notification);
        });
    }

    public void fetchAllNotifications(DataCallback<List<Notification>> callback) {
        String userId = sessionManager.getUid();
        executor.execute(() -> {
            List<Notification> notifications = notificationDao.getAll(userId);
            mainHandler.post(() -> callback.onSuccess(notifications));
        });
    }

    public void getUnreadCount(DataCallback<Integer> callback) {
        String userId = sessionManager.getUid();
        executor.execute(() -> {
            int count = notificationDao.getUnreadCount(userId);
            mainHandler.post(() -> callback.onSuccess(count));
        });
    }

    public void markAsRead(Notification notification) {
        executor.execute(() -> {
            notification.setRead(true);
            notificationDao.update(notification);
        });
    }

    public void markAllAsRead(DataCallback<Void> callback) {
        String userId = sessionManager.getUid();
        executor.execute(() -> {
            notificationDao.markAllAsRead(userId);
            if (callback != null) mainHandler.post(() -> callback.onSuccess(null));
        });
    }

    public void deleteNotification(Notification notification, DataCallback<Void> callback) {
        executor.execute(() -> {
            notificationDao.delete(notification);
            if (callback != null) mainHandler.post(() -> callback.onSuccess(null));
        });
    }
}
