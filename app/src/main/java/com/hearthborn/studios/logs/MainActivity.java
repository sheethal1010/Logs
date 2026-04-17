package com.hearthborn.studios.logs;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private ImageView iconTasks, iconNotes, iconCalendar, iconTodo;

    private static final int PAGE_TASKS = 0;
    private static final int PAGE_NOTES = 1;
    private static final int PAGE_CALENDAR = 2;
    private static final int PAGE_TODO = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.viewPager);
        iconTasks = findViewById(R.id.iconTasks);
        iconNotes = findViewById(R.id.iconNotes);
        iconCalendar = findViewById(R.id.iconCalendar);
        iconTodo = findViewById(R.id.iconTodo);

        MainPagerAdapter adapter = new MainPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // DISABLE swiping - only icon clicks change pages
        viewPager.setUserInputEnabled(false);

        // Icon clicks
        iconTasks.setOnClickListener(v -> viewPager.setCurrentItem(PAGE_TASKS, false));
        iconNotes.setOnClickListener(v -> viewPager.setCurrentItem(PAGE_NOTES, false));
        iconCalendar.setOnClickListener(v -> viewPager.setCurrentItem(PAGE_CALENDAR, false));
        iconTodo.setOnClickListener(v -> viewPager.setCurrentItem(PAGE_TODO, false));

        // Update icons when page changes
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateIcons(position);
            }
        });

        // Initial state
        updateIcons(PAGE_NOTES); // Start on Notes page
        viewPager.setCurrentItem(PAGE_NOTES, false);
    }

    private void updateIcons(int position) {
        int sel = ContextCompat.getColor(this, R.color.nav_selected);      // #000000
        int unsel = ContextCompat.getColor(this, R.color.nav_unselected);  // #9B9B9B

        iconTasks.setColorFilter(position == PAGE_TASKS ? sel : unsel);
        iconNotes.setColorFilter(position == PAGE_NOTES ? sel : unsel);
        iconCalendar.setColorFilter(position == PAGE_CALENDAR ? sel : unsel);
        iconTodo.setColorFilter(position == PAGE_TODO ? sel : unsel);
    }
}