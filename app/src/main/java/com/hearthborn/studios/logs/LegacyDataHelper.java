package com.hearthborn.studios.logs;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to read data from the old JSON file format.
 * This is used for a one-time migration to the Room database.
 */
public class LegacyDataHelper {

    public static List<Note> getAllNotesSync(Context context) {
        List<Note> notes = new ArrayList<>();
        File notesFile = new File(new File(context.getFilesDir(), "Logs_Notes"), "notes.json");
        if (!notesFile.exists()) return notes;

        try (FileInputStream fis = new FileInputStream(notesFile)) {
            byte[] data = new byte[(int) notesFile.length()];
            fis.read(data);
            String jsonString = new String(data, "UTF-8");
            JSONArray jsonArray = new JSONArray(jsonString);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String id = obj.optString("id");
                if (id == null || id.isEmpty()) continue;

                notes.add(new Note(id, obj.optString("title", ""), obj.optString("content", ""), obj.optBoolean("pinned", false)));
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return notes;
    }

    public static List<Task> getAllTasks(Context context) {
        List<Task> tasks = new ArrayList<>();
        File tasksFile = new File(new File(context.getFilesDir(), "Logs_Tasks"), "tasks.json");
        if (!tasksFile.exists()) return tasks;

        try (FileInputStream fis = new FileInputStream(tasksFile)) {
            byte[] data = new byte[(int) tasksFile.length()];
            fis.read(data);
            String jsonString = new String(data, "UTF-8");
            JSONArray jsonArray = new JSONArray(jsonString);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                tasks.add(new Task(obj.getString("id"), obj.getString("title"), obj.getString("dayOfWeek"), obj.getBoolean("isCompleted"), obj.getLong("timestamp")));
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return tasks;
    }

    public static List<TodoFolder> getAllFolders(Context context) {
        List<TodoFolder> folders = new ArrayList<>();
        File foldersFile = new File(new File(context.getFilesDir(), "Logs_Todo"), "todo_folders.json");
        if (!foldersFile.exists()) return folders;

        try (FileInputStream fis = new FileInputStream(foldersFile)) {
            byte[] data = new byte[(int) foldersFile.length()];
            fis.read(data);
            String jsonString = new String(data, "UTF-8");
            JSONArray jsonArray = new JSONArray(jsonString);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                folders.add(new TodoFolder(obj.getString("id"), obj.getString("name"), obj.getInt("sortOrder")));
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return folders;
    }

    public static List<TodoItem> getAllItems(Context context) {
        List<TodoItem> items = new ArrayList<>();
        File itemsFile = new File(new File(context.getFilesDir(), "Logs_Todo"), "todo_items.json");
        if (!itemsFile.exists()) return items;

        try (FileInputStream fis = new FileInputStream(itemsFile)) {
            byte[] data = new byte[(int) itemsFile.length()];
            fis.read(data);
            String jsonString = new String(data, "UTF-8");
            JSONArray jsonArray = new JSONArray(jsonString);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                items.add(new TodoItem(obj.getString("id"), obj.getString("title"), obj.getString("folderId"), obj.getBoolean("isCompleted"), obj.getLong("timestamp")));
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return items;
    }
}