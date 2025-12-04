package com.example.finals1.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface FlashcardSetDao {
    @Insert
    long insert(FlashcardSet set);

    @Query("SELECT * FROM flashcard_sets ORDER BY id DESC")
    List<FlashcardSet> getAll();
}

