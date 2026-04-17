package com.hearthborn.studios.logs;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Task task);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Task> tasks);

    @Update
    void update(Task task);

    @Query("SELECT * FROM tasks WHERE dayOfWeek = :dayOfWeek ORDER BY timestamp DESC")
    LiveData<List<Task>> getTasksForDay(String dayOfWeek);

    @Query("SELECT COUNT(*) FROM tasks WHERE dayOfWeek = :dayOfWeek AND isCompleted = 0")
    LiveData<Integer> getIncompleteTaskCount(String dayOfWeek);

    @Query("DELETE FROM tasks WHERE id = :taskId")
    void deleteTask(String taskId);
}