package com.hearthborn.studios.logs;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskRepository {

    private final TaskDao taskDao;
    private final ExecutorService databaseWriteExecutor;

    public TaskRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        this.taskDao = db.taskDao();
        this.databaseWriteExecutor = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Task>> getTasksForDay(String dayOfWeek) {
        return taskDao.getTasksForDay(dayOfWeek);
    }

    public LiveData<Integer> getIncompleteTaskCount(String dayOfWeek) {
        return taskDao.getIncompleteTaskCount(dayOfWeek);
    }

    public void insert(Task task) {
        databaseWriteExecutor.execute(() -> {
            taskDao.insert(task);
        });
    }

    public void update(Task task) {
        databaseWriteExecutor.execute(() -> {
            taskDao.update(task);
        });
    }

    public void deleteTask(String taskId) {
        databaseWriteExecutor.execute(() -> {
            taskDao.deleteTask(taskId);
        });
    }
}