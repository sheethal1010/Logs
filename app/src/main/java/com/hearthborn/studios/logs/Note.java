package com.hearthborn.studios.logs;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Objects;
import java.util.UUID;

@Entity(tableName = "notes")
public class Note {

    @PrimaryKey
    @NonNull
    private String id;

    private String title;
    private String content;
    private boolean pinned;

    // Constructor for new notes, auto-generated id
    @Ignore
    public Note(String title, String content) {
        this(UUID.randomUUID().toString(), title, content, false);
    }

    // Full constructor for Room and other uses
    public Note(@NonNull String id, String title, String content, boolean pinned) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.pinned = pinned;
    }

    // Getters
    @NonNull
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public boolean isPinned() {
        return pinned;
    }

    // Setters
    public void setId(@NonNull String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    // Equals and HashCode for DiffUtil
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Note note = (Note) o;
        return pinned == note.pinned &&
                id.equals(note.id) &&
                Objects.equals(title, note.title) &&
                Objects.equals(content, note.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, content, pinned);
    }
}