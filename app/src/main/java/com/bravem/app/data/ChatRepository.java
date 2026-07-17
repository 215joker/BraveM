package com.bravem.app.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.bravem.app.data.local.AppDatabase;
import com.bravem.app.data.local.ChatMessageDao;
import com.bravem.app.model.ChatMessage;
import com.bravem.app.model.User;
import com.bravem.app.utils.SessionManager;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatRepository {

    private final Context context;
    private final ChatMessageDao chatMessageDao;
    private final com.bravem.app.data.local.UserDao userDao;
    private final com.bravem.app.data.local.FriendshipDao friendshipDao;
    private final NotificationRepository notificationRepository;
    private final SessionManager sessionManager;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public ChatRepository(Context context) {
        this.context = context.getApplicationContext();
        AppDatabase db = AppDatabase.getInstance(context);
        chatMessageDao = db.chatMessageDao();
        userDao = db.userDao();
        friendshipDao = db.friendshipDao();
        notificationRepository = new NotificationRepository(context);
        sessionManager = new SessionManager(context);
    }

    public void getChatHistory(String otherUserId, DataCallback<List<ChatMessage>> callback) {
        String currentUserId = sessionManager.getUid();
        executor.execute(() -> {
            try {
                List<ChatMessage> history = chatMessageDao.getChatHistory(currentUserId, otherUserId);
                chatMessageDao.markAsRead(otherUserId, currentUserId);
                
                // Update interaction time
                com.bravem.app.model.Friendship friendship = friendshipDao.getFriendship(currentUserId, otherUserId);
                if (friendship != null) {
                    friendship.setUpdatedAt(System.currentTimeMillis());
                    friendshipDao.update(friendship);
                }

                mainHandler.post(() -> callback.onSuccess(history));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    public void sendMessage(String receiverId, String message, String attachmentPath, String attachmentType, DataCallback<ChatMessage> callback) {
        String senderId = sessionManager.getUid();
        String senderName = sessionManager.getFullName();
        executor.execute(() -> {
            try {
                ChatMessage chatMessage = new ChatMessage(senderId, receiverId, message, System.currentTimeMillis());
                chatMessage.setAttachmentPath(attachmentPath);
                chatMessage.setAttachmentType(attachmentType);
                chatMessageDao.insert(chatMessage);

                // Update friendship updatedAt for sorting
                com.bravem.app.model.Friendship friendship = friendshipDao.getFriendship(senderId, receiverId);
                if (friendship != null) {
                    friendship.setUpdatedAt(System.currentTimeMillis());
                    friendshipDao.update(friendship);
                }

                // Simulate message notification for receiver
                notificationRepository.addNotification(
                        "New Message from " + senderName,
                        message != null ? message : "Sent an attachment",
                        "chat_message",
                        receiverId,
                        senderId // relatedId is senderId
                );

                // Show system notification (simulating for receiver)
                executor.execute(() -> {
                    com.bravem.app.model.User sender = userDao.getByUid(senderId);
                    if (sender != null) {
                        mainHandler.post(() -> {
                            android.content.Intent intent = new android.content.Intent(context, com.bravem.app.ui.community.ChatActivity.class);
                            intent.putExtra(com.bravem.app.ui.community.ChatActivity.EXTRA_USER, sender);
                            com.bravem.app.utils.NotificationHelper.showNotification(
                                    context,
                                    "New Message from " + senderName,
                                    message != null ? message : "Sent an attachment",
                                    intent
                            );
                        });
                    }
                });

                mainHandler.post(() -> callback.onSuccess(chatMessage));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    public void getTotalUnreadCount(DataCallback<Integer> callback) {
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

    public void getRecentChats(DataCallback<List<RecentChat>> callback) {
        String currentUserId = sessionManager.getUid();
        executor.execute(() -> {
            try {
                List<ChatMessage> lastMessages = chatMessageDao.getRecentChats(currentUserId);
                List<RecentChat> recentChats = new java.util.ArrayList<>();

                for (ChatMessage msg : lastMessages) {
                    String otherUserId = msg.getSenderId().equals(currentUserId) ? msg.getReceiverId() : msg.getSenderId();
                    User otherUser = userDao.getByUid(otherUserId);
                    int unreadCount = chatMessageDao.getUnreadCountFromUser(otherUserId, currentUserId);
                    
                    if (otherUser != null) {
                        recentChats.add(new RecentChat(otherUser, msg, unreadCount));
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
        public final User user;
        public final ChatMessage lastMessage;
        public final int unreadCount;

        public RecentChat(User user, ChatMessage lastMessage, int unreadCount) {
            this.user = user;
            this.lastMessage = lastMessage;
            this.unreadCount = unreadCount;
        }
    }
}
