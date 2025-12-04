package com.example.finals1.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "flashcard_sets")
public class FlashcardSet {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String title;
    public String description;

    public FlashcardSet(String title, String description) {
        this.title = title;
        this.description = description;
    }
}

