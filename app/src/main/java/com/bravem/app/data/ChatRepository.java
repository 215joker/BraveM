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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChatRepository {

    private final Context context;
    private final ChatMessageDao chatMessageDao;
    private final com.bravem.app.data.local.UserDao userDao;
    private final com.bravem.app.data.local.FriendshipDao friendshipDao;
    private final com.bravem.app.data.local.GroupDao groupDao;
    private final NotificationRepository notificationRepository;
    private final SessionManager sessionManager;
    private final DatabaseReference messagesRef;
    private final DatabaseReference groupsRef;
    private final DatabaseReference groupMembersRef;
    private final DatabaseReference groupMessagesRef;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    
    private ValueEventListener currentChatListener;
    private DatabaseReference currentChatRef;

    public ChatRepository(Context context) {
        this.context = context.getApplicationContext();
        AppDatabase db = AppDatabase.getInstance(context);
        chatMessageDao = db.chatMessageDao();
        userDao = db.userDao();
        friendshipDao = db.friendshipDao();
        groupDao = db.groupDao();
        notificationRepository = new NotificationRepository(context);
        sessionManager = new SessionManager(context);
        messagesRef = FirebaseDatabase.getInstance().getReference("messages");
        groupsRef = FirebaseDatabase.getInstance().getReference("groups");
        groupMembersRef = FirebaseDatabase.getInstance().getReference("groupMembers");
        groupMessagesRef = FirebaseDatabase.getInstance().getReference("groupMessages");
    }

    public void startListening(String otherUserId, DataCallback<List<ChatMessage>> callback) {
        stopListening();
        String currentUserId = sessionManager.getUid();
        String chatId = getChatId(currentUserId, otherUserId);
        
        currentChatRef = messagesRef.child(chatId);
        currentChatListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                executor.execute(() -> {
                    for (DataSnapshot msgSnapshot : snapshot.getChildren()) {
                        ChatMessage msg = msgSnapshot.getValue(ChatMessage.class);
                        if (msg != null) {
                            chatMessageDao.insert(msg);
                        }
                    }
                    List<ChatMessage> history = chatMessageDao.getChatHistory(currentUserId, otherUserId);
                    chatMessageDao.markAsRead(otherUserId, currentUserId);
                    mainHandler.post(() -> callback.onSuccess(history));
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                mainHandler.post(() -> callback.onError(error.toException()));
            }
        };
        currentChatRef.addValueEventListener(currentChatListener);
    }

    public void stopListening() {
        if (currentChatRef != null && currentChatListener != null) {
            currentChatRef.removeEventListener(currentChatListener);
            currentChatRef = null;
            currentChatListener = null;
        }
    }

    private String getChatId(String u1, String u2) {
        if (u1 == null || u2 == null) return "unknown_chat";
        return u1.compareTo(u2) < 0 ? u1 + "_" + u2 : u2 + "_" + u1;
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
                String messageId = messagesRef.push().getKey();
                if (messageId == null) {
                    mainHandler.post(() -> callback.onError(new Exception("Failed to generate message ID")));
                    return;
                }
                ChatMessage chatMessage = new ChatMessage(senderId, receiverId, message, System.currentTimeMillis());
                chatMessage.setId(messageId);
                chatMessage.setAttachmentPath(attachmentPath);
                chatMessage.setAttachmentType(attachmentType);
                
                String chatId = getChatId(senderId, receiverId);
                messagesRef.child(chatId).child(messageId).setValue(chatMessage).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        executor.execute(() -> {
                            chatMessageDao.insert(chatMessage);
                            // Update friendship updatedAt for sorting
                            com.bravem.app.model.Friendship friendship = friendshipDao.getFriendship(senderId, receiverId);
                            if (friendship != null) {
                                friendship.setUpdatedAt(System.currentTimeMillis());
                                friendshipDao.update(friendship);
                            }

                            // Notification for receiver
                            notificationRepository.addNotification(
                                    "New Message from " + senderName,
                                    message != null && !message.isEmpty() ? message : "Sent an attachment",
                                    "chat_message",
                                    receiverId,
                                    senderId
                            );
                            mainHandler.post(() -> callback.onSuccess(chatMessage));
                        });
                    } else {
                        mainHandler.post(() -> callback.onError(task.getException()));
                    }
                });
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

    public void startListeningToGroup(String groupId, DataCallback<List<ChatMessage>> callback) {
        stopListening();
        currentChatRef = groupMessagesRef.child(groupId);
        currentChatListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                executor.execute(() -> {
                    for (DataSnapshot msgSnapshot : snapshot.getChildren()) {
                        ChatMessage msg = msgSnapshot.getValue(ChatMessage.class);
                        if (msg != null) {
                            chatMessageDao.insert(msg);
                        }
                    }
                    List<ChatMessage> history = chatMessageDao.getGroupChatHistory(groupId);
                    chatMessageDao.markGroupAsRead(groupId);
                    mainHandler.post(() -> callback.onSuccess(history));
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                mainHandler.post(() -> callback.onError(error.toException()));
            }
        };
        currentChatRef.addValueEventListener(currentChatListener);
    }

    public void createGroup(String name, String courseId, List<String> memberIds, DataCallback<Group> callback) {
        String currentUserId = sessionManager.getUid();
        String groupId = groupsRef.push().getKey();
        if (groupId == null) {
            callback.onError(new Exception("Failed to generate group ID"));
            return;
        }

        Group group = new Group(groupId, name, currentUserId, courseId, System.currentTimeMillis());
        groupsRef.child(groupId).setValue(group).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Add members
                List<String> allMembers = new java.util.ArrayList<>(memberIds);
                if (!allMembers.contains(currentUserId)) allMembers.add(currentUserId);

                for (String memberId : allMembers) {
                    groupMembersRef.child(groupId).child(memberId).setValue(true);
                    FirebaseDatabase.getInstance().getReference("users").child(memberId).child("groups").child(groupId).setValue(true);
                }

                executor.execute(() -> {
                    groupDao.insert(group);
                    mainHandler.post(() -> callback.onSuccess(group));
                });
            } else {
                callback.onError(task.getException());
            }
        });
    }

    public void getGroups(DataCallback<List<Group>> callback) {
        String currentUserId = sessionManager.getUid();
        // This is a bit complex in Firebase because we need to find groups where the user is a member
        // Option 1: Store group IDs under users/uid/groups
        // Option 2: Query groupMembers (less efficient)
        // For simplicity, let's assume we have users/uid/groups
        FirebaseDatabase.getInstance().getReference("users").child(currentUserId).child("groups")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<String> groupIds = new java.util.ArrayList<>();
                        for (DataSnapshot s : snapshot.getChildren()) {
                            groupIds.add(s.getKey());
                        }
                        
                        if (groupIds.isEmpty()) {
                            callback.onSuccess(new java.util.ArrayList<>());
                            return;
                        }

                        List<Group> groups = new java.util.ArrayList<>();
                        java.util.concurrent.atomic.AtomicInteger count = new java.util.concurrent.atomic.AtomicInteger(groupIds.size());
                        for (String gid : groupIds) {
                            groupsRef.child(gid).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot snapshot) {
                                    Group g = snapshot.getValue(Group.class);
                                    if (g != null) groups.add(g);
                                    if (count.decrementAndGet() == 0) {
                                        executor.execute(() -> {
                                            for (Group group : groups) groupDao.insert(group);
                                            mainHandler.post(() -> callback.onSuccess(groups));
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError error) {}
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        callback.onError(error.toException());
                    }
                });
    }

    public void sendGroupMessage(String groupId, String message, String attachmentPath, String attachmentType, DataCallback<ChatMessage> callback) {
        String senderId = sessionManager.getUid();
        String senderName = sessionManager.getFullName();
        
        String messageId = groupMessagesRef.child(groupId).push().getKey();
        if (messageId == null) {
            callback.onError(new Exception("Failed to generate message ID"));
            return;
        }

        ChatMessage chatMessage = new ChatMessage(senderId, null, message, System.currentTimeMillis());
        chatMessage.setId(messageId);
        chatMessage.setGroupId(groupId);
        chatMessage.setGroup(true);
        chatMessage.setAttachmentPath(attachmentPath);
        chatMessage.setAttachmentType(attachmentType);

        groupMessagesRef.child(groupId).child(messageId).setValue(chatMessage).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                executor.execute(() -> {
                    chatMessageDao.insert(chatMessage);
                    // Update group last message
                    groupsRef.child(groupId).child("lastMessage").setValue(message);
                    groupsRef.child(groupId).child("lastMessageTimestamp").setValue(System.currentTimeMillis());
                    
                    mainHandler.post(() -> callback.onSuccess(chatMessage));
                });
                
                // Notify group members (this would usually be done by a cloud function)
                // For now, we'll skip complex group notifications
            } else {
                callback.onError(task.getException());
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
