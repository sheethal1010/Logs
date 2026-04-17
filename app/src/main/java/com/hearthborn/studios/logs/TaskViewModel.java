package com.hearthborn.studios.logs;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class TaskViewModel extends AndroidViewModel {

    private final TaskRepository taskRepository;

    public TaskViewModel(@NonNull Application application) {
        super(application);
        taskRepository = new TaskRepository(application);
    }

    public LiveData<List<Task>> getTasksForDay(String dayOfWeek) {
        return taskRepository.getTasksForDay(dayOfWeek);
    }

    public LiveData<Integer> getIncompleteTaskCount(String dayOfWeek) {
        return taskRepository.getIncompleteTaskCount(dayOfWeek);
    }

    public void insert(Task task) {
        taskRepository.insert(task);
    }

    public void update(Task task) {
        taskRepository.update(task);
    }

    public void deleteTask(String taskId) {
        taskRepository.deleteTask(taskId);
    }
}