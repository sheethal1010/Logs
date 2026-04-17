package com.hearthborn.studios.logs;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Note.class, Task.class, TodoFolder.class, TodoItem.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {

    public abstract NoteDao noteDao();
    public abstract TaskDao taskDao();
    public abstract TodoDao todoDao();

    private static volatile AppDatabase INSTANCE;

    private static final ExecutorService databaseWriteExecutor = Executors.newSingleThreadExecutor();

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `tasks` (`id` TEXT NOT NULL, `title` TEXT, `dayOfWeek` TEXT, `isCompleted` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL, PRIMARY KEY(`id`))");
            database.execSQL("CREATE TABLE IF NOT EXISTS `todo_folders` (`id` TEXT NOT NULL, `name` TEXT, `sortOrder` INTEGER NOT NULL, PRIMARY KEY(`id`))");
            database.execSQL("CREATE TABLE IF NOT EXISTS `todo_items` (`id` TEXT NOT NULL, `title` TEXT, `isCompleted` INTEGER NOT NULL, `folderId` TEXT, `timestamp` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`folderId`) REFERENCES `todo_folders`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)");
        }
    };

    static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "logs_database")
                            .addMigrations(MIGRATION_1_2)
                            .addCallback(new RoomDatabase.Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    databaseWriteExecutor.execute(() -> {
                                        // This is a one-time migration from the old JSON format.
                                        List<Note> oldNotes = LegacyDataHelper.getAllNotesSync(context);
                                        if (oldNotes != null && !oldNotes.isEmpty()) {
                                            INSTANCE.noteDao().insertAll(oldNotes);
                                        }

                                        List<Task> oldTasks = LegacyDataHelper.getAllTasks(context);
                                        if (oldTasks != null && !oldTasks.isEmpty()) {
                                            INSTANCE.taskDao().insertAll(oldTasks);
                                        }

                                        List<TodoFolder> oldFolders = LegacyDataHelper.getAllFolders(context);
                                        if (oldFolders != null && !oldFolders.isEmpty()) {
                                            INSTANCE.todoDao().insertAllFolders(oldFolders);
                                        }

                                        List<TodoItem> oldItems = LegacyDataHelper.getAllItems(context);
                                        if (oldItems != null && !oldItems.isEmpty()) {
                                            INSTANCE.todoDao().insertAllItems(oldItems);
                                        }
                                    });
                                }
                            })
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
