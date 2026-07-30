package com.bravem.app.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.bravem.app.model.User;

@Dao
public interface UserDao {
    @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
    User getByUid(String uid);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User getByEmail(String email);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(User user);

    @Update
    void update(User user);

    @androidx.room.Delete
    void delete(User user);

    @Query("UPDATE users SET role = :role WHERE uid = :uid")
    void updateRole(String uid, String role);

    @Query("SELECT * FROM users")
    java.util.List<User> getAll();

    @Query("SELECT * FROM users WHERE isSynced = 0")
    java.util.List<User> getUnsynced();

    @Query("SELECT * FROM users WHERE (university LIKE '%' || :query || '%' OR fullName LIKE '%' || :query || '%') AND role = 'student' AND uid != :currentUserId")
    java.util.List<User> searchStudents(String query, String currentUserId);

    @Query("SELECT * FROM users WHERE university = :university AND role = 'student' AND uid != :currentUserId AND uid NOT IN (SELECT senderId FROM friendships WHERE receiverId = :currentUserId AND status = 'ACCEPTED') AND uid NOT IN (SELECT receiverId FROM friendships WHERE senderId = :currentUserId AND status = 'ACCEPTED') ORDER BY fullName ASC")
    java.util.List<User> getRecommendations(String university, String currentUserId);

    @Query("SELECT * FROM users WHERE degreeName = :degreeName AND role = 'student' AND uid != :currentUserId AND uid NOT IN (SELECT senderId FROM friendships WHERE receiverId = :currentUserId AND status = 'ACCEPTED') AND uid NOT IN (SELECT receiverId FROM friendships WHERE senderId = :currentUserId AND status = 'ACCEPTED') ORDER BY fullName ASC")
    java.util.List<User> getRecommendationsByDegree(String degreeName, String currentUserId);
}
