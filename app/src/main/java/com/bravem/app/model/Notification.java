package com.bravem.app.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "notifications")
public class Notification implements Serializable {
    @PrimaryKey
    @NonNull
    private String id;
    private String title;
    private String message;
    private String type; // "upload", "download", "new_paper"
    private String recipientId;
    private String relatedId; // paperId or other relevant ID
    private long timestamp;
    private boolean isRead;

    public Notification() {}

    public Notification(@NonNull String id, String title, String message, String type, String recipientId, String relatedId, long timestamp) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.type = type;
        this.recipientId = recipientId;
        this.relatedId = relatedId;
        this.timestamp = timestamp;
        this.isRead = false;
    }

    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getRecipientId() { return recipientId; }
    public void setRecipientId(String recipientId) { this.recipientId = recipientId; }

    public String getRelatedId() { return relatedId; }
    public void setRelatedId(String relatedId) { this.relatedId = relatedId; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
}
