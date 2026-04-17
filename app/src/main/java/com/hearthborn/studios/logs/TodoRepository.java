package com.hearthborn.studios.logs;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TodoRepository {

    private final TodoDao todoDao;
    private final ExecutorService databaseWriteExecutor;

    public TodoRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        this.todoDao = db.todoDao();
        this.databaseWriteExecutor = Executors.newSingleThreadExecutor();
    }

    // Folder operations
    public LiveData<List<TodoFolder>> getAllFolders() {
        return todoDao.getAllFolders();
    }

    public LiveData<List<TodoFolderWithItems>> getFoldersWithItems() {
        return todoDao.getFoldersWithItems();
    }

    public void insertFolder(TodoFolder folder) {
        databaseWriteExecutor.execute(() -> {
            todoDao.insertFolder(folder);
        });
    }

    public void updateFolder(TodoFolder folder) {
        databaseWriteExecutor.execute(() -> {
            todoDao.updateFolder(folder);
        });
    }

    public void deleteFolder(String folderId) {
        databaseWriteExecutor.execute(() -> {
            todoDao.deleteFolder(folderId);
        });
    }

    // Item operations
    public LiveData<List<TodoItem>> getItemsForFolder(String folderId) {
        return todoDao.getItemsForFolder(folderId);
    }

    public LiveData<Integer> getIncompleteItemCount(String folderId) {
        return todoDao.getIncompleteItemCount(folderId);
    }

    public void insertItem(TodoItem item) {
        databaseWriteExecutor.execute(() -> {
            todoDao.insertItem(item);
        });
    }

    public void updateItem(TodoItem item) {
        databaseWriteExecutor.execute(() -> {
            todoDao.updateItem(item);
        });
    }

    public void deleteItem(String itemId) {
        databaseWriteExecutor.execute(() -> {
            todoDao.deleteItem(itemId);
        });
    }
}