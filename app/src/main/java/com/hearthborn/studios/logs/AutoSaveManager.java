package com.hearthborn.studios.logs;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.EditText;

public class AutoSaveManager {

    private Context context;
    private EditText titleField;
    private EditText contentField;
    private Handler handler;
    private Runnable autoSaveRunnable;

    private static final long AUTO_SAVE_INTERVAL = 30000; // 30 seconds
    private String lastSavedTitle = "";
    private String lastSavedContent = "";

    public AutoSaveManager(Context context, EditText titleField, EditText contentField) {
        this.context = context;
        this.titleField = titleField;
        this.contentField = contentField;
        this.handler = new Handler(Looper.getMainLooper());

        // Initialize last saved state
        this.lastSavedTitle = titleField.getText().toString();
        this.lastSavedContent = contentField.getText().toString();

        setupAutoSaveRunnable();
    }

    private void setupAutoSaveRunnable() {
        autoSaveRunnable = new Runnable() {
            @Override
            public void run() {
                saveIfChanged();
                // Schedule next auto-save
                handler.postDelayed(this, AUTO_SAVE_INTERVAL);
            }
        };
    }

    public void startAutoSave() {
        handler.postDelayed(autoSaveRunnable, AUTO_SAVE_INTERVAL);
    }

    public void stopAutoSave() {
        handler.removeCallbacks(autoSaveRunnable);
    }

    private void saveIfChanged() {
        String currentTitle = titleField.getText().toString();
        String currentContent = contentField.getText().toString();

        // Check if content has changed
        if (!currentTitle.equals(lastSavedTitle) || !currentContent.equals(lastSavedContent)) {
            // Only auto-save if there's actual content
            if (!currentTitle.trim().isEmpty() || !currentContent.trim().isEmpty()) {
                performSave(currentTitle, currentContent);
                lastSavedTitle = currentTitle;
                lastSavedContent = currentContent;
            }
        }
    }

    public void saveNow() {
        String currentTitle = titleField.getText().toString();
        String currentContent = contentField.getText().toString();

        if (!currentTitle.trim().isEmpty() || !currentContent.trim().isEmpty()) {
            performSave(currentTitle, currentContent);
            lastSavedTitle = currentTitle;
            lastSavedContent = currentContent;
        }
    }

    private void performSave(String title, String content) {
        // TODO: Save to database
        // For now, just log or show a subtle indication
        // Uncomment below to see auto-save working
        // Toast.makeText(context, "Auto-saved", Toast.LENGTH_SHORT).show();

        // This is where you'll add database save logic later
        System.out.println("Auto-save: Title=" + title + ", Content length=" + content.length());
    }

    public void resetLastSaved() {
        lastSavedTitle = titleField.getText().toString();
        lastSavedContent = contentField.getText().toString();
    }
}