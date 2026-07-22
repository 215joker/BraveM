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
        syncUsersFromFirebase(new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
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

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    public void searchStudents(String query, DataCallback<List<User>> callback) {
        syncUsersFromFirebase(new DataCallback<Void>() {
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
        String currentUserName = sessionManager.getFullName();
        executor.execute(() -> {
            try {
                Friendship existing = friendshipDao.getFriendship(currentUserId, targetUserId);
                if (existing == null) {
                    Friendship friendship = new Friendship(currentUserId, targetUserId, Friendship.STATUS_PENDING, System.currentTimeMillis());
                    
                    // Save to Firebase
                    String friendshipId = getFriendshipId(currentUserId, targetUserId);
                    friendshipsRef.child(friendshipId).setValue(friendship).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            executor.execute(() -> {
                                friendshipDao.insert(friendship);
                                // Notification for receiver
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
                    mainHandler.post(() -> callback.onSuccess(false));
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

                    // Update in Firebase
                    String friendshipId = getFriendshipId(senderId, currentUserId);
                    friendshipsRef.child(friendshipId).setValue(friendship).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            executor.execute(() -> {
                                friendshipDao.update(friendship);
                                // Notification for sender
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
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    private String getFriendshipId(String u1, String u2) {
        return u1.compareTo(u2) < 0 ? u1 + "_" + u2 : u2 + "_" + u1;
    }

    private void syncUsersFromFirebase(DataCallback<Void> callback) {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
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
        friendshipsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                executor.execute(() -> {
                    for (DataSnapshot friendshipSnapshot : snapshot.getChildren()) {
                        Friendship friendship = friendshipSnapshot.getValue(Friendship.class);
                        if (friendship != null) {
                            friendshipDao.insert(friendship);
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
}
