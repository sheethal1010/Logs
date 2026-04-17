package com.hearthborn.studios.logs;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NoteStorage {

    private final NoteDao noteDao;
    private final ExecutorService databaseWriteExecutor;
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    public NoteStorage(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        this.noteDao = db.noteDao();
        this.databaseWriteExecutor = Executors.newSingleThreadExecutor();
    }

    // For fire-and-forget operations like auto-save
    public void insert(Note note) {
        databaseWriteExecutor.execute(() -> noteDao.insert(note));
    }

    // For operations that require a callback upon completion
    public void insert(Note note, Runnable onComplete) {
        databaseWriteExecutor.execute(() -> {
            noteDao.insert(note);
            if (onComplete != null) {
                mainThreadHandler.post(onComplete);
            }
        });
    }

    public void update(Note note) {
        databaseWriteExecutor.execute(() -> noteDao.update(note));
    }

    public void update(Note note, Runnable onComplete) {
        databaseWriteExecutor.execute(() -> {
            noteDao.update(note);
            if (onComplete != null) {
                mainThreadHandler.post(onComplete);
            }
        });
    }

    public void delete(Note note, Runnable onComplete) {
        databaseWriteExecutor.execute(() -> {
            noteDao.delete(note);
            if (onComplete != null) {
                mainThreadHandler.post(onComplete);
            }
        });
    }

    public LiveData<List<Note>> getAllNotes() {
        return noteDao.getAllNotes();
    }

    public LiveData<Note> getNoteById(String noteId) {
        return noteDao.getNoteById(noteId);
    }
}