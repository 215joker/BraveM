package com.bravem.app.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.bravem.app.data.local.AppDatabase;
import com.bravem.app.data.local.ChatMessageDao;
import com.bravem.app.domain.model.UserMapper;
import com.bravem.app.model.ChatMessage;
import com.bravem.app.model.Group;
import com.bravem.app.model.User;
import com.bravem.app.utils.SessionManager;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatRepository {

    private final ChatMessageDao chatMessageDao;
    private final com.bravem.app.data.local.UserDao userDao;
    private final com.bravem.app.data.local.FriendshipDao friendshipDao;
    private final com.bravem.app.data.local.GroupDao groupDao;
    private final NotificationRepository notificationRepository;
    private final SessionManager sessionManager;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public ChatRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        chatMessageDao = db.chatMessageDao();
        userDao = db.userDao();
        friendshipDao = db.friendshipDao();
        groupDao = db.groupDao();
        notificationRepository = new NotificationRepository(context);
        sessionManager = new SessionManager(context);
    }

    public void startListening(String otherUserId, DataCallback<List<ChatMessage>> callback) {
        // In local-only mode, we just fetch the history once or periodically
        getChatHistory(otherUserId, callback);
    }

    public void stopListening() {
        // No-op in local mode
    }

    public void getChatHistory(String otherUserId, DataCallback<List<ChatMessage>> callback) {
        String currentUserId = sessionManager.getUid();
        if (currentUserId == null || otherUserId == null) {
            callback.onError(new Exception("User not authenticated or receiver invalid"));
            return;
        }
        executor.execute(() -> {
            try {
                List<ChatMessage> history = chatMessageDao.getChatHistory(currentUserId, otherUserId);
                chatMessageDao.markAsRead(otherUserId, currentUserId);
                mainHandler.post(() -> callback.onSuccess(history));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    public void sendMessage(String receiverId, String message, String attachmentPath, String attachmentType, DataCallback<ChatMessage> callback) {
        String senderId = sessionManager.getUid();
        if (senderId == null || receiverId == null) {
            callback.onError(new Exception("User not authenticated or receiver invalid"));
            return;
        }
        String senderName = sessionManager.getFullName();
        executor.execute(() -> {
            try {
                String messageId = UUID.randomUUID().toString();
                ChatMessage chatMessage = new ChatMessage(senderId, receiverId, message, System.currentTimeMillis());
                chatMessage.setId(messageId);
                chatMessage.setAttachmentPath(attachmentPath);
                chatMessage.setAttachmentType(attachmentType);
                
                chatMessageDao.insert(chatMessage);
                
                // Update friendship updatedAt for sorting
                com.bravem.app.model.Friendship friendship = friendshipDao.getFriendship(senderId, receiverId);
                if (friendship != null) {
                    friendship.setUpdatedAt(System.currentTimeMillis());
                    friendshipDao.update(friendship);
                }

                // In local mode, we don't notify the "remote" receiver, but we could simulate it
                mainHandler.post(() -> callback.onSuccess(chatMessage));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    public void getTotalUnreadCount(DataCallback<Integer> callback) {
        getUnreadChatCount(callback);
    }

    public void getUnreadChatCount(DataCallback<Integer> callback) {
        String currentUserId = sessionManager.getUid();
        executor.execute(() -> {
            try {
                int count = chatMessageDao.getUnreadCount(currentUserId);
                mainHandler.post(() -> callback.onSuccess(count));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    public void startListeningToGroup(String groupId, DataCallback<List<ChatMessage>> callback) {
        executor.execute(() -> {
            List<ChatMessage> history = chatMessageDao.getGroupChatHistory(groupId);
            chatMessageDao.markGroupAsRead(groupId);
            mainHandler.post(() -> callback.onSuccess(history));
        });
    }

    public void createGroup(String name, String courseId, List<String> memberIds, DataCallback<Group> callback) {
        String currentUserId = sessionManager.getUid();
        String groupId = UUID.randomUUID().toString();

        Group group = new Group(groupId, name, currentUserId, courseId, System.currentTimeMillis());
        executor.execute(() -> {
            groupDao.insert(group);
            // In local mode, we'd also need to store members somewhere if needed
            mainHandler.post(() -> callback.onSuccess(group));
        });
    }

    public void getGroups(DataCallback<List<Group>> callback) {
        executor.execute(() -> {
            List<Group> groups = groupDao.getAll();
            mainHandler.post(() -> callback.onSuccess(groups));
        });
    }

    public void sendGroupMessage(String groupId, String message, String attachmentPath, String attachmentType, DataCallback<ChatMessage> callback) {
        String senderId = sessionManager.getUid();
        String messageId = UUID.randomUUID().toString();

        ChatMessage chatMessage = new ChatMessage(senderId, null, message, System.currentTimeMillis());
        chatMessage.setId(messageId);
        chatMessage.setGroupId(groupId);
        chatMessage.setGroup(true);
        chatMessage.setAttachmentPath(attachmentPath);
        chatMessage.setAttachmentType(attachmentType);

        executor.execute(() -> {
            chatMessageDao.insert(chatMessage);
            Group group = groupDao.getById(groupId);
            if (group != null) {
                group.setLastMessage(message);
                group.setLastMessageTimestamp(System.currentTimeMillis());
                groupDao.update(group);
            }
            mainHandler.post(() -> callback.onSuccess(chatMessage));
        });
    }

    public void getRecentChats(DataCallback<List<RecentChat>> callback) {
        String currentUserId = sessionManager.getUid();
        executor.execute(() -> {
            try {
                List<ChatMessage> lastMessages = chatMessageDao.getRecentChats(currentUserId);
                List<RecentChat> recentChats = new java.util.ArrayList<>();

                for (ChatMessage msg : lastMessages) {
                    String otherUserId = msg.getSenderId().equals(currentUserId) ? msg.getReceiverId() : msg.getSenderId();
                    User dataUser = userDao.getByUid(otherUserId);
                    int unreadCount = chatMessageDao.getUnreadCountFromUser(otherUserId, currentUserId);
                    
                    if (dataUser != null) {
                        recentChats.add(new RecentChat(UserMapper.toDomain(dataUser), msg, unreadCount));
                    }
                }

                mainHandler.post(() -> callback.onSuccess(recentChats));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    public void markAsRead(String senderId) {
        String currentUserId = sessionManager.getUid();
        executor.execute(() -> chatMessageDao.markAsRead(senderId, currentUserId));
    }

    public static class RecentChat {
        public final com.bravem.app.domain.model.User user;
        public final ChatMessage lastMessage;
        public final int unreadCount;

        public RecentChat(com.bravem.app.domain.model.User user, ChatMessage lastMessage, int unreadCount) {
            this.user = user;
            this.lastMessage = lastMessage;
            this.unreadCount = unreadCount;
        }
    }
}
