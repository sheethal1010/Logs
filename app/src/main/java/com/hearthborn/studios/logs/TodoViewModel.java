package com.hearthborn.studios.logs;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class TodoViewModel extends AndroidViewModel {

    private final TodoRepository todoRepository;

    public TodoViewModel(@NonNull Application application) {
        super(application);
        todoRepository = new TodoRepository(application);
    }

    // Folder operations
    public LiveData<List<TodoFolder>> getAllFolders() {
        return todoRepository.getAllFolders();
    }

    public LiveData<List<TodoFolderWithItems>> getFoldersWithItems() {
        return todoRepository.getFoldersWithItems();
    }

    public void insertFolder(TodoFolder folder) {
        todoRepository.insertFolder(folder);
    }

    public void updateFolder(TodoFolder folder) {
        todoRepository.updateFolder(folder);
    }

    public void deleteFolder(String folderId) {
        todoRepository.deleteFolder(folderId);
    }

    // Item operations
    public LiveData<List<TodoItem>> getItemsForFolder(String folderId) {
        return todoRepository.getItemsForFolder(folderId);
    }

    public LiveData<Integer> getIncompleteItemCount(String folderId) {
        return todoRepository.getIncompleteItemCount(folderId);
    }

    public void insertItem(TodoItem item) {
        todoRepository.insertItem(item);
    }

    public void updateItem(TodoItem item) {
        todoRepository.updateItem(item);
    }

    public void deleteItem(String itemId) {
        todoRepository.deleteItem(itemId);
    }
}