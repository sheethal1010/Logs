package com.hearthborn.studios.logs;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TasksFragment extends Fragment {

    private LinearLayout daysContainer;
    private TextView titleText;
    private EditText searchInput;
    private ImageView searchIcon, closeSearchIcon;

    private final String[] daysOfWeek = {
            "MONDAY", "TUESDAY", "WEDNESDAY",
            "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"
    };
    private String currentExpandedDay = null;
    private boolean isSearchMode = false;
    private String searchQuery = "";
    private TaskViewModel taskViewModel;

    private final List<TaskAdapter> adapters = new ArrayList<>();
    private final List<List<Task>> allTasks = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        for (int i = 0; i < daysOfWeek.length; i++) {
            allTasks.add(new ArrayList<>());
        }
        return inflater.inflate(R.layout.fragment_tasks, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        daysContainer = view.findViewById(R.id.daysContainer);
        titleText = view.findViewById(R.id.titleText);
        searchInput = view.findViewById(R.id.searchInput);
        searchIcon = view.findViewById(R.id.searchIcon);
        closeSearchIcon = view.findViewById(R.id.closeSearchIcon);
        ImageView menuIcon = view.findViewById(R.id.menuIcon);

        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        setupSearch();
        menuIcon.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Menu - Coming soon", Toast.LENGTH_SHORT).show()
        );

        buildDaysView();
        setupObservers();

        expandDay(getCurrentDayOfWeek());
    }

    private void setupSearch() {
        searchIcon.setOnClickListener(v -> enterSearchMode());
        closeSearchIcon.setOnClickListener(v -> exitSearchMode());

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().toLowerCase();
                filterAndUpdateLists();
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
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(searchInput, InputMethodManager.SHOW_IMPLICIT);
        }, 100);
    }

    private void exitSearchMode() {
        isSearchMode = false;
        searchQuery = "";

        titleText.setVisibility(View.VISIBLE);
        searchIcon.setVisibility(View.VISIBLE);
        searchInput.setVisibility(View.GONE);
        closeSearchIcon.setVisibility(View.GONE);

        searchInput.setText("");
        filterAndUpdateLists();

        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
    }

    private void buildDaysView() {
        daysContainer.removeAllViews();
        adapters.clear();
        for (String day : daysOfWeek) {
            daysContainer.addView(createDayView(day));
        }
    }

    private void setupObservers() {
        for (int i = 0; i < daysOfWeek.length; i++) {
            final int index = i;
            taskViewModel.getTasksForDay(daysOfWeek[i]).observe(getViewLifecycleOwner(), tasks -> {
                if (tasks != null) {
                    allTasks.set(index, tasks);
                    filterAndUpdateListsForDay(index);
                }
            });
        }
    }

    private void filterAndUpdateLists() {
        for (int i = 0; i < daysOfWeek.length; i++) {
            filterAndUpdateListsForDay(i);
        }
    }

    private void filterAndUpdateListsForDay(int dayIndex) {
        List<Task> fullList = allTasks.get(dayIndex);
        List<Task> filteredTasks = new ArrayList<>();

        if (searchQuery.isEmpty()) {
            filteredTasks.addAll(fullList);
        } else {
            for (Task task : fullList) {
                if (task.getTitle().toLowerCase().contains(searchQuery)) {
                    filteredTasks.add(task);
                }
            }
        }

        if (dayIndex < adapters.size()) {
            adapters.get(dayIndex).updateTasks(filteredTasks);
        }

        View dayView = daysContainer.getChildAt(dayIndex);
        if (dayView != null) {
            boolean shouldBeVisible = !(isSearchMode && filteredTasks.isEmpty() && !daysOfWeek[dayIndex].toLowerCase().contains(searchQuery));
            dayView.setVisibility(shouldBeVisible ? View.VISIBLE : View.GONE);
        }
    }


    private View createDayView(String dayName) {
        View dayView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_day, daysContainer, false);

        TextView dayNameText = dayView.findViewById(R.id.dayName);
        TextView taskCountText = dayView.findViewById(R.id.taskCount);
        TextView dateTimeText = dayView.findViewById(R.id.dateTime);
        LinearLayout dayHeader = dayView.findViewById(R.id.dayHeader);
        ScrollView dayScroll = dayView.findViewById(R.id.dayScroll);
        RecyclerView tasksRecycler = dayView.findViewById(R.id.tasksRecycler);
        EditText addTaskInput = dayView.findViewById(R.id.addTaskInput);
        TextView addTaskButton = dayView.findViewById(R.id.addTaskButton);

        dayNameText.setText(dayName);

        TaskAdapter adapter = new TaskAdapter(requireContext(), new ArrayList<>(), taskViewModel);
        adapters.add(adapter);
        tasksRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        tasksRecycler.setAdapter(adapter);
        tasksRecycler.setNestedScrollingEnabled(false);
        if (tasksRecycler.getItemAnimator() instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) tasksRecycler.getItemAnimator()).setSupportsChangeAnimations(false);
        }

        taskViewModel.getIncompleteTaskCount(dayName).observe(getViewLifecycleOwner(), count -> {
            if (count != null) {
                taskCountText.setText(String.valueOf(count));
            }
        });

        updateDateTime(dayName, dateTimeText);

        dayHeader.setOnClickListener(v -> {
            if (dayScroll.getVisibility() == View.VISIBLE) {
                collapseDay(dayScroll);
            } else {
                expandDay(dayName);
            }
        });

        addTaskButton.setOnClickListener(v -> {
            String title = addTaskInput.getText().toString().trim();
            if (!title.isEmpty()) {
                addNewTask(dayName, title, addTaskInput);
            }
        });

        addTaskInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addTaskButton.performClick();
                return true;
            }
            return false;
        });

        dayScroll.setVisibility(View.GONE);
        return dayView;
    }

    private void expandDay(String dayName) {
        if (dayName == null || dayName.equals(currentExpandedDay)) return;

        for (int i = 0; i < daysContainer.getChildCount(); i++) {
            View v = daysContainer.getChildAt(i);
            ScrollView scroll = v.findViewById(R.id.dayScroll);
            if (scroll != null && scroll.getVisibility() == View.VISIBLE) {
                collapseWithAnimation(scroll);
            }
        }

        for (int i = 0; i < daysContainer.getChildCount(); i++) {
            View v = daysContainer.getChildAt(i);
            TextView name = v.findViewById(R.id.dayName);
            if (name != null && name.getText().toString().equals(dayName)) {
                ScrollView toExpand = v.findViewById(R.id.dayScroll);
                if (toExpand != null) {
                    expandWithAnimation(toExpand);
                    currentExpandedDay = dayName;
                }
                break;
            }
        }
    }

    private void collapseDay(ScrollView view) {
        if (view == null) return;
        collapseWithAnimation(view);
        currentExpandedDay = null;
    }

    private void expandWithAnimation(final View view) {
        if (view == null) return;

        view.setVisibility(View.VISIBLE);
        view.measure(
                View.MeasureSpec.makeMeasureSpec(
                        ((View) view.getParent()).getWidth(),
                        View.MeasureSpec.EXACTLY
                ),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );

        int targetHeight = view.getMeasuredHeight();
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = 0;
        view.setLayoutParams(params);

        ValueAnimator animator = ValueAnimator.ofInt(0, targetHeight);
        animator.addUpdateListener(a -> {
            params.height = (int) a.getAnimatedValue();
            view.setLayoutParams(params);
        });

        animator.addListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(Animator animation) {}
            @Override public void onAnimationEnd(Animator animation) {
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                view.setLayoutParams(params);
            }
            @Override public void onAnimationCancel(Animator animation) {}
            @Override public void onAnimationRepeat(Animator animation) {}
        });

        animator.setDuration(300);
        animator.start();
    }

    private void collapseWithAnimation(final View view) {
        if (view == null || view.getVisibility() != View.VISIBLE) return;

        int height = view.getMeasuredHeight();
        final ViewGroup.LayoutParams params = view.getLayoutParams();

        ValueAnimator animator = ValueAnimator.ofInt(height, 0);
        animator.addUpdateListener(a -> {
            params.height = (int) a.getAnimatedValue();
            view.setLayoutParams(params);
        });

        animator.addListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(Animator animation) {}
            @Override public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.GONE);
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                view.setLayoutParams(params);
            }
            @Override public void onAnimationCancel(Animator animation) {}
            @Override public void onAnimationRepeat(Animator animation) {}
        });

        animator.setDuration(300);
        animator.start();
    }

    private void addNewTask(String dayName, String title, EditText input) {
        Task task = new Task(title, dayName);
        taskViewModel.insert(task);
        input.setText("");
    }

    private void updateDateTime(String dayName, TextView dateTimeText) {
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DAY_OF_WEEK);

        int targetDay = 1;
        for (int i = 0; i < daysOfWeek.length; i++) {
            if (daysOfWeek[i].equals(dayName)) {
                targetDay = i + 2; // Calendar.SUNDAY = 1, MONDAY = 2, etc.
                break;
            }
        }

        int diff = targetDay - today;
        calendar.add(Calendar.DAY_OF_YEAR, diff);

        Date date = calendar.getTime();
        String formattedDate = new SimpleDateFormat("MMM, dd yyyy - h:mm a", Locale.ENGLISH).format(date);
        dateTimeText.setText(formattedDate.toUpperCase());
    }

    private String getCurrentDayOfWeek() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);

        switch (day) {
            case Calendar.MONDAY: return "MONDAY";
            case Calendar.TUESDAY: return "TUESDAY";
            case Calendar.WEDNESDAY: return "WEDNESDAY";
            case Calendar.THURSDAY: return "THURSDAY";
            case Calendar.FRIDAY: return "FRIDAY";
            case Calendar.SATURDAY: return "SATURDAY";
            case Calendar.SUNDAY: return "SUNDAY";
            default: return "";
        }
    }
}