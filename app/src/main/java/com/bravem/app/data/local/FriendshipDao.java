package com.bravem.app.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.bravem.app.model.Friendship;
import com.bravem.app.model.User;

import java.util.List;

@Dao
public interface FriendshipDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Friendship friendship);

    @Update
    void update(Friendship friendship);

    @Query("SELECT * FROM friendships WHERE (senderId = :userId1 AND receiverId = :userId2) OR (senderId = :userId2 AND receiverId = :userId1) LIMIT 1")
    Friendship getFriendship(String userId1, String userId2);

    @Query("SELECT u.* FROM users u INNER JOIN friendships f ON (u.uid = f.senderId OR u.uid = f.receiverId) WHERE (f.senderId = :userId OR f.receiverId = :userId) AND u.uid != :userId AND f.status = 'ACCEPTED' ORDER BY f.updatedAt DESC")
    List<User> getFriends(String userId);

    @Query("SELECT COUNT(*) FROM friendships WHERE (senderId = :userId OR receiverId = :userId) AND status = 'PENDING' AND receiverId = :userId")
    int getPendingRequestCount(String userId);
}
