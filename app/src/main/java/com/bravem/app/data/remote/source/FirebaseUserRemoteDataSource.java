package com.bravem.app.data.remote.source;

import com.bravem.app.data.DataCallback;
import com.bravem.app.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseUserRemoteDataSource implements UserRemoteDataSource {

    private final FirebaseAuth auth;
    private final DatabaseReference usersRef;

    public FirebaseUserRemoteDataSource() {
        this.auth = FirebaseAuth.getInstance();
        this.usersRef = FirebaseDatabase.getInstance().getReference("users");
    }

    @Override
    public void login(String email, String password, DataCallback<User> callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().getUser() != null) {
                        fetchUser(task.getResult().getUser().getUid(), callback);
                    } else {
                        callback.onError(task.getException());
                    }
                });
    }

    @Override
    public void register(String fullName, String email, String password, String university, DataCallback<User> callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().getUser() != null) {
                        String uid = task.getResult().getUser().getUid();
                        User newUser = new User(uid, fullName, email, password, university, null, null, null, User.ROLE_STUDENT, System.currentTimeMillis());
                        usersRef.child(uid).setValue(newUser).addOnCompleteListener(dbTask -> {
                            if (dbTask.isSuccessful()) {
                                callback.onSuccess(newUser);
                            } else {
                                callback.onError(dbTask.getException());
                            }
                        });
                    } else {
                        callback.onError(task.getException());
                    }
                });
    }

    @Override
    public void fetchUser(String uid, DataCallback<User> callback) {
        usersRef.child(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                User user = task.getResult().getValue(User.class);
                if (user != null) {
                    callback.onSuccess(user);
                } else {
                    callback.onError(new Exception("User data null"));
                }
            } else {
                callback.onError(task.getException() != null ? task.getException() : new Exception("User not found"));
            }
        });
    }

    @Override
    public void updateProfile(User user, DataCallback<Void> callback) {
        usersRef.child(user.getUid()).setValue(user).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess(null);
            } else {
                callback.onError(task.getException());
            }
        });
    }

    @Override
    public void logout() {
        auth.signOut();
    }

    @Override
    public void sendPasswordReset(String email, DataCallback<Void> callback) {
        auth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess(null);
            } else {
                callback.onError(task.getException());
            }
        });
    }

    @Override
    public void requestDeletion(String uid, DataCallback<Void> callback) {
        usersRef.child(uid).child("deletionRequested").setValue(true).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                usersRef.child(uid).child("deletionRequestedAt").setValue(System.currentTimeMillis());
                callback.onSuccess(null);
            } else {
                callback.onError(task.getException());
            }
        });
    }
}
