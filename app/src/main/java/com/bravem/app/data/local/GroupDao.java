package com.bravem.app.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.bravem.app.model.Group;

import java.util.List;

@Dao
public interface GroupDao {
    @Query("SELECT * FROM groups WHERE id = :id LIMIT 1")
    Group getById(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Group group);

    @Update
    void update(Group group);

    @Query("SELECT * FROM groups ORDER BY lastMessageTimestamp DESC")
    List<Group> getAll();
    
    @Query("DELETE FROM groups WHERE id = :id")
    void deleteById(String id);
}
