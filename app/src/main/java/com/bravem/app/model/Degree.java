package com.bravem.app.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

/**
 * Represents a Degree Programme stored locally in Room.
 */
@Entity(tableName = "degrees")
public class Degree implements Serializable {

    @PrimaryKey
    @NonNull
    private String id;
    private String name;
    private String description;
    private String university;
    private long createdAt;
    private boolean isSynced;

    public Degree() {
    }

    public Degree(@NonNull String id, String name, String description, long createdAt) {
        this(id, name, description, null, createdAt);
    }

    public Degree(@NonNull String id, String name, String description, String university, long createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.university = university;
        this.createdAt = createdAt;
        this.isSynced = false;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public void setId(@NonNull Object id) {
        this.id = String.valueOf(id);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUniversity() {
        return university;
    }

    public void setUniversity(String university) {
        this.university = university;
    }

    public void setUniversity(Object university) {
        this.university = university != null ? String.valueOf(university) : null;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isSynced() {
        return isSynced;
    }

    public void setSynced(boolean synced) {
        isSynced = synced;
    }
}
