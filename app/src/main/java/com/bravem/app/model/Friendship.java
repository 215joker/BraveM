package com.bravem.app.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "friendships")
public class Friendship implements Serializable {
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_ACCEPTED = "ACCEPTED";

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String senderId;
    private String receiverId;
    private String status;
    private long updatedAt;

    public Friendship(String senderId, String receiverId, String status, long updatedAt) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.status = status;
        this.updatedAt = updatedAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
