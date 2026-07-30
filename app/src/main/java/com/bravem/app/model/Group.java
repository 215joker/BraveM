package com.bravem.app.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "groups")
public class Group implements Serializable {
    @PrimaryKey
    @NonNull
    private String id;
    private String name;
    private String creatorId;
    private String courseId; // For course-linked groups
    private long createdAt;
    private String lastMessage;
    private long lastMessageTimestamp;
    private String groupIcon;

    public Group() {}

    public Group(@NonNull String id, String name, String creatorId, String courseId, long createdAt) {
        this.id = id;
        this.name = name;
        this.creatorId = creatorId;
        this.courseId = courseId;
        this.createdAt = createdAt;
    }

    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCreatorId() { return creatorId; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public long getLastMessageTimestamp() { return lastMessageTimestamp; }
    public void setLastMessageTimestamp(long lastMessageTimestamp) { this.lastMessageTimestamp = lastMessageTimestamp; }

    public String getGroupIcon() { return groupIcon; }
    public void setGroupIcon(String groupIcon) { this.groupIcon = groupIcon; }
}
