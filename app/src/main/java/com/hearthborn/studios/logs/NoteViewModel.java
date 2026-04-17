package com.hearthborn.studios.logs;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class NoteViewModel extends AndroidViewModel {

    private final NoteStorage noteStorage;
    private final LiveData<List<Note>> allNotes;

    public NoteViewModel(@NonNull Application application) {
        super(application);
        noteStorage = new NoteStorage(application);
        allNotes = noteStorage.getAllNotes();
    }

    public LiveData<List<Note>> getAllNotes() {
        return allNotes;
    }

    public void insert(Note note) {
        noteStorage.insert(note);
    }

    public void insert(Note note, Runnable onComplete) {
        noteStorage.insert(note, onComplete);
    }

    public void update(Note note) {
        noteStorage.update(note);
    }

    public void update(Note note, Runnable onComplete) {
        noteStorage.update(note, onComplete);
    }

    public void delete(Note note, Runnable onComplete) {
        noteStorage.delete(note, onComplete);
    }

    public LiveData<Note> getNoteById(String noteId) {
        return noteStorage.getNoteById(noteId);
    }
}