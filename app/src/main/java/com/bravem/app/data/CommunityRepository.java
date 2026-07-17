package com.bravem.app.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.bravem.app.data.local.AppDatabase;
import com.bravem.app.data.local.FriendshipDao;
import com.bravem.app.data.local.UserDao;
import com.bravem.app.model.Friendship;
import com.bravem.app.model.Notification;
import com.bravem.app.model.User;
import com.bravem.app.utils.SessionManager;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommunityRepository {

    private final UserDao userDao;
    private final FriendshipDao friendshipDao;
    private final NotificationRepository notificationRepository;
    private final SessionManager sessionManager;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public CommunityRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        userDao = db.userDao();
        friendshipDao = db.friendshipDao();
        notificationRepository = new NotificationRepository(context);
        sessionManager = new SessionManager(context);
    }

    public void getFriends(DataCallback<List<User>> callback) {
        String currentUserId = sessionManager.getUid();
        executor.execute(() -> {
            try {
                List<User> friends = friendshipDao.getFriends(currentUserId);
                mainHandler.post(() -> callback.onSuccess(friends));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    public void getRecommendations(DataCallback<List<User>> callback) {
        String university = sessionManager.getUniversity();
        String degreeId = sessionManager.getDegreeId();
        String currentUserId = sessionManager.getUid();

        executor.execute(() -> {
            try {
                List<User> users = userDao.getRecommendations(university, degreeId, currentUserId);
                mainHandler.post(() -> callback.onSuccess(users));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    public void searchStudents(String query, DataCallback<List<User>> callback) {
        String currentUserId = sessionManager.getUid();
        executor.execute(() -> {
            try {
                List<User> users = userDao.searchStudents(query, currentUserId);
                mainHandler.post(() -> callback.onSuccess(users));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    public void sendFriendRequest(String targetUserId, DataCallback<Boolean> callback) {
        String currentUserId = sessionManager.getUid();
        String currentUserName = sessionManager.getFullName();
        executor.execute(() -> {
            try {
                Friendship existing = friendshipDao.getFriendship(currentUserId, targetUserId);
                if (existing == null) {
                    Friendship friendship = new Friendship(currentUserId, targetUserId, Friendship.STATUS_PENDING, System.currentTimeMillis());
                    friendshipDao.insert(friendship);

                    // Notification for receiver
                    notificationRepository.addNotification(
                            "New Friend Request",
                            currentUserName + " sent you a friend request.",
                            "friend_request",
                            targetUserId,
                            currentUserId // relatedId is senderId
                    );

                    mainHandler.post(() -> callback.onSuccess(true));
                } else {
                    mainHandler.post(() -> callback.onSuccess(false)); // Already friends or pending
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    public void acceptFriendRequest(String senderId, DataCallback<Boolean> callback) {
        String currentUserId = sessionManager.getUid();
        String currentUserName = sessionManager.getFullName();
        executor.execute(() -> {
            try {
                Friendship friendship = friendshipDao.getFriendship(senderId, currentUserId);
                if (friendship != null && friendship.getStatus().equals(Friendship.STATUS_PENDING)) {
                    friendship.setStatus(Friendship.STATUS_ACCEPTED);
                    friendship.setUpdatedAt(System.currentTimeMillis());
                    friendshipDao.update(friendship);

                    // Notification for sender
                    notificationRepository.addNotification(
                            "Friend Request Accepted",
                            currentUserName + " accepted your friend request.",
                            "friend_accepted",
                            senderId,
                            currentUserId // relatedId is acceptorId
                    );

                    mainHandler.post(() -> callback.onSuccess(true));
                } else {
                    mainHandler.post(() -> callback.onSuccess(false));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }
}
