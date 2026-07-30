package com.bravem.app.domain.repository;

import com.bravem.app.data.DataCallback;
import com.bravem.app.domain.model.Degree;
import com.bravem.app.domain.model.User;

public interface UserRepository {
    void login(String email, String password, DataCallback<User> callback);
    void register(String fullName, String email, String password, DataCallback<User> callback);
    void getCurrentUser(DataCallback<User> callback);
    void logout();
    boolean isUserLoggedIn();
    void forgotPassword(String email, DataCallback<Void> callback);
    void updateProfile(String uid, String name, String intake, Degree degree, DataCallback<Void> callback);
    void updateProfilePicture(String uid, String localPath, DataCallback<Void> callback);
    void requestAccountDeletion(String uid, DataCallback<Void> callback);
    void checkUserExists(String email, DataCallback<Boolean> callback);
    void register(String fullName, String email, String password, String degreeId, String degreeName, String intake, DataCallback<com.bravem.app.domain.model.User> callback);
    void updateUserDegree(String uid, String degreeId, String degreeName, String intake, DataCallback<Void> callback);
    void fetchAllUsers(DataCallback<java.util.List<com.bravem.app.domain.model.User>> callback);
    void updateUserRole(String uid, String role, DataCallback<Void> callback);
    void suspendUser(com.bravem.app.domain.model.User user, long durationMillis, DataCallback<Void> callback);
    void markUserForDeletion(com.bravem.app.domain.model.User user, DataCallback<Void> callback);
    void restoreUser(com.bravem.app.domain.model.User user, DataCallback<Void> callback);
    void deleteUserPermanently(com.bravem.app.domain.model.User user, DataCallback<Void> callback);
}
