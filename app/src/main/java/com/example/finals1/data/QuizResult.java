package com.example.finals1.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "quiz_results")
public class QuizResult {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long setId;
    public String userEmail;
    public int correctCount;
    public int totalCount;
    public long timestampMillis;
}

