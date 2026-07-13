package com.bravem.app.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.bravem.app.model.User;

@Dao
public interface UserDao {
    @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
    User getByUid(String uid);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User getByEmail(String email);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(User user);

    @Update
    void update(User user);
}
