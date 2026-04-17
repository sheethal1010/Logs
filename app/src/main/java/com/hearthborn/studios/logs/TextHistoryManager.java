package com.hearthborn.studios.logs;

import android.widget.EditText;

import java.util.Stack;

public class TextHistoryManager {

    private Stack<String> undoStack;
    private Stack<String> redoStack;
    private EditText editText;
    private String currentText;
    private boolean isUndoRedoOperation = false;

    private static final int MAX_HISTORY_SIZE = 50; // Limit history to prevent memory issues

    public TextHistoryManager(EditText editText) {
        this.editText = editText;
        this.undoStack = new Stack<>();
        this.redoStack = new Stack<>();
        this.currentText = editText.getText().toString();
    }

    public void addToHistory(String text) {
        // Don't add to history if this is an undo/redo operation
        if (isUndoRedoOperation) {
            isUndoRedoOperation = false;
            return;
        }

        // Don't add if text hasn't changed
        if (text.equals(currentText)) {
            return;
        }

        // Add current text to undo stack
        undoStack.push(currentText);

        // Limit stack size
        if (undoStack.size() > MAX_HISTORY_SIZE) {
            undoStack.remove(0);
        }

        // Clear redo stack when new text is added
        redoStack.clear();

        currentText = text;
    }

    public String undo() {
        if (undoStack.isEmpty()) {
            return null;
        }

        // Push current text to redo stack
        redoStack.push(currentText);

        // Pop from undo stack
        currentText = undoStack.pop();

        isUndoRedoOperation = true;
        return currentText;
    }

    public String redo() {
        if (redoStack.isEmpty()) {
            return null;
        }

        // Push current text to undo stack
        undoStack.push(currentText);

        // Pop from redo stack
        currentText = redoStack.pop();

        isUndoRedoOperation = true;
        return currentText;
    }

    public void clear() {
        undoStack.clear();
        redoStack.clear();
        currentText = editText.getText().toString();
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }
}