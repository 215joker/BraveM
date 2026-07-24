package com.bravem.app.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.bravem.app.data.local.AppDatabase;
import com.bravem.app.data.local.FriendshipDao;
import com.bravem.app.data.local.UserDao;
import com.bravem.app.model.Friendship;
import com.bravem.app.model.User;
import com.bravem.app.utils.SessionManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommunityRepository {

    private final UserDao userDao;
    private final FriendshipDao friendshipDao;
    private final NotificationRepository notificationRepository;
    private final SessionManager sessionManager;
    private final DatabaseReference usersRef;
    private final DatabaseReference friendshipsRef;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public CommunityRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        userDao = db.userDao();
        friendshipDao = db.friendshipDao();
        notificationRepository = new NotificationRepository(context);
        sessionManager = new SessionManager(context);
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        friendshipsRef = FirebaseDatabase.getInstance().getReference("friendships");
    }

    public void getFriends(DataCallback<List<User>> callback) {
        syncFriendshipsFromFirebase(new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
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

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    public void getRecommendations(DataCallback<List<User>> callback) {
        syncUsersByUniversity(sessionManager.getUniversity(), new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                String university = sessionManager.getUniversity();
                String currentUserId = sessionManager.getUid();

                executor.execute(() -> {
                    try {
                        List<User> users = userDao.getRecommendations(university, currentUserId);
                        mainHandler.post(() -> callback.onSuccess(users));
                    } catch (Exception e) {
                        mainHandler.post(() -> callback.onError(e));
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    public void searchStudents(String query, DataCallback<List<User>> callback) {
        syncUsersByUniversity(sessionManager.getUniversity(), new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
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

            @Override
            public void onError(Exception e) {
                callback.onError(e);
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
        String friendshipId = Friendship.generateId(currentUserId, targetUserId);

        // Check Firebase directly to avoid duplicate requests if local sync is delayed
        friendshipsRef.child(friendshipId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Friendship friendship = new Friendship(currentUserId, targetUserId, Friendship.STATUS_PENDING, System.currentTimeMillis());
                    friendshipsRef.child(friendshipId).setValue(friendship).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            executor.execute(() -> {
                                friendshipDao.insert(friendship);
                                notificationRepository.addNotification(
                                        "New Friend Request",
                                        currentUserName + " sent you a friend request.",
                                        "friend_request",
                                        targetUserId,
                                        currentUserId
                                );
                                mainHandler.post(() -> callback.onSuccess(true));
                            });
                        } else {
                            mainHandler.post(() -> callback.onError(task.getException()));
                        }
                    });
                } else {
                    // Check if it's already accepted or pending from other side
                    Friendship existing = snapshot.getValue(Friendship.class);
                    if (existing != null && Friendship.STATUS_PENDING.equals(existing.getStatus()) 
                            && existing.getReceiverId().equals(currentUserId)) {
                        // If they sent us a request, just accept it? 
                        // For now, just return false as per current logic
                        mainHandler.post(() -> callback.onSuccess(false));
                    } else {
                        mainHandler.post(() -> callback.onSuccess(false));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                mainHandler.post(() -> callback.onError(error.toException()));
            }
        });
    }

    public void acceptFriendRequest(String senderId, DataCallback<Boolean> callback) {
        String currentUserId = sessionManager.getUid();
        String currentUserName = sessionManager.getFullName();
        String friendshipId = Friendship.generateId(senderId, currentUserId);

        friendshipsRef.child(friendshipId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Friendship friendship = snapshot.getValue(Friendship.class);
                if (friendship != null && Friendship.STATUS_PENDING.equals(friendship.getStatus())) {
                    if (friendship.getReceiverId().equals(currentUserId)) {
                        friendship.setStatus(Friendship.STATUS_ACCEPTED);
                        friendship.setUpdatedAt(System.currentTimeMillis());

                        friendshipsRef.child(friendshipId).setValue(friendship).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                executor.execute(() -> {
                                    friendshipDao.insert(friendship);
                                    notificationRepository.addNotification(
                                            "Friend Request Accepted",
                                            currentUserName + " accepted your friend request.",
                                            "friend_accepted",
                                            senderId,
                                            currentUserId
                                    );
                                    mainHandler.post(() -> callback.onSuccess(true));
                                });
                            } else {
                                mainHandler.post(() -> callback.onError(task.getException()));
                            }
                        });
                    } else {
                        mainHandler.post(() -> callback.onSuccess(false));
                    }
                } else {
                    mainHandler.post(() -> callback.onSuccess(false));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                mainHandler.post(() -> callback.onError(error.toException()));
            }
        });
    }

    private void syncSingleUser(String uid, Runnable onComplete) {
        usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    user.setSynced(true);
                    executor.execute(() -> {
                        userDao.insert(user);
                        if (onComplete != null) mainHandler.post(onComplete);
                    });
                } else {
                    if (onComplete != null) onComplete.run();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                if (onComplete != null) onComplete.run();
            }
        });
    }

    private void syncUsersByUniversity(String university, DataCallback<Void> callback) {
        if (university == null || university.isEmpty()) {
            callback.onSuccess(null);
            return;
        }
        usersRef.orderByChild("university").equalTo(university).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                executor.execute(() -> {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        User user = userSnapshot.getValue(User.class);
                        if (user != null) {
                            user.setSynced(true);
                            userDao.insert(user);
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

    private void syncFriendshipsFromFirebase(DataCallback<Void> callback) {
        String currentUserId = sessionManager.getUid();
        if (currentUserId == null) {
            callback.onSuccess(null);
            return;
        }

        friendshipsRef.orderByChild("senderId").equalTo(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                saveFriendships(snapshot, () -> {
                    friendshipsRef.orderByChild("receiverId").equalTo(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            saveFriendships(snapshot, () -> callback.onSuccess(null));
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            mainHandler.post(() -> callback.onError(error.toException()));
                        }
                    });
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                mainHandler.post(() -> callback.onError(error.toException()));
            }
        });
    }

    private void saveFriendships(DataSnapshot snapshot, Runnable onComplete) {
        String currentUserId = sessionManager.getUid();
        executor.execute(() -> {
            long childrenCount = snapshot.getChildrenCount();
            if (childrenCount == 0) {
                mainHandler.post(onComplete);
                return;
            }

            java.util.concurrent.atomic.AtomicInteger pending = new java.util.concurrent.atomic.AtomicInteger((int) childrenCount);
            for (DataSnapshot s : snapshot.getChildren()) {
                Friendship f = s.getValue(Friendship.class);
                if (f != null) {
                    friendshipDao.insert(f);
                    String otherUid = f.getSenderId().equals(currentUserId) ? f.getReceiverId() : f.getSenderId();
                    syncSingleUser(otherUid, () -> {
                        if (pending.decrementAndGet() == 0) {
                            onComplete.run();
                        }
                    });
                } else {
                    if (pending.decrementAndGet() == 0) {
                        onComplete.run();
                    }
                }
            }
        });
    }
}
