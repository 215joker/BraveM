package com.bravem.app.data.local;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.bravem.app.model.Notification;
import java.util.List;

@Dao
public interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE recipientId = :userId ORDER BY timestamp DESC")
    List<Notification> getAll(String userId);

    @Query("SELECT COUNT(*) FROM notifications WHERE recipientId = :userId AND isRead = 0")
    int getUnreadCount(String userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Notification notification);

    @Update
    void update(Notification notification);

    @Query("UPDATE notifications SET isRead = 1 WHERE recipientId = :userId")
    void markAllAsRead(String userId);

    @Delete
    void delete(Notification notification);
}
