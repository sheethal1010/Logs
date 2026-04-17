package com.hearthborn.studios.logs;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.UUID;

@Entity(tableName = "todo_folders")
public class TodoFolder {

    @PrimaryKey
    @NonNull
    private String id;

    private String name;
    private int sortOrder;

    @Ignore
    public TodoFolder(String name, int sortOrder) {
        this(UUID.randomUUID().toString(), name, sortOrder);
    }

    public TodoFolder(@NonNull String id, String name, int sortOrder) {
        this.id = id;
        this.name = name;
        this.sortOrder = sortOrder;
    }

    // Getters
    @NonNull
    public String getId() { return id; }
    public String getName() { return name; }
    public int getSortOrder() { return sortOrder; }

    // Setters
    public void setId(@NonNull String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
}