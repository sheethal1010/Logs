package com.hearthborn.studios.logs;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

/**
 * A data class to hold a com.hearthborn.studios.logs.TodoFolder and its corresponding list of TodoItems.
 */
public class TodoFolderWithItems {

    @Embedded
    public TodoFolder folder;

    @Relation(
            parentColumn = "id",
            entityColumn = "folderId",
            entity = TodoItem.class
    )
    public List<TodoItem> items;
}