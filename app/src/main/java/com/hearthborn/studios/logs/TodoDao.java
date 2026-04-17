package com.hearthborn.studios.logs;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TodoDao {

    // Folder operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertFolder(TodoFolder folder);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllFolders(List<TodoFolder> folders);

    @Update
    void updateFolder(TodoFolder folder);

    @Query("DELETE FROM todo_folders WHERE id = :folderId")
    void deleteFolder(String folderId);

    @Query("SELECT * FROM todo_folders ORDER BY sortOrder ASC")
    LiveData<List<TodoFolder>> getAllFolders();

    @Transaction
    @Query("SELECT * FROM todo_folders ORDER BY sortOrder ASC")
    LiveData<List<TodoFolderWithItems>> getFoldersWithItems();

    // Item operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertItem(TodoItem item);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllItems(List<TodoItem> items);

    @Update
    void updateItem(TodoItem item);

    @Query("DELETE FROM todo_items WHERE id = :itemId")
    void deleteItem(String itemId);

    @Query("SELECT * FROM todo_items WHERE folderId = :folderId ORDER BY timestamp ASC")
    LiveData<List<TodoItem>> getItemsForFolder(String folderId);

    @Query("SELECT COUNT(*) FROM todo_items WHERE folderId = :folderId AND isCompleted = 0")
    LiveData<Integer> getIncompleteItemCount(String folderId);
}