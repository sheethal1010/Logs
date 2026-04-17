package com.hearthborn.studios.logs;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class NotesFragment extends Fragment {

    private RecyclerView notesRecyclerView;
    private NotesAdapter notesAdapter;
    private NoteViewModel noteViewModel;
    private List<Note> allNotes = new ArrayList<>(); // Store the full list of notes

    private TextView titleText;
    private EditText searchInput;
    private ImageView searchIcon, closeSearchIcon;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        titleText = view.findViewById(R.id.titleText);
        searchInput = view.findViewById(R.id.searchInput);
        searchIcon = view.findViewById(R.id.searchIcon);
        closeSearchIcon = view.findViewById(R.id.closeSearchIcon);
        ImageView plusIcon = view.findViewById(R.id.plusIcon);
        notesRecyclerView = view.findViewById(R.id.notesRecyclerView);

        notesRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        notesAdapter = new NotesAdapter(new ArrayList<>(), (note, position) -> openNote(note));
        notesRecyclerView.setAdapter(notesAdapter);

        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);
        noteViewModel.getAllNotes().observe(getViewLifecycleOwner(), notes -> {
            if (notes != null) {
                allNotes.clear();
                allNotes.addAll(notes);
                filterNotes(searchInput.getText().toString()); // Apply current filter
            }
        });

        setupSearch();
        plusIcon.setOnClickListener(v -> createNewNote());
    }

    private void setupSearch() {
        searchIcon.setOnClickListener(v -> enterSearchMode());
        closeSearchIcon.setOnClickListener(v -> exitSearchMode());

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterNotes(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void enterSearchMode() {
        titleText.setVisibility(View.GONE);
        searchIcon.setVisibility(View.GONE);
        searchInput.setVisibility(View.VISIBLE);
        closeSearchIcon.setVisibility(View.VISIBLE);

        searchInput.requestFocus();
        searchInput.postDelayed(() -> {
            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager) requireContext()
                            .getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(searchInput, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
            }
        }, 100);
    }

    private void exitSearchMode() {
        titleText.setVisibility(View.VISIBLE);
        searchIcon.setVisibility(View.VISIBLE);
        searchInput.setVisibility(View.GONE);
        closeSearchIcon.setVisibility(View.GONE);

        searchInput.setText("");
        notesAdapter.updateNotes(allNotes);

        android.view.inputmethod.InputMethodManager imm =
                (android.view.inputmethod.InputMethodManager) requireContext()
                        .getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
        }
    }

    private void filterNotes(String query) {
        String lowerCaseQuery = query.toLowerCase();

        if (query.isEmpty()) {
            notesAdapter.updateNotes(allNotes);
            return;
        }

        List<Note> filtered = new ArrayList<>();
        for (Note note : allNotes) {
            if (note.getTitle().toLowerCase().contains(lowerCaseQuery) ||
                    note.getContent().toLowerCase().contains(lowerCaseQuery)) {
                filtered.add(note);
            }
        }
        notesAdapter.updateNotes(filtered);
    }

    private void openNote(Note note) {
        Intent intent = new Intent(requireContext(), NoteActivity.class);
        intent.putExtra("note_id", note.getId());
        startActivity(intent);
    }

    private void createNewNote() {
        Intent intent = new Intent(requireContext(), NoteActivity.class);
        startActivity(intent);
    }
}
