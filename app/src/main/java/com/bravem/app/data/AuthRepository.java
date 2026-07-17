package com.bravem.app.data;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bravem.app.data.local.AppDatabase;
import com.bravem.app.data.local.UserDao;
import com.bravem.app.model.User;
import com.bravem.app.utils.SessionManager;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Replaces Firebase Authentication and Firestore /users with local Room storage.
 */
public class AuthRepository {

    private final UserDao userDao;
    private final SessionManager sessionManager;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());

    public AuthRepository(Context context) {
        this.userDao = AppDatabase.getInstance(context).userDao();
        this.sessionManager = new SessionManager(context);
    }

    public boolean isLoggedIn() {
        return sessionManager.getUid() != null;
    }

    public String getCurrentUid() {
        return sessionManager.getUid();
    }

    public void logout() {
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
        executor.execute(() -> {
            User existingUser = userDao.getByEmail(email);
            if (existingUser != null) {
                mainHandler.post(() -> callback.onError(new Exception("User already exists")));
                return;
            }

            String uid = UUID.randomUUID().toString();
            // First user is admin for easier local testing
            String role = (userDao.getByUid("admin") == null && email.contains("admin"))
                    ? User.ROLE_ADMIN : User.ROLE_STUDENT;

            String university = sessionManager.getUniversity();
            User newUser = new User(uid, fullName, email, password, university, degreeId, degreeName, intake, role, System.currentTimeMillis());
            userDao.insert(newUser);
            
            // Save session immediately after registration
            sessionManager.saveSession(uid, email, fullName, role, university, degreeId, degreeName, intake);
            
            mainHandler.post(() -> callback.onSuccess(newUser));
        });
    }

    public void register(String fullName, String email, String password, DataCallback<User> callback) {
        register(fullName, email, password, null, null, null, callback);
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

    public void login(String email, String password, DataCallback<User> callback) {
        executor.execute(() -> {
            User user = userDao.getByEmail(email);
            if (user != null && Objects.equals(user.getPassword(), password)) {
                if (user.isSuspended()) {
                    if (System.currentTimeMillis() > user.getSuspendedUntil()) {
                        user.setSuspended(false);
                        user.setSuspendedUntil(0);
                        userDao.update(user);
                    } else {
                        mainHandler.post(() -> callback.onError(new Exception("Account suspended until " + new java.util.Date(user.getSuspendedUntil()))));
                        return;
                    }
                }
                if (user.isDeletionRequested()) {
                    mainHandler.post(() -> callback.onError(new Exception("Account is marked for deletion.")));
                    return;
                }
                sessionManager.saveSession(user.getUid(), user.getEmail(), user.getFullName(), user.getRole(),
                        user.getUniversity(), user.getDegreeId(), user.getDegreeName(), user.getIntake());
                mainHandler.post(() -> callback.onSuccess(user));
            } else {
                mainHandler.post(() -> callback.onError(new Exception("Invalid credentials")));
            }
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
