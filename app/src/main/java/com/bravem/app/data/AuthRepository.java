package com.bravem.app.data;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bravem.app.data.local.AppDatabase;
import com.bravem.app.data.local.UserDao;
import com.bravem.app.model.User;
import com.bravem.app.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository handling Authentication using Firebase Auth and syncing with Local Room + Realtime DB.
 */
public class AuthRepository {

    private final UserDao userDao;
    private final SessionManager sessionManager;
    private final FirebaseAuth firebaseAuth;
    private final DatabaseReference usersRef;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());

    public AuthRepository(Context context) {
        this.userDao = AppDatabase.getInstance(context).userDao();
        this.sessionManager = new SessionManager(context);
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.usersRef = FirebaseDatabase.getInstance().getReference("users");
    }

    public boolean isLoggedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }

    public String getCurrentUid() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        return user != null ? user.getUid() : sessionManager.getUid();
    }

    public void logout() {
        firebaseAuth.signOut();
        sessionManager.clear();
    }

    public void checkUserExists(String email, DataCallback<Boolean> callback) {
        usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                callback.onSuccess(snapshot.exists());
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                callback.onError(error.toException());
            }
        });
    }

    public void register(String fullName, String email, String password, 
                         String degreeId, String degreeName, String intake, 
                         DataCallback<User> callback) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().getUser() != null) {
                        String uid = task.getResult().getUser().getUid();
                        executor.execute(() -> {
                            String role = (email.contains("admin")) ? User.ROLE_ADMIN : User.ROLE_STUDENT;
                            String university = sessionManager.getUniversity();
                            
                            User newUser = new User(uid, fullName, email, password, university, degreeId, degreeName, intake, role, System.currentTimeMillis());
                            
                            userDao.insert(newUser);
                            
                            usersRef.child(uid).setValue(newUser).addOnCompleteListener(dbTask -> {
                                if (dbTask.isSuccessful()) {
                                    newUser.setSynced(true);
                                    executor.execute(() -> userDao.update(newUser));
                                }
                            });

                            sessionManager.saveSession(uid, email, fullName, role, university, degreeId, degreeName, intake);
                            mainHandler.post(() -> callback.onSuccess(newUser));
                        });
                    } else {
                        callback.onError(task.getException());
                    }
                });
    }

    public void login(String email, String password, DataCallback<User> callback) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().getUser() != null) {
                        String uid = task.getResult().getUser().getUid();
                        fetchFromRealtimeDB(uid, callback);
                    } else {
                        callback.onError(task.getException());
                    }
                });
    }

    public void forgotPassword(String email, DataCallback<Void> callback) {
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(null);
                    } else {
                        callback.onError(task.getException());
                    }
                });
    }

    private void fetchFromRealtimeDB(String uid, DataCallback<User> callback) {
        usersRef.child(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                User user = task.getResult().getValue(User.class);
                if (user != null) {
                    if (user.isSuspended()) {
                        if (System.currentTimeMillis() > user.getSuspendedUntil()) {
                            user.setSuspended(false);
                            user.setSuspendedUntil(0);
                        } else {
                            firebaseAuth.signOut();
                            callback.onError(new Exception("Account suspended"));
                            return;
                        }
                    }
                    if (user.isDeletionRequested()) {
                        firebaseAuth.signOut();
                        callback.onError(new Exception("Account marked for deletion"));
                        return;
                    }
                    executor.execute(() -> userDao.insert(user));
                    sessionManager.saveSession(user.getUid(), user.getEmail(), user.getFullName(), user.getRole(),
                            user.getUniversity(), user.getDegreeId(), user.getDegreeName(), user.getIntake());
                    callback.onSuccess(user);
                } else {
                    callback.onError(new Exception("User data corrupted"));
                }
            } else {
                callback.onError(new Exception("User profile not found in cloud"));
            }
        });
    }

    public void updateUserProfile(String uid, String fullName, String profilePicture, DataCallback<Void> callback) {
        executor.execute(() -> {
            User user = userDao.getByUid(uid);
            if (user != null) {
                user.setFullName(fullName);
                user.setProfilePicture(profilePicture);
                
                usersRef.child(uid).setValue(user).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        executor.execute(() -> {
                            userDao.update(user);
                            sessionManager.saveSession(user.getUid(), user.getEmail(), user.getFullName(), user.getRole(),
                                    user.getUniversity(), user.getDegreeId(), user.getDegreeName(), user.getIntake());
                            mainHandler.post(() -> callback.onSuccess(null));
                        });
                    } else {
                        mainHandler.post(() -> callback.onError(task.getException()));
                    }
                });
            } else {
                mainHandler.post(() -> callback.onError(new Exception("User not found")));
            }
        });
    }

    public void updateUserDegree(@NonNull String uid, @NonNull String degreeId, @NonNull String degreeName, String intake,
                                 DataCallback<Void> callback) {
        executor.execute(() -> {
            User user = userDao.getByUid(uid);
            if (user != null) {
                user.setDegreeId(degreeId);
                user.setDegreeName(degreeName);
                user.setIntake(intake);
                
                usersRef.child(uid).setValue(user).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        executor.execute(() -> {
                            userDao.update(user);
                            sessionManager.updateDegree(degreeId, degreeName, intake);
                            mainHandler.post(() -> callback.onSuccess(null));
                        });
                    } else {
                        mainHandler.post(() -> callback.onError(task.getException()));
                    }
                });
            } else {
                mainHandler.post(() -> callback.onError(new Exception("User not found")));
            }
        });
    }

    public void fetchUserProfile(String uid, DataCallback<User> callback) {
        executor.execute(() -> {
            User user = userDao.getByUid(uid);
            if (user != null) {
                mainHandler.post(() -> callback.onSuccess(user));
            } else {
                // If not found locally, try Firebase
                usersRef.child(uid).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        User remoteUser = task.getResult().getValue(User.class);
                        if (remoteUser != null) {
                            executor.execute(() -> userDao.insert(remoteUser));
                            mainHandler.post(() -> callback.onSuccess(remoteUser));
                        } else {
                            mainHandler.post(() -> callback.onError(new Exception("User not found")));
                        }
                    } else {
                        mainHandler.post(() -> callback.onError(new Exception("User not found")));
                    }
                });
            }
        });
    }

    public void fetchAllUsers(DataCallback<List<User>> callback) {
        usersRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                List<User> users = new ArrayList<>();
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        users.add(user);
                    }
                }
                callback.onSuccess(users);
            } else {
                callback.onError(task.getException() != null ? task.getException() : new Exception("Failed to fetch users"));
            }
        });
    }

    // Other methods... (truncated for brevity but they should follow similar pattern)
    
    public void fetchCurrentUserProfile(DataCallback<User> callback) {
        String uid = getCurrentUid();
        if (uid != null) {
            fetchUserProfile(uid, callback);
        } else {
            callback.onError(new Exception("User not logged in"));
        }
    }

    public void updateUserRole(String uid, String role, DataCallback<Void> callback) {
        usersRef.child(uid).child("role").setValue(role).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                executor.execute(() -> {
                    userDao.updateRole(uid, role);
                    mainHandler.post(() -> callback.onSuccess(null));
                });
            } else {
                callback.onError(task.getException());
            }
        });
    }

    public void suspendUser(User user, long durationMillis, DataCallback<Void> callback) {
        long suspendedUntil = System.currentTimeMillis() + durationMillis;
        user.setSuspended(true);
        user.setSuspendedUntil(suspendedUntil);
        usersRef.child(user.getUid()).setValue(user).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                executor.execute(() -> {
                    userDao.update(user);
                    mainHandler.post(() -> callback.onSuccess(null));
                });
            } else {
                callback.onError(task.getException());
            }
        });
    }

    public void markUserForDeletion(User user, DataCallback<Void> callback) {
        user.setDeletionRequested(true);
        user.setDeletionRequestedAt(System.currentTimeMillis());
        usersRef.child(user.getUid()).setValue(user).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                executor.execute(() -> {
                    userDao.update(user);
                    mainHandler.post(() -> callback.onSuccess(null));
                });
            } else {
                callback.onError(task.getException());
            }
        });
    }

    public void restoreUser(User user, DataCallback<Void> callback) {
        user.setSuspended(false);
        user.setSuspendedUntil(0);
        user.setDeletionRequested(false);
        user.setDeletionRequestedAt(0);
        usersRef.child(user.getUid()).setValue(user).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                executor.execute(() -> {
                    userDao.update(user);
                    mainHandler.post(() -> callback.onSuccess(null));
                });
            } else {
                callback.onError(task.getException());
            }
        });
    }

    public void deleteUserPermanently(User user, DataCallback<Void> callback) {
        usersRef.child(user.getUid()).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                executor.execute(() -> {
                    userDao.delete(user);
                    mainHandler.post(() -> callback.onSuccess(null));
                });
            } else {
                callback.onError(task.getException());
            }
        });
    }

    public void requestAccountDeletion(String uid, DataCallback<Void> callback) {
        usersRef.child(uid).child("deletionRequested").setValue(true).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                usersRef.child(uid).child("deletionRequestedAt").setValue(System.currentTimeMillis());
                executor.execute(() -> {
                    User user = userDao.getByUid(uid);
                    if (user != null) {
                        user.setDeletionRequested(true);
                        user.setDeletionRequestedAt(System.currentTimeMillis());
                        userDao.update(user);
                    }
                    mainHandler.post(() -> callback.onSuccess(null));
                });
            } else {
                callback.onError(task.getException());
            }
        });
    }

    public void register(String fullName, String email, String password, DataCallback<User> callback) {
        register(fullName, email, password, "", "", "", callback);
    }
}
