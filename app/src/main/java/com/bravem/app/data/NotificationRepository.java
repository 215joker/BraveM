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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NotificationRepository {
    private final NotificationDao notificationDao;
    private final SessionManager sessionManager;
    private final DatabaseReference notificationsRef;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());

    public NotificationRepository(Context context) {
        this.notificationDao = AppDatabase.getInstance(context).notificationDao();
        this.sessionManager = new SessionManager(context);
        this.notificationsRef = FirebaseDatabase.getInstance().getReference("notifications");
    }

    public void addNotification(String title, String message, String type, String recipientId, String relatedId) {
        executor.execute(() -> {
            Notification notification = new Notification(
                    java.util.UUID.randomUUID().toString(),
                    title, message, type, recipientId, relatedId,
                    System.currentTimeMillis()
            );
            
            // 1. Save to Firebase
            notificationsRef.child(recipientId).child(notification.getId()).setValue(notification)
                    .addOnSuccessListener(aVoid -> {
                        executor.execute(() -> notificationDao.insert(notification));
                    });
        });
    }

    public void fetchAllNotifications(DataCallback<List<Notification>> callback) {
        String userId = sessionManager.getUid();
        syncNotificationsFromFirebase(userId, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                executor.execute(() -> {
                    List<Notification> notifications = notificationDao.getAll(userId);
                    mainHandler.post(() -> callback.onSuccess(notifications));
                });
            }

            @Override
            public void onError(Exception e) {
                // Fallback to local if sync fails
                executor.execute(() -> {
                    List<Notification> notifications = notificationDao.getAll(userId);
                    mainHandler.post(() -> callback.onSuccess(notifications));
                });
            }
        });
    }

    private void syncNotificationsFromFirebase(String userId, DataCallback<Void> callback) {
        notificationsRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                executor.execute(() -> {
                    // Clear local notifs that are no longer in Firebase for this user
                    notificationDao.deleteAll(userId);

                    for (DataSnapshot notifSnapshot : snapshot.getChildren()) {
                        Notification notification = notifSnapshot.getValue(Notification.class);
                        if (notification != null) {
                            notificationDao.insert(notification);
                        }
                    }
                    mainHandler.post(() -> callback.onSuccess(null));
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                mainHandler.post(() -> callback.onError(error.toException()));
            }
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
        // Optimistically delete locally first
        executor.execute(() -> {
            notificationDao.delete(notification);
            mainHandler.post(() -> {
                if (callback != null) callback.onSuccess(null);
            });
            
            // Then delete from Firebase in the background
            String userId = sessionManager.getUid();
            notificationsRef.child(userId).child(notification.getId()).removeValue();
        });
    }
}
