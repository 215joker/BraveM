package com.bravem.app.data;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bravem.app.data.local.AppDatabase;
import com.bravem.app.data.local.UserDao;
import com.bravem.app.model.User;
import com.bravem.app.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());

    public AuthRepository(Context context) {
        this.userDao = AppDatabase.getInstance(context).userDao();
        this.sessionManager = new SessionManager(context);
        this.firebaseAuth = FirebaseAuth.getInstance();
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
        executor.execute(() -> {
            User existingUser = userDao.getByEmail(email);
            mainHandler.post(() -> callback.onSuccess(existingUser != null));
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
                            // First user logic or admin check based on email
                            String role = (email.contains("admin")) ? User.ROLE_ADMIN : User.ROLE_STUDENT;
                            String university = sessionManager.getUniversity();
                            
                            User newUser = new User(uid, fullName, email, password, university, degreeId, degreeName, intake, role, System.currentTimeMillis());
                            
                            // 1. Save to Local Room
                            userDao.insert(newUser);
                            
                            // 2. Push to Firebase Realtime Database
                            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
                            userRef.setValue(newUser).addOnCompleteListener(dbTask -> {
                                if (dbTask.isSuccessful()) {
                                    newUser.setSynced(true);
                                    executor.execute(() -> userDao.update(newUser));
                                }
                            });

                            // Save session
                            sessionManager.saveSession(uid, email, fullName, role, university, degreeId, degreeName, intake);
                            mainHandler.post(() -> callback.onSuccess(newUser));
                        });
                    } else {
                        callback.onError(task.getException());
                    }
                });
    }

    public void register(String fullName, String email, String password, DataCallback<User> callback) {
        register(fullName, email, password, null, null, null, callback);
    }

    public void login(String email, String password, DataCallback<User> callback) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().getUser() != null) {
                        String uid = task.getResult().getUser().getUid();
                        
                        fetchUserProfile(uid, new DataCallback<User>() {
                            @Override
                            public void onSuccess(User user) {
                                if (user.isSuspended()) {
                                    if (System.currentTimeMillis() > user.getSuspendedUntil()) {
                                        user.setSuspended(false);
                                        user.setSuspendedUntil(0);
                                        executor.execute(() -> userDao.update(user));
                                    } else {
                                        firebaseAuth.signOut();
                                        callback.onError(new Exception("Account suspended until " + new java.util.Date(user.getSuspendedUntil())));
                                        return;
                                    }
                                }
                                if (user.isDeletionRequested()) {
                                    firebaseAuth.signOut();
                                    callback.onError(new Exception("Account is marked for deletion."));
                                    return;
                                }
                                sessionManager.saveSession(user.getUid(), user.getEmail(), user.getFullName(), user.getRole(),
                                        user.getUniversity(), user.getDegreeId(), user.getDegreeName(), user.getIntake());
                                callback.onSuccess(user);
                            }

                            @Override
                            public void onError(Exception e) {
                                // If not found locally, try fetching from Realtime Database
                                fetchFromRealtimeDB(uid, callback);
                            }
                        });
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
        FirebaseDatabase.getInstance().getReference("users").child(uid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        User user = task.getResult().getValue(User.class);
                        if (user != null) {
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

    public void deleteUserPermanently(User user, DataCallback<Void> callback) {
        executor.execute(() -> {
            userDao.delete(user);
            mainHandler.post(() -> callback.onSuccess(null));
        });
    }

    public void markUserForDeletion(User user, DataCallback<Void> callback) {
        executor.execute(() -> {
            user.setDeletionRequested(true);
            user.setDeletionRequestedAt(System.currentTimeMillis());
            userDao.update(user);
            mainHandler.post(() -> callback.onSuccess(null));
        });
    }

    public void suspendUser(User user, long durationMillis, DataCallback<Void> callback) {
        executor.execute(() -> {
            user.setSuspended(true);
            user.setSuspendedUntil(System.currentTimeMillis() + durationMillis);
            userDao.update(user);
            mainHandler.post(() -> callback.onSuccess(null));
        });
    }

    public void restoreUser(User user, DataCallback<Void> callback) {
        executor.execute(() -> {
            user.setDeletionRequested(false);
            user.setDeletionRequestedAt(0);
            user.setSuspended(false);
            user.setSuspendedUntil(0);
            userDao.update(user);
            mainHandler.post(() -> callback.onSuccess(null));
        });
    }

    public void fetchCurrentUserProfile(DataCallback<User> callback) {
        String uid = getCurrentUid();
        if (uid == null) {
            callback.onError(new Exception("No session"));
            return;
        }
        fetchUserProfile(uid, callback);
    }

    public void fetchUserProfile(String uid, DataCallback<User> callback) {
        executor.execute(() -> {
            User user = userDao.getByUid(uid);
            if (user != null) {
                mainHandler.post(() -> callback.onSuccess(user));
            } else {
                mainHandler.post(() -> callback.onError(new Exception("User not found")));
            }
        });
    }

    public void updateUserProfile(String uid, String fullName, String profilePicture, DataCallback<Void> callback) {
        executor.execute(() -> {
            User user = userDao.getByUid(uid);
            if (user != null) {
                user.setFullName(fullName);
                user.setProfilePicture(profilePicture);
                userDao.update(user);
                sessionManager.saveSession(user.getUid(), user.getEmail(), user.getFullName(), user.getRole(),
                        user.getUniversity(), user.getDegreeId(), user.getDegreeName(), user.getIntake());
                mainHandler.post(() -> callback.onSuccess(null));
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
                userDao.update(user);
                sessionManager.updateDegree(degreeId, degreeName, intake);
                mainHandler.post(() -> callback.onSuccess(null));
            } else {
                mainHandler.post(() -> callback.onError(new Exception("User not found")));
            }
        });
    }

    public void updateUserIntake(String uid, String intake, DataCallback<Void> callback) {
        executor.execute(() -> {
            User user = userDao.getByUid(uid);
            if (user != null) {
                user.setIntake(intake);
                userDao.update(user);
                sessionManager.updateIntake(intake);
                mainHandler.post(() -> callback.onSuccess(null));
            } else {
                mainHandler.post(() -> callback.onError(new Exception("User not found")));
            }
        });
    }

    public void fetchAllUsers(DataCallback<java.util.List<User>> callback) {
        executor.execute(() -> {
            java.util.List<User> users = userDao.getAll();
            mainHandler.post(() -> callback.onSuccess(users));
        });
    }

    public void updateUserRole(String uid, String role, DataCallback<Void> callback) {
        executor.execute(() -> {
            userDao.updateRole(uid, role);
            mainHandler.post(() -> callback.onSuccess(null));
        });
    }

    public void deleteUser(User user, DataCallback<Void> callback) {
        executor.execute(() -> {
            userDao.delete(user);
            mainHandler.post(() -> callback.onSuccess(null));
        });
    }

    public void requestAccountDeletion(String uid, DataCallback<Void> callback) {
        executor.execute(() -> {
            User user = userDao.getByUid(uid);
            if (user != null) {
                user.setDeletionRequested(true);
                user.setDeletionRequestedAt(System.currentTimeMillis());
                userDao.update(user);
                mainHandler.post(() -> callback.onSuccess(null));
            } else {
                mainHandler.post(() -> callback.onError(new Exception("User not found")));
            }
        });
    }
}
