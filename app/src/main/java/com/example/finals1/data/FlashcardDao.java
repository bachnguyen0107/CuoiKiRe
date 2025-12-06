package com.example.finals1.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface FlashcardDao {
    @Insert
    long insert(Flashcard card);

    @Query("SELECT * FROM flashcards WHERE setId = :setId")
    List<Flashcard> getBySet(long setId);

    @Update
    int update(Flashcard card);

    @Delete
    int delete(Flashcard card);

    @Query("DELETE FROM flashcards WHERE id = :id")
    int deleteById(long id);
}
