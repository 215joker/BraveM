package com.bravem.app.data.local;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.bravem.app.model.Course;

import java.util.List;

@Dao
public interface CourseDao {
    @Query("SELECT * FROM courses WHERE degreeId = :degreeId ORDER BY name ASC")
    List<Course> getByDegreeId(String degreeId);

    @Query("SELECT * FROM courses ORDER BY name ASC")
    List<Course> getAll();

    @Query("SELECT * FROM courses WHERE id = :id LIMIT 1")
    Course getById(String id);

    @Query("SELECT * FROM courses WHERE isSynced = 0")
    List<Course> getUnsynced();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Course course);

    @Update
    void update(Course course);

    @Delete
    void delete(Course course);
}
