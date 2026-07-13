package com.bravem.app.data.local;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.bravem.app.model.Degree;

import java.util.List;

@Dao
public interface DegreeDao {
    @Query("SELECT * FROM degrees ORDER BY name ASC")
    List<Degree> getAll();

    @Query("SELECT * FROM degrees WHERE university = :university ORDER BY name ASC")
    List<Degree> getByUniversity(String university);

    @Query("SELECT * FROM degrees WHERE id = :id LIMIT 1")
    Degree getById(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Degree degree);

    @Update
    void update(Degree degree);

    @Delete
    void delete(Degree degree);
}
