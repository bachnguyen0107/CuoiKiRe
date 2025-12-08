package com.example.finals1.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DictionaryDao {
    @Query("SELECT * FROM dictionary_entries WHERE word = :word ORDER BY fetchedAtMillis DESC LIMIT 1")
    DictionaryEntry getLatest(String word);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(DictionaryEntry entry);

    @Query("DELETE FROM dictionary_entries WHERE word = :word")
    int deleteWord(String word);

    @Query("SELECT * FROM dictionary_entries ORDER BY fetchedAtMillis DESC LIMIT 50")
    List<DictionaryEntry> recent();
}

