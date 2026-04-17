package com.hearthborn.studios.logs;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.UUID;

@Entity(tableName = "tasks")
public class Task {

    @PrimaryKey
    @NonNull
    private String id;

    private String title;
    private String dayOfWeek; // MONDAY, TUESDAY, etc.
    private boolean isCompleted;
    private long timestamp; // When task was added/modified

    @Ignore
    public Task(String title, String dayOfWeek) {
        this(UUID.randomUUID().toString(), title, dayOfWeek, false, System.currentTimeMillis());
    }

    public Task(@NonNull String id, String title, String dayOfWeek, boolean isCompleted, long timestamp) {
        this.id = id;
        this.title = title;
        this.dayOfWeek = dayOfWeek;
        this.isCompleted = isCompleted;
        this.timestamp = timestamp;
    }

    // Getters
    @NonNull
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDayOfWeek() { return dayOfWeek; }
    public boolean isCompleted() { return isCompleted; }
    public long getTimestamp() { return timestamp; }

    // Setters
    public void setId(@NonNull String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}