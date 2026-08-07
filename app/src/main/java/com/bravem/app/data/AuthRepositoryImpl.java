package com.bravem.app.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.bravem.app.data.local.AppDatabase;
import com.bravem.app.data.local.source.UserLocalDataSource;
import com.bravem.app.domain.model.UserMapper;
import com.bravem.app.domain.repository.UserRepository;
import com.bravem.app.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuthRepositoryImpl implements UserRepository {

    private final UserLocalDataSource localDataSource;
    private final SessionManager sessionManager;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public AuthRepositoryImpl(Context context) {
        this.localDataSource = new UserLocalDataSource(AppDatabase.getInstance(context).userDao());
        this.sessionManager = new SessionManager(context);
    }

    @Override
    public boolean isUserLoggedIn() {
        return sessionManager.getUid() != null;
    }

    @Override
    public void login(String email, String password, DataCallback<com.bravem.app.domain.model.User> callback) {
        executor.execute(() -> {
            com.bravem.app.model.User user = localDataSource.getUserByEmail(email);
            if (user != null && user.getPassword() != null && user.getPassword().equals(password)) {
                if (user.isSuspended() && user.getSuspendedUntil() > System.currentTimeMillis()) {
                    mainHandler.post(() -> callback.onError(new Exception("Account suspended until " + new java.util.Date(user.getSuspendedUntil()))));
                    return;
                }
                saveUserLocallyAndNotify(user, callback);
            } else {
                mainHandler.post(() -> callback.onError(new Exception("Invalid email or password")));
            }
        });
    }

    @Override
    public void register(String fullName, String email, String password, DataCallback<com.bravem.app.domain.model.User> callback) {
        register(fullName, email, password, "", "", "", callback);
    }

    @Override
    public void register(String fullName, String email, String password, String degreeId, String degreeName, String intake, DataCallback<com.bravem.app.domain.model.User> callback) {
        executor.execute(() -> {
            if (localDataSource.getUserByEmail(email) != null) {
                mainHandler.post(() -> callback.onError(new Exception("User already exists")));
                return;
            }

            String uid = UUID.randomUUID().toString();
            String role = (email.contains("admin")) ? com.bravem.app.model.User.ROLE_ADMIN : com.bravem.app.model.User.ROLE_STUDENT;
            String university = sessionManager.getUniversity();

            com.bravem.app.model.User newUser = new com.bravem.app.model.User(uid, fullName, email, password, university, degreeId, degreeName, intake, role, System.currentTimeMillis());
            newUser.setSynced(true); // Always synced in local mode
            
            localDataSource.saveUser(newUser);
            sessionManager.saveSession(uid, email, fullName, role, university, degreeId, degreeName, intake);
            
            mainHandler.post(() -> callback.onSuccess(UserMapper.toDomain(newUser)));
        });
    }

    @Override
    public void getCurrentUser(DataCallback<com.bravem.app.domain.model.User> callback) {
        String uid = sessionManager.getUid();
        if (uid == null) {
            callback.onError(new Exception("No active session"));
            return;
        }

        executor.execute(() -> {
            com.bravem.app.model.User localUser = localDataSource.getUser(uid);
            if (localUser != null) {
                mainHandler.post(() -> callback.onSuccess(UserMapper.toDomain(localUser)));
            } else {
                mainHandler.post(() -> callback.onError(new Exception("User not found")));
            }
        });
    }

    @Override
    public void logout() {
        sessionManager.clear();
    }

    @Override
    public void checkUserExists(String email, DataCallback<Boolean> callback) {
        executor.execute(() -> {
            com.bravem.app.model.User user = localDataSource.getUserByEmail(email);
            mainHandler.post(() -> callback.onSuccess(user != null));
        });
    }

    @Override
    public void forgotPassword(String email, DataCallback<Void> callback) {
        // In local mode, we might just "reset" the password to a default or show it
        mainHandler.post(() -> callback.onSuccess(null));
    }

    @Override
    public void updateProfile(String uid, String name, String intake, com.bravem.app.domain.model.Degree degree, DataCallback<Void> callback) {
        executor.execute(() -> {
            com.bravem.app.model.User user = localDataSource.getUser(uid);
            if (user != null) {
                user.setFullName(name);
                user.setIntake(intake);
                if (degree != null) {
                    user.setDegreeId(degree.getId());
                    user.setDegreeName(degree.getName());
                }
                localDataSource.saveUser(user);
                sessionManager.saveSession(user.getUid(), user.getEmail(), user.getFullName(), user.getRole(),
                        user.getUniversity(), user.getDegreeId(), user.getDegreeName(), user.getIntake());
                mainHandler.post(() -> callback.onSuccess(null));
            } else {
                mainHandler.post(() -> callback.onError(new Exception("User not found")));
            }
        });
    }

    @Override
    public void updateUserDegree(String uid, String degreeId, String degreeName, String intake, DataCallback<Void> callback) {
        executor.execute(() -> {
            com.bravem.app.model.User user = localDataSource.getUser(uid);
            if (user != null) {
                user.setDegreeId(degreeId);
                user.setDegreeName(degreeName);
                user.setIntake(intake);
                localDataSource.saveUser(user);
                sessionManager.updateDegree(degreeId, degreeName, intake);
                mainHandler.post(() -> callback.onSuccess(null));
            } else {
                mainHandler.post(() -> callback.onError(new Exception("User not found")));
            }
        });
    }

    @Override
    public void updateProfilePicture(String uid, String localPath, DataCallback<Void> callback) {
        executor.execute(() -> {
            com.bravem.app.model.User user = localDataSource.getUser(uid);
            if (user != null) {
                user.setProfilePicture(localPath);
                localDataSource.saveUser(user);
                mainHandler.post(() -> callback.onSuccess(null));
            } else {
                mainHandler.post(() -> callback.onError(new Exception("User not found")));
            }
        });
    }

    @Override
    public void requestAccountDeletion(String uid, DataCallback<Void> callback) {
        executor.execute(() -> {
            com.bravem.app.model.User user = localDataSource.getUser(uid);
            if (user != null) {
                user.setDeletionRequested(true);
                user.setDeletionRequestedAt(System.currentTimeMillis());
                localDataSource.saveUser(user);
                mainHandler.post(() -> callback.onSuccess(null));
            }
        });
    }

    @Override
    public void fetchAllUsers(DataCallback<List<com.bravem.app.domain.model.User>> callback) {
        executor.execute(() -> {
            List<com.bravem.app.model.User> users = localDataSource.getAllUsers();
            List<com.bravem.app.domain.model.User> domainUsers = new ArrayList<>();
            for (com.bravem.app.model.User u : users) {
                domainUsers.add(UserMapper.toDomain(u));
            }
            mainHandler.post(() -> callback.onSuccess(domainUsers));
        });
    }

    @Override
    public void updateUserRole(String uid, String role, DataCallback<Void> callback) {
        executor.execute(() -> {
            com.bravem.app.model.User user = localDataSource.getUser(uid);
            if (user != null) {
                user.setRole(role);
                localDataSource.saveUser(user);
                mainHandler.post(() -> callback.onSuccess(null));
            }
        });
    }

    @Override
    public void suspendUser(com.bravem.app.domain.model.User domainUser, long durationMillis, DataCallback<Void> callback) {
        executor.execute(() -> {
            com.bravem.app.model.User dataUser = localDataSource.getUser(domainUser.getUid());
            if (dataUser != null) {
                dataUser.setSuspended(true);
                dataUser.setSuspendedUntil(System.currentTimeMillis() + durationMillis);
                localDataSource.saveUser(dataUser);
                mainHandler.post(() -> callback.onSuccess(null));
            }
        });
    }

    @Override
    public void markUserForDeletion(com.bravem.app.domain.model.User domainUser, DataCallback<Void> callback) {
        executor.execute(() -> {
            com.bravem.app.model.User dataUser = localDataSource.getUser(domainUser.getUid());
            if (dataUser != null) {
                dataUser.setDeletionRequested(true);
                dataUser.setDeletionRequestedAt(System.currentTimeMillis());
                localDataSource.saveUser(dataUser);
                mainHandler.post(() -> callback.onSuccess(null));
            }
        });
    }

    @Override
    public void restoreUser(com.bravem.app.domain.model.User domainUser, DataCallback<Void> callback) {
        executor.execute(() -> {
            com.bravem.app.model.User dataUser = localDataSource.getUser(domainUser.getUid());
            if (dataUser != null) {
                dataUser.setSuspended(false);
                dataUser.setSuspendedUntil(0);
                dataUser.setDeletionRequested(false);
                dataUser.setDeletionRequestedAt(0);
                localDataSource.saveUser(dataUser);
                mainHandler.post(() -> callback.onSuccess(null));
            }
        });
    }

    @Override
    public void deleteUserPermanently(com.bravem.app.domain.model.User domainUser, DataCallback<Void> callback) {
        executor.execute(() -> {
            com.bravem.app.model.User dataUser = localDataSource.getUser(domainUser.getUid());
            if (dataUser != null) {
                localDataSource.deleteUser(dataUser);
                mainHandler.post(() -> callback.onSuccess(null));
            }
        });
    }

    private void saveUserLocallyAndNotify(com.bravem.app.model.User user, DataCallback<com.bravem.app.domain.model.User> callback) {
        executor.execute(() -> {
            if (user.getRole() == null) {
                user.setRole(com.bravem.app.model.User.ROLE_STUDENT);
            }
            localDataSource.saveUser(user);
            sessionManager.saveSession(user.getUid(), user.getEmail(), user.getFullName(), user.getRole(),
                    user.getUniversity(), user.getDegreeId(), user.getDegreeName(), user.getIntake());
            if (callback != null) {
                mainHandler.post(() -> callback.onSuccess(UserMapper.toDomain(user)));
            }
        });
    }
}
