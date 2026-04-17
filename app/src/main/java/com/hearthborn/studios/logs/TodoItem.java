package com.hearthborn.studios.logs;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.UUID;

@Entity(tableName = "todo_items",
        indices = {@Index(value = "folderId")},
        foreignKeys = @ForeignKey(entity = TodoFolder.class,
                                  parentColumns = "id",
                                  childColumns = "folderId",
                                  onDelete = ForeignKey.CASCADE))
public class TodoItem {

    @PrimaryKey
    @NonNull
    private String id;

    private String title;
    private boolean isCompleted;
    private String folderId;
    private long timestamp;

    @Ignore
    public TodoItem(String title, String folderId) {
        this(UUID.randomUUID().toString(), title, folderId, false, System.currentTimeMillis());
    }

    public TodoItem(@NonNull String id, String title, String folderId, boolean isCompleted, long timestamp) {
        this.id = id;
        this.title = title;
        this.folderId = folderId;
        this.isCompleted = isCompleted;
        this.timestamp = timestamp;
    }

    // Getters
    @NonNull
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getFolderId() { return folderId; }
    public boolean isCompleted() { return isCompleted; }
    public long getTimestamp() { return timestamp; }

    // Setters
    public void setId(@NonNull String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setFolderId(String folderId) { this.folderId = folderId; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}