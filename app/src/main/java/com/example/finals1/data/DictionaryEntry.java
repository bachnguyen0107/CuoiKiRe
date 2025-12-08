package com.example.finals1.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "dictionary_entries")
public class DictionaryEntry {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String word;
    public String definition;
    public long fetchedAtMillis;
}

