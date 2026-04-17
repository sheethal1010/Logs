package com.hearthborn.studios.logs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CalendarFragment extends Fragment {

    private RecyclerView calendarRecyclerView;
    private CalendarAdapter adapter;
    private List<CalendarDay> calendarDays;
    private LinearLayout signInPrompt;

    private boolean isSignedIn = true; // TODO: Change to true when Google Sign-In implemented
    private boolean isLoadingPast = false;
    private boolean isLoadingFuture = false;

    private Calendar currentCalendar;
    private static final int DAYS_TO_LOAD = 30;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        calendarRecyclerView = view.findViewById(R.id.calendarRecyclerView);
        signInPrompt = view.findViewById(R.id.signInPrompt);

        view.findViewById(R.id.searchIcon).setOnClickListener(v ->
                Toast.makeText(requireContext(), "Search - Coming soon", Toast.LENGTH_SHORT).show()
        );

        view.findViewById(R.id.menuIcon).setOnClickListener(v ->
                Toast.makeText(requireContext(), "Menu - Coming soon", Toast.LENGTH_SHORT).show()
        );

        view.findViewById(R.id.signInButton).setOnClickListener(v -> {
            // TODO: Implement Google Sign-In
            Toast.makeText(requireContext(), "Google Sign-In - Coming soon", Toast.LENGTH_SHORT).show();
        });

        if (isSignedIn) {
            showCalendar();
        } else {
            showSignInPrompt();
        }
    }

    private void showSignInPrompt() {
        calendarRecyclerView.setVisibility(View.GONE);
        signInPrompt.setVisibility(View.VISIBLE);
    }

    private void showCalendar() {
        calendarRecyclerView.setVisibility(View.VISIBLE);
        signInPrompt.setVisibility(View.GONE);

        calendarDays = new ArrayList<>();
        currentCalendar = Calendar.getInstance();

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        calendarRecyclerView.setLayoutManager(layoutManager);

        // Load initial days (15 past + today + 14 future = 30 days)
        loadInitialDays();

        adapter = new CalendarAdapter(requireContext(), calendarDays);
        calendarRecyclerView.setAdapter(adapter);

        // Scroll to today (position 15)
        calendarRecyclerView.scrollToPosition(15);

        // Setup infinite scroll
        setupInfiniteScroll(layoutManager);
    }

    private void loadInitialDays() {
        Calendar cal = Calendar.getInstance();

        // Load 15 days in the past
        cal.add(Calendar.DAY_OF_YEAR, -15);
        for (int i = 0; i < DAYS_TO_LOAD; i++) {
            calendarDays.add(createCalendarDay(cal));
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    private void setupInfiniteScroll(LinearLayoutManager layoutManager) {
        calendarRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
                int lastVisiblePosition = layoutManager.findLastVisibleItemPosition();
                int totalItems = layoutManager.getItemCount();

                // Load more past days when scrolling up
                if (firstVisiblePosition < 5 && !isLoadingPast) {
                    loadMorePastDays();
                }

                // Load more future days when scrolling down
                if (lastVisiblePosition > totalItems - 5 && !isLoadingFuture) {
                    loadMoreFutureDays();
                }
            }
        });
    }

    private void loadMorePastDays() {
        isLoadingPast = true;

        List<CalendarDay> newDays = new ArrayList<>();
        CalendarDay firstDay = calendarDays.get(0);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, firstDay.getDayNumber());
        cal.set(Calendar.MONTH, getMonthNumber(firstDay.getMonth()));

        // Load 15 more days in the past
        for (int i = 0; i < 15; i++) {
            cal.add(Calendar.DAY_OF_YEAR, -1);
            newDays.add(0, createCalendarDay(cal));
        }

        calendarDays.addAll(0, newDays);
        adapter.notifyItemRangeInserted(0, newDays.size());

        // Maintain scroll position
        calendarRecyclerView.scrollToPosition(newDays.size());

        isLoadingPast = false;
    }

    private void loadMoreFutureDays() {
        isLoadingFuture = true;

        List<CalendarDay> newDays = new ArrayList<>();
        CalendarDay lastDay = calendarDays.get(calendarDays.size() - 1);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, lastDay.getDayNumber());
        cal.set(Calendar.MONTH, getMonthNumber(lastDay.getMonth()));

        // Load 15 more days in the future
        for (int i = 0; i < 15; i++) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
            newDays.add(createCalendarDay(cal));
        }

        adapter.addDays(newDays);
        isLoadingFuture = false;
    }

    private CalendarDay createCalendarDay(Calendar cal) {
        int dayNumber = cal.get(Calendar.DAY_OF_MONTH);
        String month = new SimpleDateFormat("MMM", Locale.ENGLISH).format(cal.getTime());

        CalendarDay day = new CalendarDay(dayNumber, month);

        // TODO: Replace with real Google Calendar events
        // For now, add sample events for demo
        if (dayNumber % 2 != 0) { // Odd days have events
            day.addEvent(new CalendarEvent("Coursera Course", "11:00 AM"));
            day.addEvent(new CalendarEvent("Coursera Course", "11:00 AM"));
            day.addEvent(new CalendarEvent("Coursera Course", "11:00 AM"));
        }

        return day;
    }

    private int getMonthNumber(String monthAbbr) {
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        for (int i = 0; i < months.length; i++) {
            if (months[i].equals(monthAbbr)) {
                return i;
            }
        }
        return 0;
    }
}