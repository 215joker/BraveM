package com.bravem.app.data;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bravem.app.data.local.AppDatabase;
import com.bravem.app.data.local.UserDao;
import com.bravem.app.model.User;
import com.bravem.app.utils.SessionManager;

import java.util.Objects;
import java.util.UUID;

/**
 * Replaces Firebase Authentication and Firestore /users with local Room storage.
 */
public class AuthRepository {

    private final UserDao userDao;
    private final SessionManager sessionManager;

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
        User existingUser = userDao.getByEmail(email);
        callback.onSuccess(existingUser != null);
    }

    public void register(String fullName, String email, String password, 
                         String degreeId, String degreeName, String intake, 
                         DataCallback<User> callback) {
        User existingUser = userDao.getByEmail(email);
        if (existingUser != null) {
            callback.onError(new Exception("User already exists"));
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
        sessionManager.saveSession(uid, fullName, role, university, degreeId, degreeName, intake);
        
        callback.onSuccess(newUser);
    }

    public void register(String fullName, String email, String password, DataCallback<User> callback) {
        register(fullName, email, password, null, null, null, callback);
    }

    public void login(String email, String password, DataCallback<User> callback) {
        User user = userDao.getByEmail(email);
        if (user != null && Objects.equals(user.getPassword(), password)) {
            sessionManager.saveSession(user.getUid(), user.getFullName(), user.getRole(),
                    user.getUniversity(), user.getDegreeId(), user.getDegreeName(), user.getIntake());
            callback.onSuccess(user);
        } else {
            callback.onError(new Exception("Invalid credentials"));
        }
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
        User user = userDao.getByUid(uid);
        if (user != null) {
            callback.onSuccess(user);
        } else {
            callback.onError(new Exception("User not found"));
        }
    }

    public void updateUserProfile(String uid, String fullName, String profilePicture, DataCallback<Void> callback) {
        User user = userDao.getByUid(uid);
        if (user != null) {
            user.setFullName(fullName);
            user.setProfilePicture(profilePicture);
            userDao.update(user);
            sessionManager.saveSession(user.getUid(), user.getFullName(), user.getRole(),
                    user.getUniversity(), user.getDegreeId(), user.getDegreeName(), user.getIntake());
            callback.onSuccess(null);
        } else {
            callback.onError(new Exception("User not found"));
        }
    }

    public void updateUserDegree(@NonNull String uid, @NonNull String degreeId, @NonNull String degreeName, String intake,
                                 DataCallback<Void> callback) {
        User user = userDao.getByUid(uid);
        if (user != null) {
            user.setDegreeId(degreeId);
            user.setDegreeName(degreeName);
            user.setIntake(intake);
            userDao.update(user);
            sessionManager.updateDegree(degreeId, degreeName, intake);
            callback.onSuccess(null);
        } else {
            callback.onError(new Exception("User not found"));
        }
    }
}
