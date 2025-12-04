package com.example.finals1.data;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "flashcards",
        foreignKeys = @ForeignKey(entity = FlashcardSet.class,
                parentColumns = "id",
                childColumns = "setId",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index(value = {"setId"})})
public class Flashcard {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long setId;
    public String term;
    public String definition;

    public Flashcard(long setId, String term, String definition) {
        this.setId = setId;
        this.term = term;
        this.definition = definition;
    }
}

