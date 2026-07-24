package com.bravem.app.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "friendships")
public class Friendship implements Serializable {
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_ACCEPTED = "ACCEPTED";

    @PrimaryKey
    @NonNull
    private String id;
    private String senderId;
    private String receiverId;
    private String status;
    private long updatedAt;

    public Friendship() {
        this.id = "";
    }

    public Friendship(String senderId, String receiverId, String status, long updatedAt) {
        this.id = generateId(senderId, receiverId);
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.status = status;
        this.updatedAt = updatedAt;
    }

    public static String generateId(String u1, String u2) {
        if (u1 == null || u2 == null) return "";
        return u1.compareTo(u2) < 0 ? u1 + "_" + u2 : u2 + "_" + u1;
    }

    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
