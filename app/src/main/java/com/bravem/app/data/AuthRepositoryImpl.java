package com.bravem.app.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.bravem.app.data.local.AppDatabase;
import com.bravem.app.data.local.source.UserLocalDataSource;
import com.bravem.app.data.remote.source.FirebaseUserRemoteDataSource;
import com.bravem.app.data.remote.source.UserRemoteDataSource;
import com.bravem.app.domain.model.UserMapper;
import com.bravem.app.domain.repository.UserRepository;
import com.bravem.app.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuthRepositoryImpl implements UserRepository {

    private final UserRemoteDataSource remoteDataSource;
    private final UserLocalDataSource localDataSource;
    private final SessionManager sessionManager;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    
    private final FirebaseAuth firebaseAuth;
    private final DatabaseReference usersRef;

    public AuthRepositoryImpl(Context context) {
        this.remoteDataSource = new FirebaseUserRemoteDataSource();
        this.localDataSource = new UserLocalDataSource(AppDatabase.getInstance(context).userDao());
        this.sessionManager = new SessionManager(context);
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.usersRef = FirebaseDatabase.getInstance().getReference("users");
    }

    @Override
    public boolean isUserLoggedIn() {
        return sessionManager.getUid() != null;
    }

    @Override
    public void login(String email, String password, DataCallback<com.bravem.app.domain.model.User> callback) {
        remoteDataSource.login(email, password, new DataCallback<com.bravem.app.model.User>() {
            @Override
            public void onSuccess(com.bravem.app.model.User user) {
                saveUserLocallyAndNotify(user, callback);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(new Exception(ErrorMapper.map(e)));
            }
        });
    }

    @Override
    public void register(String fullName, String email, String password, DataCallback<com.bravem.app.domain.model.User> callback) {
        register(fullName, email, password, "", "", "", callback);
    }

    @Override
    public void register(String fullName, String email, String password, String degreeId, String degreeName, String intake, DataCallback<com.bravem.app.domain.model.User> callback) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().getUser() != null) {
                        String uid = task.getResult().getUser().getUid();
                        executor.execute(() -> {
                            String role = (email.contains("admin")) ? com.bravem.app.model.User.ROLE_ADMIN : com.bravem.app.model.User.ROLE_STUDENT;
                            String university = sessionManager.getUniversity();
                            
                            com.bravem.app.model.User newUser = new com.bravem.app.model.User(uid, fullName, email, password, university, degreeId, degreeName, intake, role, System.currentTimeMillis());
                            
                            localDataSource.saveUser(newUser);
                            
                            usersRef.child(uid).setValue(newUser).addOnCompleteListener(dbTask -> {
                                if (dbTask.isSuccessful()) {
                                    newUser.setSynced(true);
                                    executor.execute(() -> localDataSource.saveUser(newUser));
                                }
                            });

                            sessionManager.saveSession(uid, email, fullName, role, university, degreeId, degreeName, intake);
                            mainHandler.post(() -> callback.onSuccess(UserMapper.toDomain(newUser)));
                        });
                    } else {
                        callback.onError(new Exception(ErrorMapper.map(task.getException())));
                    }
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
            }
            
            remoteDataSource.fetchUser(uid, new DataCallback<com.bravem.app.model.User>() {
                @Override
                public void onSuccess(com.bravem.app.model.User user) {
                    saveUserLocallyAndNotify(user, null);
                }
                @Override
                public void onError(Exception e) {}
            });
        });
    }

    @Override
    public void logout() {
        remoteDataSource.logout();
        executor.execute(() -> {
            String uid = sessionManager.getUid();
            if (uid != null) {
                com.bravem.app.model.User user = localDataSource.getUser(uid);
                if (user != null) localDataSource.deleteUser(user);
            }
            sessionManager.clear();
        });
    }

    @Override
    public void checkUserExists(String email, DataCallback<Boolean> callback) {
        usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {
                callback.onSuccess(snapshot.exists());
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {
                callback.onError(error.toException());
            }
        });
    }

    @Override
    public void forgotPassword(String email, DataCallback<Void> callback) {
        remoteDataSource.sendPasswordReset(email, callback);
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
                remoteDataSource.updateProfile(user, new DataCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        executor.execute(() -> {
                            localDataSource.saveUser(user);
                            sessionManager.saveSession(user.getUid(), user.getEmail(), user.getFullName(), user.getRole(),
                                    user.getUniversity(), user.getDegreeId(), user.getDegreeName(), user.getIntake());
                            mainHandler.post(() -> callback.onSuccess(null));
                        });
                    }
                    @Override
                    public void onError(Exception e) {
                        mainHandler.post(() -> callback.onError(e));
                    }
                });
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
                
                usersRef.child(uid).setValue(user).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        executor.execute(() -> {
                            localDataSource.saveUser(user);
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

    @Override
    public void updateProfilePicture(String uid, String localPath, DataCallback<Void> callback) {
        executor.execute(() -> {
            com.bravem.app.model.User user = localDataSource.getUser(uid);
            if (user != null) {
                user.setProfilePicture(localPath);
                remoteDataSource.updateProfile(user, new DataCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        executor.execute(() -> {
                            localDataSource.saveUser(user);
                            mainHandler.post(() -> callback.onSuccess(null));
                        });
                    }
                    @Override
                    public void onError(Exception e) {
                        mainHandler.post(() -> callback.onError(e));
                    }
                });
            }
        });
    }

    @Override
    public void requestAccountDeletion(String uid, DataCallback<Void> callback) {
        remoteDataSource.requestDeletion(uid, callback);
    }

    @Override
    public void fetchAllUsers(DataCallback<List<com.bravem.app.domain.model.User>> callback) {
        usersRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                List<com.bravem.app.domain.model.User> domainUsers = new ArrayList<>();
                for (com.google.firebase.database.DataSnapshot snapshot : task.getResult().getChildren()) {
                    com.bravem.app.model.User dataUser = snapshot.getValue(com.bravem.app.model.User.class);
                    if (dataUser != null) {
                        domainUsers.add(UserMapper.toDomain(dataUser));
                    }
                }
                callback.onSuccess(domainUsers);
            } else {
                callback.onError(task.getException() != null ? task.getException() : new Exception("Failed to fetch users"));
            }
        });
    }

    @Override
    public void updateUserRole(String uid, String role, DataCallback<Void> callback) {
        usersRef.child(uid).child("role").setValue(role).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                executor.execute(() -> {
                    com.bravem.app.model.User user = localDataSource.getUser(uid);
                    if (user != null) {
                        user.setRole(role);
                        localDataSource.saveUser(user);
                    }
                    mainHandler.post(() -> callback.onSuccess(null));
                });
            } else {
                callback.onError(task.getException());
            }
        });
    }

    @Override
    public void suspendUser(com.bravem.app.domain.model.User domainUser, long durationMillis, DataCallback<Void> callback) {
        executor.execute(() -> {
            com.bravem.app.model.User dataUser = localDataSource.getUser(domainUser.getUid());
            if (dataUser != null) {
                long suspendedUntil = System.currentTimeMillis() + durationMillis;
                dataUser.setSuspended(true);
                dataUser.setSuspendedUntil(suspendedUntil);
                
                usersRef.child(dataUser.getUid()).setValue(dataUser).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        executor.execute(() -> {
                            localDataSource.saveUser(dataUser);
                            mainHandler.post(() -> callback.onSuccess(null));
                        });
                    } else {
                        mainHandler.post(() -> callback.onError(task.getException()));
                    }
                });
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
                
                usersRef.child(dataUser.getUid()).setValue(dataUser).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        executor.execute(() -> {
                            localDataSource.saveUser(dataUser);
                            mainHandler.post(() -> callback.onSuccess(null));
                        });
                    } else {
                        mainHandler.post(() -> callback.onError(task.getException()));
                    }
                });
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
                
                usersRef.child(dataUser.getUid()).setValue(dataUser).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        executor.execute(() -> {
                            localDataSource.saveUser(dataUser);
                            mainHandler.post(() -> callback.onSuccess(null));
                        });
                    } else {
                        mainHandler.post(() -> callback.onError(task.getException()));
                    }
                });
            }
        });
    }

    @Override
    public void deleteUserPermanently(com.bravem.app.domain.model.User domainUser, DataCallback<Void> callback) {
        executor.execute(() -> {
            com.bravem.app.model.User dataUser = localDataSource.getUser(domainUser.getUid());
            usersRef.child(domainUser.getUid()).removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    executor.execute(() -> {
                        if (dataUser != null) localDataSource.deleteUser(dataUser);
                        if (callback != null) mainHandler.post(() -> callback.onSuccess(null));
                    });
                } else {
                    if (callback != null) mainHandler.post(() -> callback.onError(task.getException()));
                }
            });
        });
    }

    private void saveUserLocallyAndNotify(com.bravem.app.model.User user, DataCallback<com.bravem.app.domain.model.User> callback) {
        executor.execute(() -> {
            localDataSource.saveUser(user);
            sessionManager.saveSession(user.getUid(), user.getEmail(), user.getFullName(), user.getRole(),
                    user.getUniversity(), user.getDegreeId(), user.getDegreeName(), user.getIntake());
            if (callback != null) {
                mainHandler.post(() -> callback.onSuccess(UserMapper.toDomain(user)));
            }
        });
    }
}
