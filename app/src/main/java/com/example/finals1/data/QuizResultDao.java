package com.example.finals1.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface QuizResultDao {
    @Insert
    long insert(QuizResult result);

    @Query("SELECT * FROM quiz_results WHERE userEmail = :email ORDER BY timestampMillis DESC LIMIT 1")
    QuizResult getLastForUser(String email);

    @Query("SELECT * FROM quiz_results WHERE userEmail = :email AND setId = :setId ORDER BY timestampMillis DESC LIMIT 1")
    QuizResult getLastForUserAndSet(String email, long setId);
}
