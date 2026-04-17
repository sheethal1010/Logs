package com.hearthborn.studios.logs;

import java.util.Stack;

public class NoteHistoryManager {

    // Made public to be accessible from com.hearthborn.studios.logs.NoteActivity
    public static class NoteState {
        public final String title;
        public final String content;

        NoteState(String title, String content) {
            this.title = title;
            this.content = content;
        }
    }

    private Stack<NoteState> undoStack = new Stack<>();
    private Stack<NoteState> redoStack = new Stack<>();
    private boolean isUndoRedoOperation = false;

    public void saveState(String title, String content) {
        if (!isUndoRedoOperation) {
            undoStack.push(new NoteState(title, content));
            redoStack.clear();

            if (undoStack.size() > 50) {
                undoStack.remove(0);
            }
        }
    }

    public NoteState undo(String currentTitle, String currentContent) {
        if (undoStack.isEmpty()) {
            return null;
        }

        isUndoRedoOperation = true;
        redoStack.push(new NoteState(currentTitle, currentContent));
        return undoStack.pop();
    }

    public NoteState redo(String currentTitle, String currentContent) {
        if (redoStack.isEmpty()) {
            return null;
        }

        isUndoRedoOperation = true;
        undoStack.push(new NoteState(currentTitle, currentContent));
        return redoStack.pop();
    }

    public void setUndoRedoOperation(boolean isUndoRedoOperation) {
        this.isUndoRedoOperation = isUndoRedoOperation;
    }
}