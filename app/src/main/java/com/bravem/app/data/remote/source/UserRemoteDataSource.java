package com.bravem.app.data.remote.source;

import com.bravem.app.data.DataCallback;
import com.bravem.app.model.User;

public interface UserRemoteDataSource {
    void login(String email, String password, DataCallback<User> callback);
    void register(String fullName, String email, String password, String university, DataCallback<User> callback);
    void fetchUser(String uid, DataCallback<User> callback);
    void updateProfile(User user, DataCallback<Void> callback);
    void logout();
    void sendPasswordReset(String email, DataCallback<Void> callback);
    void requestDeletion(String uid, DataCallback<Void> callback);
}
