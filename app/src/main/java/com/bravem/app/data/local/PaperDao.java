package com.bravem.app.data.local;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.bravem.app.model.PastPaper;

import java.util.List;

@Dao
public interface PaperDao {
    @Query("SELECT * FROM papers WHERE courseId = :courseId AND approved = 1 ORDER BY year DESC")
    List<PastPaper> getByCourseId(String courseId);

    @Query("SELECT * FROM papers WHERE approved = 0 ORDER BY createdAt DESC")
    List<PastPaper> getUnapproved();

    @Query("SELECT * FROM papers ORDER BY createdAt DESC")
    List<PastPaper> getAll();

    @Query("SELECT * FROM papers WHERE id = :id LIMIT 1")
    PastPaper getById(String id);

    @Query("SELECT * FROM papers WHERE uploadedByUid = :uid ORDER BY createdAt DESC")
    List<PastPaper> getByUploader(String uid);

    @Query("SELECT * FROM papers WHERE degreeId = :degreeId AND approved = 1 ORDER BY createdAt DESC")
    List<PastPaper> getByDegreeId(String degreeId);

    @Query("SELECT * FROM papers WHERE title LIKE '%' || :query || '%' AND approved = 1")
    List<PastPaper> search(String query);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(PastPaper paper);

    @Update
    void update(PastPaper paper);

    @Delete
    void delete(PastPaper paper);
}
