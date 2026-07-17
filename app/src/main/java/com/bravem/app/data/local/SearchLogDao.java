package com.bravem.app.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.bravem.app.model.SearchLog;

import java.util.List;

@Dao
public interface SearchLogDao {
    @Query("SELECT * FROM search_logs ORDER BY timestamp DESC")
    List<SearchLog> getAll();

    @Insert
    void insert(SearchLog log);

    @Query("DELETE FROM search_logs")
    void deleteAll();
}
