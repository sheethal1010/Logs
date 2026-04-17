package com.hearthborn.studios.logs;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TodoFragment extends Fragment {

    private RecyclerView foldersRecyclerView;
    private TodoFolderAdapter adapter;
    private TodoViewModel todoViewModel;
    private List<TodoFolderWithItems> allFolders = new ArrayList<>();

    private TextView titleText;
    private EditText searchInput;
    private ImageView searchIcon, closeSearchIcon;

    private boolean isSearchMode = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_todo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        foldersRecyclerView = view.findViewById(R.id.foldersRecyclerView);
        titleText = view.findViewById(R.id.titleText);
        searchInput = view.findViewById(R.id.searchInput);
        searchIcon = view.findViewById(R.id.searchIcon);
        closeSearchIcon = view.findViewById(R.id.closeSearchIcon);
        ImageView plusIcon = view.findViewById(R.id.plusIcon);
        ImageView menuIcon = view.findViewById(R.id.menuIcon);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 2);
        foldersRecyclerView.setLayoutManager(gridLayoutManager);

        todoViewModel = new ViewModelProvider(this).get(TodoViewModel.class);

        adapter = new TodoFolderAdapter(requireContext(), new ArrayList<>(), todoViewModel, new TodoFolderAdapter.OnFolderClickListener() {
            @Override
            public void onFolderClick(TodoFolderWithItems folder) {
                openFolder(folder.folder);
            }

            @Override
            public void onFolderLongClick(TodoFolderWithItems folder) {
                showFolderOptions(folder.folder);
            }
        });
        foldersRecyclerView.setAdapter(adapter);

        todoViewModel.getFoldersWithItems().observe(getViewLifecycleOwner(), folders -> {
            if (folders != null) {
                allFolders.clear();
                allFolders.addAll(folders);
                filterFolders(searchInput.getText().toString()); // Apply current filter
            }
        });

        setupReordering();
        setupSearch();

        plusIcon.setOnClickListener(v -> showCreateFolderDialog());

        menuIcon.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Menu - Coming soon", Toast.LENGTH_SHORT).show()
        );
    }

    private void setupSearch() {
        searchIcon.setOnClickListener(v -> enterSearchMode());
        closeSearchIcon.setOnClickListener(v -> exitSearchMode());

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterFolders(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void enterSearchMode() {
        isSearchMode = true;
        titleText.setVisibility(View.GONE);
        searchIcon.setVisibility(View.GONE);
        searchInput.setVisibility(View.VISIBLE);
        closeSearchIcon.setVisibility(View.VISIBLE);

        searchInput.requestFocus();
        searchInput.postDelayed(() -> {
            InputMethodManager imm = (InputMethodManager) requireContext()
                    .getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(searchInput, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 100);
    }

    private void exitSearchMode() {
        isSearchMode = false;
        titleText.setVisibility(View.VISIBLE);
        searchIcon.setVisibility(View.VISIBLE);
        searchInput.setVisibility(View.GONE);
        closeSearchIcon.setVisibility(View.GONE);

        searchInput.setText("");
        adapter.updateFolders(allFolders);

        InputMethodManager imm = (InputMethodManager) requireContext()
                .getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
        }
    }

    private void filterFolders(String query) {
        if (allFolders == null) return;

        if (query.isEmpty()) {
            adapter.updateFolders(allFolders);
            return;
        }

        List<TodoFolderWithItems> filtered = new ArrayList<>();
        String lowerCaseQuery = query.toLowerCase();
        for (TodoFolderWithItems folder : allFolders) {
            if (folder.folder.getName().toLowerCase().contains(lowerCaseQuery)) {
                filtered.add(folder);
            }
        }
        adapter.updateFolders(filtered);
    }

    private void showCreateFolderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("New Folder");

        final EditText input = new EditText(requireContext());
        input.setHint("Folder name");
        input.setPadding(50, 30, 50, 30);
        builder.setView(input);

        builder.setPositiveButton("CREATE", (dialog, which) -> {
            String folderName = input.getText().toString().trim();
            if (!folderName.isEmpty()) {
                createFolder(folderName);
            } else {
                Toast.makeText(requireContext(), "Folder name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("CANCEL", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void createFolder(String name) {
        int sortOrder = adapter.getItemCount();
        TodoFolder folder = new TodoFolder(name, sortOrder);
        todoViewModel.insertFolder(folder);

        Toast.makeText(requireContext(), "Folder created", Toast.LENGTH_SHORT).show();
    }

    private void openFolder(TodoFolder folder) {
        Intent intent = new Intent(requireContext(), TodoMakerActivity.class);
        intent.putExtra("folder_id", folder.getId());
        startActivity(intent);
    }

    private void showFolderOptions(TodoFolder folder) {
        String[] options = {"Rename", "Delete"};

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(folder.getName());
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                showRenameFolderDialog(folder);
            } else if (which == 1) {
                showDeleteConfirmation(folder);
            }
        });
        builder.show();
    }

    private void showRenameFolderDialog(TodoFolder folder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Rename Folder");

        final EditText input = new EditText(requireContext());
        input.setText(folder.getName());
        input.setSelection(folder.getName().length());
        input.setPadding(50, 30, 50, 30);
        builder.setView(input);

        builder.setPositiveButton("SAVE", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                folder.setName(newName);
                todoViewModel.updateFolder(folder);
                Toast.makeText(requireContext(), "Folder renamed", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("CANCEL", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showDeleteConfirmation(TodoFolder folder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Delete Folder");
        builder.setMessage("Are you sure you want to delete \"" + folder.getName() + "\" and all its items?");

        builder.setPositiveButton("DELETE", (dialog, which) -> {
            todoViewModel.deleteFolder(folder.getId());
            Toast.makeText(requireContext(), "Folder deleted", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("CANCEL", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void setupReordering() {
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT,
                0
        ) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                adapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {}

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                adapter.saveOrder();
            }
        });

        itemTouchHelper.attachToRecyclerView(foldersRecyclerView);
    }
}
