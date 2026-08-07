package com.bravem.app.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

/**
 * Represents a Course stored locally in Room.
 */
@Entity(tableName = "courses")
public class Course implements Serializable {

    @PrimaryKey
    @NonNull
    private String id;
    private String degreeId;
    private String name;
    private String code;
    private long createdAt;
    private boolean isSynced;

    public Course() {
    }

    public Course(@NonNull String id, String degreeId, String name, String code, long createdAt) {
        this.id = id;
        this.degreeId = degreeId;
        this.name = name;
        this.code = code;
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

    public String getDegreeId() {
        return degreeId;
    }

    public void setDegreeId(String degreeId) {
        this.degreeId = degreeId;
    }

    public void setDegreeId(Object degreeId) {
        this.degreeId = degreeId != null ? String.valueOf(degreeId) : null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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
