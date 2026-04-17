package com.hearthborn.studios.logs;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NoteActivity extends AppCompatActivity {

    private EditText titleInput;
    private EditText contentInput;
    private TextView dateText;
    private TextView wordCountText;

    private ImageView boldButton, italicButton, underlineButton, capsButton, highlightButton;
    private ImageView alignLeftButton, alignCenterButton, alignRightButton, imageButton, bulletButton;
    private ImageView undoButton, redoButton, pinButton;

    private NoteViewModel noteViewModel;
    private Note currentNote;
    private String noteId;

    private Handler autoSaveHandler;
    private Runnable autoSaveRunnable;

    private boolean isPinned = false;

    // Formatting states
    private boolean isBoldActive = false;
    private boolean isItalicActive = false;
    private boolean isUnderlineActive = false;
    private boolean isHighlightActive = false;
    private int currentHighlightColor = 0xFFFFFF00;

    private NoteHistoryManager historyManager;
    private PopupWindow colorPickerPopup;
    private PopupWindow bulletPopup;

    private static final int PICK_IMAGE_REQUEST = 101;
    private static final int PERMISSION_REQUEST_CODE = 102;

    // KEY FIX: prevents TextWatcher loops and cursor jumping
    private boolean isUpdatingText = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        initViews();

        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);
        noteId = getIntent().getStringExtra("note_id");

        historyManager = new NoteHistoryManager();

        // Set up everything BEFORE loading note content
        setupFormattingButtons();
        setupTopToolbar();
        setupBottomButtons();
        setupAutoSave();
        setupFormattingTextWatcher();

        // Load note data - only once using currentNote == null guard
        if (noteId != null) {
            noteViewModel.getNoteById(noteId).observe(this, note -> {
                if (note != null && currentNote == null) {
                    currentNote = note;

                    isUpdatingText = true;
                    titleInput.setText(note.getTitle());

                    SpannableStringBuilder spannableContent = loadImagesIntoContent(note.getContent());
                    contentInput.setText(spannableContent);
                    // KEY FIX: cursor at END, not start
                    contentInput.setSelection(spannableContent.length());

                    isPinned = note.isPinned();
                    updatePinButton();
                    isUpdatingText = false;
                }
            });
        }

        updateDate();
        updateWordCount();
    }

    private void initViews() {
        titleInput = findViewById(R.id.noteTitle);
        contentInput = findViewById(R.id.noteContent);
        dateText = findViewById(R.id.dateText);
        wordCountText = findViewById(R.id.wordCountText);

        boldButton = findViewById(R.id.boldButton);
        italicButton = findViewById(R.id.italicButton);
        underlineButton = findViewById(R.id.underlineButton);
        capsButton = findViewById(R.id.capsButton);
        highlightButton = findViewById(R.id.highlightButton);

        alignLeftButton = findViewById(R.id.alignLeftButton);
        alignCenterButton = findViewById(R.id.alignCenterButton);
        alignRightButton = findViewById(R.id.alignRightButton);
        imageButton = findViewById(R.id.imageButton);
        bulletButton = findViewById(R.id.bulletButton);

        undoButton = findViewById(R.id.undoButton);
        redoButton = findViewById(R.id.redoButton);
        pinButton = findViewById(R.id.pinButton);
    }

    // ================ IMAGE HANDLING ================

    private SpannableStringBuilder loadImagesIntoContent(String content) {
        SpannableStringBuilder spannable = new SpannableStringBuilder(content);
        // FIXED: Proper escape sequences
        Pattern pattern = Pattern.compile("\\[IMG:([^\\]]+)\\]");
        Matcher matcher = pattern.matcher(content);

        int offset = 0;
        while (matcher.find()) {
            String filename = matcher.group(1);
            int start = matcher.start() - offset;
            int end = matcher.end() - offset;

            Bitmap bitmap = ImageManager.loadImage(this, filename);
            if (bitmap != null) {
                int maxWidth = 800;
                if (bitmap.getWidth() > maxWidth) {
                    float scale = (float) maxWidth / bitmap.getWidth();
                    int newHeight = (int) (bitmap.getHeight() * scale);
                    bitmap = Bitmap.createScaledBitmap(bitmap, maxWidth, newHeight, true);
                }

                BitmapDrawable drawable = new BitmapDrawable(getResources(), bitmap);
                drawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());

                FilenameImageSpan imageSpan = new FilenameImageSpan(drawable, filename);
                spannable.replace(start, end, " ");
                spannable.setSpan(imageSpan, start, start + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                offset += (end - start - 1);
            }
        }
        return spannable;
    }

    private void insertImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, PERMISSION_REQUEST_CODE);
                return;
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                return;
            }
        }
        openImagePicker();
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                Toast.makeText(this, "Permission denied. Cannot access images.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                String filename = ImageManager.saveImage(this, imageUri);
                if (filename != null) {
                    Bitmap bitmap = ImageManager.loadImage(this, filename);
                    if (bitmap != null) {
                        int maxWidth = 800;
                        if (bitmap.getWidth() > maxWidth) {
                            float scale = (float) maxWidth / bitmap.getWidth();
                            int newHeight = (int) (bitmap.getHeight() * scale);
                            bitmap = Bitmap.createScaledBitmap(bitmap, maxWidth, newHeight, true);
                        }

                        BitmapDrawable drawable = new BitmapDrawable(getResources(), bitmap);
                        drawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
                        FilenameImageSpan imageSpan = new FilenameImageSpan(drawable, filename);

                        // KEY FIX: use Editable.insert() not setText() to avoid cursor jump
                        int cursor = contentInput.getSelectionStart();
                        Editable editable = contentInput.getText();
                        isUpdatingText = true;
                        editable.insert(cursor, " ");
                        editable.setSpan(imageSpan, cursor, cursor + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        contentInput.setSelection(cursor + 1);
                        isUpdatingText = false;

                        Toast.makeText(this, "Image inserted", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private String extractContentWithMarkers() {
        Editable editable = contentInput.getText();
        StringBuilder result = new StringBuilder();
        FilenameImageSpan[] imageSpans = editable.getSpans(0, editable.length(), FilenameImageSpan.class);

        int lastIndex = 0;
        for (FilenameImageSpan span : imageSpans) {
            int start = editable.getSpanStart(span);
            int end = editable.getSpanEnd(span);
            if (start > lastIndex) {
                result.append(editable.subSequence(lastIndex, start));
            }
            result.append("[IMG:").append(span.getFilename()).append("]");
            lastIndex = end;
        }
        if (lastIndex < editable.length()) {
            result.append(editable.subSequence(lastIndex, editable.length()));
        }
        return result.toString();
    }

    // ================ FORMATTING ================

    private void setupFormattingButtons() {
        boldButton.setOnClickListener(v -> toggleBold());
        italicButton.setOnClickListener(v -> toggleItalic());
        underlineButton.setOnClickListener(v -> toggleUnderline());
        capsButton.setOnClickListener(v -> showCaseOptions());
        highlightButton.setOnClickListener(v -> showColorPicker());

        alignLeftButton.setOnClickListener(v -> setAlignment(Gravity.START));
        alignCenterButton.setOnClickListener(v -> setAlignment(Gravity.CENTER));
        alignRightButton.setOnClickListener(v -> setAlignment(Gravity.END));
        imageButton.setOnClickListener(v -> insertImage());
        bulletButton.setOnClickListener(v -> showBulletPopup());
    }

    private void setupTopToolbar() {
        if (undoButton != null) undoButton.setOnClickListener(v -> performUndo());
        if (redoButton != null) redoButton.setOnClickListener(v -> performRedo());

        if (pinButton != null) {
            pinButton.setOnClickListener(v -> {
                isPinned = !isPinned;
                updatePinButton();
                Toast.makeText(this, isPinned ? "com.hearthborn.studios.logs.Note pinned" : "com.hearthborn.studios.logs.Note unpinned", Toast.LENGTH_SHORT).show();
            });
        }

        View menuIcon = findViewById(R.id.menuIcon);
        if (menuIcon != null) {
            menuIcon.setOnClickListener(v -> showNoteMenu());
        }
    }

    private void updatePinButton() {
        if (pinButton == null) return;
        if (isPinned) {
            pinButton.setColorFilter(ContextCompat.getColor(this, R.color.orange));
        } else {
            pinButton.setColorFilter(ContextCompat.getColor(this, R.color.black));
        }
    }

    private void setupBottomButtons() {
        View deleteButton = findViewById(R.id.deleteButton);
        if (deleteButton != null) deleteButton.setOnClickListener(v -> deleteNote());

        View shareButton = findViewById(R.id.shareButton);
        if (shareButton != null) shareButton.setOnClickListener(v -> shareNote());

        View saveButton = findViewById(R.id.saveButton);
        if (saveButton != null) saveButton.setOnClickListener(v -> saveNote());
    }

    private void setupAutoSave() {
        autoSaveHandler = new Handler();
        autoSaveRunnable = this::autoSaveNote;

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (!isUpdatingText) {
                    historyManager.saveState(titleInput.getText().toString(), extractContentWithMarkers());
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!isUpdatingText) {
                    autoSaveHandler.removeCallbacks(autoSaveRunnable);
                    autoSaveHandler.postDelayed(autoSaveRunnable, 2000);
                    updateWordCount();
                }
            }
        };

        titleInput.addTextChangedListener(textWatcher);
        contentInput.addTextChangedListener(textWatcher);
    }

    // KEY FIX: Combined formatting + auto-bullet watcher
    private void setupFormattingTextWatcher() {
        contentInput.addTextChangedListener(new TextWatcher() {
            private int beforeLength = 0;
            private boolean detectedNewLine = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (isUpdatingText) return;
                beforeLength = s.length();
                detectedNewLine = false;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isUpdatingText) return;

                // Detect Enter key
                if (count > 0 && start < s.length() && s.charAt(start) == '\n') {
                    detectedNewLine = true;
                    return;
                }

                // Apply formatting to new text only
                if (count > 0 && s.length() > beforeLength) {
                    if (isBoldActive || isItalicActive || isUnderlineActive || isHighlightActive) {
                        applyFormattingToNewText(start, start + count);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdatingText) return;

                if (detectedNewLine) {
                    detectedNewLine = false;
                    autoContinueBullet(s);
                }
            }
        });
    }

    // FIXED: Use Editable directly, not setText()
    private void applyFormattingToNewText(int start, int end) {
        Editable editable = contentInput.getText();

        if (isBoldActive) {
            editable.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (isItalicActive) {
            editable.setSpan(new StyleSpan(Typeface.ITALIC), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (isUnderlineActive) {
            editable.setSpan(new UnderlineSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (isHighlightActive) {
            editable.setSpan(new BackgroundColorSpan(currentHighlightColor), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private void toggleBold() {
        isBoldActive = !isBoldActive;
        updateButtonState(boldButton, isBoldActive);
    }

    private void toggleItalic() {
        isItalicActive = !isItalicActive;
        updateButtonState(italicButton, isItalicActive);
    }

    private void toggleUnderline() {
        isUnderlineActive = !isUnderlineActive;
        updateButtonState(underlineButton, isUnderlineActive);
    }

    private void updateButtonState(ImageView button, boolean isActive) {
        int color = isActive ? R.color.black : R.color.text_grey;
        button.setColorFilter(ContextCompat.getColor(this, color));
    }

    // ================ COLOR PICKER ================

    private void showColorPicker() {
        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_color_picker, null);
        colorPickerPopup = new PopupWindow(popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);

        View colorYellow = popupView.findViewById(R.id.colorYellow);
        View colorGreen = popupView.findViewById(R.id.colorGreen);
        View colorBlue = popupView.findViewById(R.id.colorBlue);
        View colorPink = popupView.findViewById(R.id.colorPink);
        View colorOrange = popupView.findViewById(R.id.colorOrange);

        colorYellow.setOnClickListener(v -> selectHighlightColor(0xFFFFFF00));
        // FIXED: Light green instead of pure green
        colorGreen.setOnClickListener(v -> selectHighlightColor(0xFF90EE90));
        colorBlue.setOnClickListener(v -> selectHighlightColor(0xFFADD8E6));
        colorPink.setOnClickListener(v -> selectHighlightColor(0xFFFFB6C1));
        colorOrange.setOnClickListener(v -> selectHighlightColor(0xFFFFA500));

        colorPickerPopup.showAtLocation(highlightButton, Gravity.CENTER, 0, 0);
    }

    private void selectHighlightColor(int color) {
        currentHighlightColor = color;
        isHighlightActive = true;
        updateButtonState(highlightButton, true);
        colorPickerPopup.dismiss();
        Toast.makeText(this, "Highlight color selected", Toast.LENGTH_SHORT).show();
    }

    // ================ AUTO-CONTINUE BULLETS ================

    private void autoContinueBullet(Editable editable) {
        int cursor = contentInput.getSelectionStart();
        if (cursor <= 1) return;

        String text = editable.toString();

        // Find start of PREVIOUS line
        int prevLineStart = text.lastIndexOf('\n', cursor - 2);
        prevLineStart = (prevLineStart == -1) ? 0 : prevLineStart + 1;
        int prevLineEnd = cursor - 1;

        if (prevLineStart >= prevLineEnd) return;

        String prevLine = text.substring(prevLineStart, prevLineEnd).trim();
        String nextBullet = null;

        // Check for numeric bullets (1. 2. 3.)
        if (prevLine.matches("^\\d+\\.\\s*.*")) {
            String afterBullet = prevLine.replaceFirst("^\\d+\\.\\s*", "").trim();
            if (afterBullet.isEmpty()) {
                // Empty bullet, remove it
                isUpdatingText = true;
                editable.delete(prevLineStart, cursor);
                contentInput.setSelection(prevLineStart);
                isUpdatingText = false;
                return;
            }
            nextBullet = getNextNumericBullet(prevLine);
        }
        // Check for roman bullets (I. II. III.)
        else if (prevLine.matches("^[IVX]+\\.\\s*.*")) {
            String afterBullet = prevLine.replaceFirst("^[IVX]+\\.\\s*", "").trim();
            if (afterBullet.isEmpty()) {
                isUpdatingText = true;
                editable.delete(prevLineStart, cursor);
                contentInput.setSelection(prevLineStart);
                isUpdatingText = false;
                return;
            }
            nextBullet = getNextRomanBullet(prevLine);
        }
        // Check for dot bullets (•)
        else if (prevLine.startsWith("• ")) {
            if (prevLine.equals("•") || prevLine.trim().equals("•")) {
                isUpdatingText = true;
                editable.delete(prevLineStart, cursor);
                contentInput.setSelection(prevLineStart);
                isUpdatingText = false;
                return;
            }
            nextBullet = "• ";
        }
        // Check for square bullets (▪)
        else if (prevLine.startsWith("▪ ")) {
            if (prevLine.equals("▪") || prevLine.trim().equals("▪")) {
                isUpdatingText = true;
                editable.delete(prevLineStart, cursor);
                contentInput.setSelection(prevLineStart);
                isUpdatingText = false;
                return;
            }
            nextBullet = "▪ ";
        }

        // Insert next bullet if found
        if (nextBullet != null) {
            isUpdatingText = true;
            editable.insert(cursor, nextBullet);
            contentInput.setSelection(cursor + nextBullet.length());
            isUpdatingText = false;
        }
    }

    private String getNextNumericBullet(String line) {
        try {
            String numStr = line.substring(0, line.indexOf('.')).trim();
            return (Integer.parseInt(numStr) + 1) + ". ";
        } catch (Exception e) {
            return "1. ";
        }
    }

    private String getNextRomanBullet(String line) {
        try {
            String romanStr = line.substring(0, line.indexOf('.')).trim();
            int num = romanToDecimal(romanStr) + 1;
            return decimalToRoman(num);
        } catch (Exception e) {
            return "I. ";
        }
    }

    private int romanToDecimal(String roman) {
        int result = 0, prevValue = 0;
        for (int i = roman.length() - 1; i >= 0; i--) {
            int value = 0;
            switch (roman.charAt(i)) {
                case 'I': value = 1; break;
                case 'V': value = 5; break;
                case 'X': value = 10; break;
                case 'L': value = 50; break;
                case 'C': value = 100; break;
            }
            result += (value < prevValue) ? -value : value;
            prevValue = value;
        }
        return result;
    }

    private String decimalToRoman(int num) {
        if (num <= 0) return "I. ";
        if (num >= 40) return "XL. ";
        String[] thousands = {"", "M"};
        String[] hundreds = {"", "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM"};
        String[] tens = {"", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC"};
        String[] ones = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX"};
        return thousands[num / 1000] + hundreds[(num % 1000) / 100] + tens[(num % 100) / 10] + ones[num % 10] + ". ";
    }

    // ================ OTHER FEATURES ================

    private void showCaseOptions() {
        String[] options = {"UPPERCASE", "lowercase", "Title Case"};

        new AlertDialog.Builder(this)
                .setTitle("Change Case")
                .setItems(options, (dialog, which) -> {
                    int start = contentInput.getSelectionStart();
                    int end = contentInput.getSelectionEnd();

                    if (start == end) {
                        Toast.makeText(this, "Select text to convert", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String selected = contentInput.getText().toString().substring(start, end);
                    String result = "";

                    switch (which) {
                        case 0: result = selected.toUpperCase(); break;
                        case 1: result = selected.toLowerCase(); break;
                        case 2: result = toTitleCase(selected); break;
                    }

                    isUpdatingText = true;
                    contentInput.getText().replace(start, end, result);
                    isUpdatingText = false;
                    // FIXED: Place cursor at end, not select text
                    contentInput.setSelection(start + result.length());
                })
                .show();
    }

    private String toTitleCase(String text) {
        String[] words = text.split(" ");
        StringBuilder titleCase = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0) {
                titleCase.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    titleCase.append(word.substring(1).toLowerCase());
                }
                titleCase.append(" ");
            }
        }
        return titleCase.toString().trim();
    }

    private void setAlignment(int gravity) {
        contentInput.setGravity(gravity | Gravity.TOP);
    }

    // FIXED: Complete bullet popup implementation
    private void showBulletPopup() {
        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_bullet_options, null);
        bulletPopup = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

        LinearLayout bulletListOption = popupView.findViewById(R.id.bulletListOption);
        LinearLayout squaredListOption = popupView.findViewById(R.id.squaredListOption);
        LinearLayout numberedListOption = popupView.findViewById(R.id.numberedListOption);
        LinearLayout romanListOption = popupView.findViewById(R.id.romanListOption);

        bulletListOption.setOnClickListener(v -> {
            insertBullet("•");
            bulletPopup.dismiss();
        });

        squaredListOption.setOnClickListener(v -> {
            insertBullet("▪");
            bulletPopup.dismiss();
        });

        numberedListOption.setOnClickListener(v -> {
            insertBullet("1.");
            bulletPopup.dismiss();
        });

        romanListOption.setOnClickListener(v -> {
            insertBullet("I.");
            bulletPopup.dismiss();
        });

        bulletPopup.showAsDropDown(bulletButton);
    }

    // FIXED: Smart bullet insertion at line start
    private void insertBullet(String bullet) {
        int cursorPosition = contentInput.getSelectionStart();
        String text = contentInput.getText().toString();

        // Find start of current line
        int lineStart = text.lastIndexOf('\n', cursorPosition - 1) + 1;

        Editable editable = contentInput.getText();
        isUpdatingText = true;

        // If cursor is at line start, just insert bullet
        if (cursorPosition == lineStart) {
            editable.insert(cursorPosition, bullet + " ");
            contentInput.setSelection(cursorPosition + bullet.length() + 1);
        } else {
            // Otherwise insert newline + bullet
            editable.insert(cursorPosition, "\n" + bullet + " ");
            contentInput.setSelection(cursorPosition + bullet.length() + 2);
        }

        isUpdatingText = false;
    }

    // ================ UNDO/REDO ================

    private void performUndo() {
        NoteHistoryManager.NoteState previousState = historyManager.undo(titleInput.getText().toString(), extractContentWithMarkers());
        if (previousState != null) {
            isUpdatingText = true;
            historyManager.setUndoRedoOperation(true);

            titleInput.setText(previousState.title);
            SpannableStringBuilder spannableContent = loadImagesIntoContent(previousState.content);
            contentInput.setText(spannableContent);
            contentInput.setSelection(spannableContent.length());

            historyManager.setUndoRedoOperation(false);
            isUpdatingText = false;
        } else {
            Toast.makeText(this, "Nothing to undo", Toast.LENGTH_SHORT).show();
        }
    }

    private void performRedo() {
        NoteHistoryManager.NoteState nextState = historyManager.redo(titleInput.getText().toString(), extractContentWithMarkers());
        if (nextState != null) {
            isUpdatingText = true;
            historyManager.setUndoRedoOperation(true);

            titleInput.setText(nextState.title);
            SpannableStringBuilder spannableContent = loadImagesIntoContent(nextState.content);
            contentInput.setText(spannableContent);
            contentInput.setSelection(spannableContent.length());

            historyManager.setUndoRedoOperation(false);
            isUpdatingText = false;
        } else {
            Toast.makeText(this, "Nothing to redo", Toast.LENGTH_SHORT).show();
        }
    }

    // ================ SAVE/SHARE/DELETE ================

    private void shareNote() {
        String title = titleInput.getText().toString();
        String content = extractContentWithMarkers();

        if (title.isEmpty() && content.isEmpty()) {
            Toast.makeText(this, "Nothing to share", Toast.LENGTH_SHORT).show();
            return;
        }

        PdfGenerator.generateAndSharePdf(this, title, content);
    }

    private void showNoteMenu() {
        String[] options = {"Share as PDF", "Delete com.hearthborn.studios.logs.Note"};

        new AlertDialog.Builder(this)
                .setTitle("com.hearthborn.studios.logs.Note Options")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        shareNote();
                    } else if (which == 1) {
                        deleteNote();
                    }
                })
                .show();
    }

    private void autoSaveNote() {
        saveNoteInternal(null);
    }

    private void saveNote() {
        saveNoteInternal(() -> {
            Toast.makeText(this, "com.hearthborn.studios.logs.Note saved", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void saveNoteInternal(@Nullable Runnable onComplete) {
        String title = titleInput.getText().toString().trim();
        String content = extractContentWithMarkers();

        if (currentNote == null) {
            if (title.isEmpty() && content.isEmpty()) {
                if (onComplete != null) onComplete.run();
                return;
            }
            Note newNote = new Note(title, content);
            newNote.setPinned(isPinned);
            noteViewModel.insert(newNote, () -> {
                if (onComplete != null) onComplete.run();
            });
        } else {
            currentNote.setTitle(title);
            currentNote.setContent(content);
            currentNote.setPinned(isPinned);
            noteViewModel.update(currentNote, onComplete);
        }
    }

    private void deleteNote() {
        if (currentNote != null && noteId != null) {
            new AlertDialog.Builder(this)
                    .setTitle("Delete com.hearthborn.studios.logs.Note")
                    .setMessage("Are you sure you want to delete this note?")
                    .setPositiveButton("DELETE", (dialog, which) -> {
                        // Also delete associated images
                        String content = extractContentWithMarkers();
                        // FIXED: Proper escape sequences
                        Pattern pattern = Pattern.compile("\\[IMG:([^\\]]+)\\]");
                        Matcher matcher = pattern.matcher(content);
                        while (matcher.find()) {
                            ImageManager.deleteImage(this, matcher.group(1));
                        }

                        noteViewModel.delete(currentNote, () -> {
                            Toast.makeText(this, "com.hearthborn.studios.logs.Note deleted", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    })
                    .setNegativeButton("CANCEL", null)
                    .show();
        } else {
            finish();
        }
    }

    private void updateDate() {
        String formattedDate = new SimpleDateFormat("d MMMM, yyyy", Locale.ENGLISH).format(new Date());
        dateText.setText(formattedDate);
    }

    private void updateWordCount() {
        String content = contentInput.getText().toString();
        String[] words = content.trim().split("\\s+");
        int wordCount = content.trim().isEmpty() ? 0 : words.length;
        wordCountText.setText("Words: " + wordCount);
    }

    @Override
    protected void onPause() {
        super.onPause();
        autoSaveNote();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (autoSaveHandler != null) {
            autoSaveHandler.removeCallbacks(autoSaveRunnable);
        }
    }
}