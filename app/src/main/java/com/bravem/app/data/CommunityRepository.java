package com.bravem.app.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.bravem.app.data.local.AppDatabase;
import com.bravem.app.data.local.FriendshipDao;
import com.bravem.app.data.local.UserDao;
import com.bravem.app.domain.model.UserMapper;
import com.bravem.app.model.Friendship;
import com.bravem.app.model.User;
import com.bravem.app.utils.SessionManager;

import java.util.ArrayList;
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

    public void getFriends(DataCallback<List<com.bravem.app.domain.model.User>> callback) {
        String currentUserId = sessionManager.getUid();
        executor.execute(() -> {
            try {
                List<User> friends = friendshipDao.getFriends(currentUserId);
                List<com.bravem.app.domain.model.User> domainFriends = new ArrayList<>();
                for (User u : friends) domainFriends.add(UserMapper.toDomain(u));
                mainHandler.post(() -> callback.onSuccess(domainFriends));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    public void getRecommendations(boolean crossUniversity, DataCallback<List<com.bravem.app.domain.model.User>> callback) {
        String university = sessionManager.getUniversity();
        String degreeName = sessionManager.getDegreeName();
        String currentUserId = sessionManager.getUid();

        executor.execute(() -> {
            try {
                List<User> users;
                if (!crossUniversity) {
                    users = userDao.getRecommendations(university, currentUserId);
                } else {
                    users = userDao.getRecommendationsByDegree(degreeName, currentUserId);
                }
                List<com.bravem.app.domain.model.User> domainUsers = new ArrayList<>();
                for (User u : users) domainUsers.add(UserMapper.toDomain(u));
                mainHandler.post(() -> callback.onSuccess(domainUsers));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    public void searchStudents(String query, DataCallback<List<com.bravem.app.domain.model.User>> callback) {
        String currentUserId = sessionManager.getUid();
        executor.execute(() -> {
            try {
                List<User> users = userDao.searchStudents(query, currentUserId);
                List<com.bravem.app.domain.model.User> domainUsers = new ArrayList<>();
                for (User u : users) domainUsers.add(UserMapper.toDomain(u));
                mainHandler.post(() -> callback.onSuccess(domainUsers));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    public void sendFriendRequest(String targetUserId, DataCallback<Boolean> callback) {
        String currentUserId = sessionManager.getUid();
        if (currentUserId == null || targetUserId == null || currentUserId.equals(targetUserId)) {
            callback.onSuccess(false);
            return;
        }
        String currentUserName = sessionManager.getFullName();
        executor.execute(() -> {
            Friendship existing = friendshipDao.getFriendship(currentUserId, targetUserId);
            if (existing == null) {
                Friendship friendship = new Friendship(currentUserId, targetUserId, Friendship.STATUS_PENDING, System.currentTimeMillis());
                friendshipDao.insert(friendship);
                notificationRepository.addNotification(
                        "New Friend Request",
                        currentUserName + " sent you a friend request.",
                        "friend_request",
                        targetUserId,
                        currentUserId
                );
                mainHandler.post(() -> callback.onSuccess(true));
            } else {
                mainHandler.post(() -> callback.onSuccess(false));
            }
        });
    }

    public void acceptFriendRequest(String senderId, DataCallback<Boolean> callback) {
        String currentUserId = sessionManager.getUid();
        String currentUserName = sessionManager.getFullName();
        executor.execute(() -> {
            Friendship friendship = friendshipDao.getFriendship(senderId, currentUserId);
            if (friendship != null && Friendship.STATUS_PENDING.equals(friendship.getStatus())) {
                if (friendship.getReceiverId().equals(currentUserId)) {
                    friendship.setStatus(Friendship.STATUS_ACCEPTED);
                    friendship.setUpdatedAt(System.currentTimeMillis());
                    friendshipDao.update(friendship);
                    notificationRepository.addNotification(
                            "Friend Request Accepted",
                            currentUserName + " accepted your friend request.",
                            "friend_accepted",
                            senderId,
                            currentUserId
                    );
                    mainHandler.post(() -> callback.onSuccess(true));
                } else {
                    mainHandler.post(() -> callback.onSuccess(false));
                }
            } else {
                mainHandler.post(() -> callback.onSuccess(false));
            }
        });
    }
}
