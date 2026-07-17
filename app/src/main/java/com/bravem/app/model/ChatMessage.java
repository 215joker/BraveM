package com.bravem.app.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "chat_messages")
public class ChatMessage implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String senderId;
    private String receiverId;
    private String message;
    private long timestamp;
    private boolean isRead;
    private String attachmentPath;
    private String attachmentType; // e.g., "PDF", "IMAGE"

    public ChatMessage(String senderId, String receiverId, String message, long timestamp) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.timestamp = timestamp;
        this.isRead = false;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public String getAttachmentPath() { return attachmentPath; }
    public void setAttachmentPath(String attachmentPath) { this.attachmentPath = attachmentPath; }

    public String getAttachmentType() { return attachmentType; }
    public void setAttachmentType(String attachmentType) { this.attachmentType = attachmentType; }
}
