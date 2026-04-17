package com.hearthborn.studios.logs;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TodoMakerActivity extends AppCompatActivity {

    private EditText folderTitle;
    private LinearLayout todosContainer; // Reverted to LinearLayout to match your XML
    private TextView dateText, wordCountText, folderName;

    private String folderId;
    private TodoViewModel todoViewModel;
    private TodoFolder currentFolder;
    private List<TodoItem> currentItems; // Keep track of the current items

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_maker);

        folderId = getIntent().getStringExtra("folder_id");

        if (folderId == null) {
            Toast.makeText(this, "Error: No folder selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();

        todoViewModel = new ViewModelProvider(this).get(TodoViewModel.class);

        todoViewModel.getItemsForFolder(folderId).observe(this, items -> {
            if (items != null) {
                currentItems = items;
                buildTodosUi();
                updateWordCount(items);
            }
        });

        todoViewModel.getAllFolders().observe(this, folders -> {
            if (folders != null) {
                for (TodoFolder folder : folders) {
                    if (folder.getId().equals(folderId)) {
                        currentFolder = folder;
                        folderTitle.setText(currentFolder.getName());
                        folderName.setText(currentFolder.getName());
                        break;
                    }
                }
            }
        });

        setupToolbar();
        setupBottomButtons();
        setupTitleWatcher();
        updateDate();
    }

    private void initViews() {
        folderTitle = findViewById(R.id.folderTitle);
        todosContainer = findViewById(R.id.todosContainer); // Correctly finding the LinearLayout
        dateText = findViewById(R.id.dateText);
        wordCountText = findViewById(R.id.wordCountText);
        folderName = findViewById(R.id.folderName);
    }

    private void setupToolbar() {
        View folderDropdown = findViewById(R.id.folderDropdown);
        if (folderDropdown != null) {
            folderDropdown.setOnClickListener(v -> showFolderPicker());
        }
    }

    private void setupBottomButtons() {
        findViewById(R.id.deleteButton).setOnClickListener(v -> showDeleteConfirmation());
        findViewById(R.id.shareButton).setOnClickListener(v -> shareTodoList());
        findViewById(R.id.saveButton).setOnClickListener(v -> saveAndExit());
    }

    private void setupTitleWatcher() {
        folderTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (currentFolder != null) {
                    currentFolder.setName(s.toString());
                    folderName.setText(s.toString());
                    todoViewModel.updateFolder(currentFolder);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void buildTodosUi() {
        todosContainer.removeAllViews();
        if (currentItems != null) {
            for (TodoItem item : currentItems) {
                todosContainer.addView(createTodoView(item));
            }
        }
        // Add the button to create a new item at the end
        todosContainer.addView(createNewTodoView());
    }

    private View createTodoView(final TodoItem item) {
        View todoView = LayoutInflater.from(this).inflate(R.layout.item_task_checkbox, todosContainer, false);
        final TextView taskTitle = todoView.findViewById(R.id.taskTitle);
        final FrameLayout checkbox = todoView.findViewById(R.id.checkbox);
        final ImageView checkmark = todoView.findViewById(R.id.checkmark);

        taskTitle.setText(item.getTitle());
        updateTodoUiState(taskTitle, checkmark, item.isCompleted());

        checkbox.setOnClickListener(v -> {
            item.setCompleted(!item.isCompleted());
            todoViewModel.updateItem(item);
            // UI will update automatically via the LiveData observer
        });

        return todoView;
    }

    private View createNewTodoView() {
        View newTodoView = LayoutInflater.from(this).inflate(R.layout.item_task_checkbox, todosContainer, false);
        final TextView taskTitle = newTodoView.findViewById(R.id.taskTitle);
        final ImageView checkmark = newTodoView.findViewById(R.id.checkmark);

        taskTitle.setText("Add new todo...");
        taskTitle.setTextColor(getResources().getColor(R.color.text_grey));
        checkmark.setVisibility(View.GONE);

        newTodoView.setOnClickListener(v -> showAddTodoDialog());
        return newTodoView;
    }

    private void showAddTodoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Todo");

        final EditText input = new EditText(this);
        input.setHint("Todo item");
        input.setPadding(50, 30, 50, 30);
        builder.setView(input);

        builder.setPositiveButton("ADD", (dialog, which) -> {
            String todoText = input.getText().toString().trim();
            if (!todoText.isEmpty()) {
                TodoItem newItem = new TodoItem(todoText, folderId);
                todoViewModel.insertItem(newItem);
            }
        });

        builder.setNegativeButton("CANCEL", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void updateTodoUiState(TextView title, ImageView checkmark, boolean isCompleted) {
        if (isCompleted) {
            checkmark.setVisibility(View.VISIBLE);
            title.setPaintFlags(title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            title.setTextColor(getResources().getColor(R.color.text_grey));
        } else {
            checkmark.setVisibility(View.GONE);
            title.setPaintFlags(title.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            title.setTextColor(getResources().getColor(R.color.black));
        }
    }

    private void showFolderPicker() {
        todoViewModel.getAllFolders().observe(this, folders -> {
            if (folders != null) {
                String[] folderNames = new String[folders.size()];
                for (int i = 0; i < folders.size(); i++) {
                    folderNames[i] = folders.get(i).getName();
                }

                new AlertDialog.Builder(this)
                        .setTitle("Move to Folder")
                        .setItems(folderNames, (dialog, which) -> {
                            TodoFolder selectedFolder = folders.get(which);
                            if (currentItems != null) {
                                for (TodoItem item : currentItems) {
                                    item.setFolderId(selectedFolder.getId());
                                    todoViewModel.updateItem(item);
                                }
                            }
                            finish(); // Close the activity after moving
                        })
                        .show();
            }
        });
    }

    private void shareTodoList() {
        if (currentItems == null || currentItems.isEmpty()) {
            Toast.makeText(this, "Nothing to share", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder shareText = new StringBuilder();
        shareText.append(currentFolder.getName()).append("\n\n");

        for (TodoItem item : currentItems) {
            shareText.append(item.isCompleted() ? "☑ " : "☐ ").append(item.getTitle()).append("\n");
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, currentFolder.getName());
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString());

        startActivity(Intent.createChooser(shareIntent, "Share todo list via"));
    }

    private void saveAndExit() {
        if (currentFolder != null) {
            currentFolder.setName(folderTitle.getText().toString());
            todoViewModel.updateFolder(currentFolder);
        }
        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Folder")
                .setMessage("Are you sure you want to delete this folder and all its items?")
                .setPositiveButton("DELETE", (dialog, which) -> {
                    if (currentFolder != null) {
                        todoViewModel.deleteFolder(currentFolder.getId());
                    }
                    finish();
                })
                .setNegativeButton("CANCEL", null)
                .show();
    }

    private void updateDate() {
        String formattedDate = new SimpleDateFormat("d MMMM, yyyy", Locale.ENGLISH).format(new Date());
        dateText.setText(formattedDate);
    }

    private void updateWordCount(List<TodoItem> items) {
        int incompleteCount = 0;
        for (TodoItem item : items) {
            if (!item.isCompleted()) {
                incompleteCount++;
            }
        }
        wordCountText.setText(incompleteCount + " Tasks Left");
    }
}