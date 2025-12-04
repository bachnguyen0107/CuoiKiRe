package com.example.finals1.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface FlashcardDao {
    @Insert
    long insert(Flashcard card);

    @Query("SELECT * FROM flashcards WHERE setId = :setId")
    List<Flashcard> getBySet(long setId);
}

