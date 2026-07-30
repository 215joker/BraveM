package com.bravem.app.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.bravem.app.model.ChatMessage;

import java.util.List;

@Dao
public interface ChatMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ChatMessage message);

    @Query("SELECT * FROM chat_messages WHERE (senderId = :userId1 AND receiverId = :userId2) OR (senderId = :userId2 AND receiverId = :userId1) ORDER BY timestamp ASC")
    List<ChatMessage> getChatHistory(String userId1, String userId2);

    @Query("SELECT COUNT(*) FROM chat_messages WHERE receiverId = :userId AND isRead = 0")
    int getUnreadCount(String userId);

    @Query("SELECT COUNT(*) FROM (SELECT DISTINCT senderId FROM chat_messages WHERE receiverId = :userId AND isRead = 0)")
    int getUnreadChatCount(String userId);

    @Query("UPDATE chat_messages SET isRead = 1 WHERE senderId = :senderId AND receiverId = :receiverId")
    void markAsRead(String senderId, String receiverId);

    @Query("SELECT * FROM chat_messages WHERE id IN (SELECT MAX(id) FROM chat_messages WHERE senderId = :userId OR receiverId = :userId GROUP BY CASE WHEN senderId = :userId THEN receiverId ELSE senderId END) ORDER BY timestamp ASC")
    List<ChatMessage> getRecentChats(String userId);

    @Query("SELECT COUNT(*) FROM chat_messages WHERE senderId = :senderId AND receiverId = :receiverId AND isRead = 0")
    int getUnreadCountFromUser(String senderId, String receiverId);

    @Query("SELECT * FROM chat_messages WHERE groupId = :groupId ORDER BY timestamp ASC")
    List<ChatMessage> getGroupChatHistory(String groupId);

    @Query("UPDATE chat_messages SET isRead = 1 WHERE groupId = :groupId")
    void markGroupAsRead(String groupId);
}
