package com.example.finals1.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(User user);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User findByEmail(String email);
}

