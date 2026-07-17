package com.bravem.app.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "search_logs")
public class SearchLog implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String query;
    private String userEmail;
    private long timestamp;

    public SearchLog(String query, String userEmail, long timestamp) {
        this.query = query;
        this.userEmail = userEmail;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
