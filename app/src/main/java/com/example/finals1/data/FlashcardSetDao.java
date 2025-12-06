package com.example.finals1.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface FlashcardSetDao {
    @Insert
    long insert(FlashcardSet set);

    @Query("SELECT * FROM flashcard_sets ORDER BY id DESC")
    List<FlashcardSet> getAll();

    // Update the whole entity (title/description)
    @Update
    int update(FlashcardSet set);

    // Convenience query to update title/description by id
    @Query("UPDATE flashcard_sets SET title = :title, description = :description WHERE id = :id")
    int rename(long id, String title, String description);

    // Delete a single set by entity
    @Delete
    int delete(FlashcardSet set);

    // Delete a single set by id
    @Query("DELETE FROM flashcard_sets WHERE id = :id")
    int deleteById(long id);
}
