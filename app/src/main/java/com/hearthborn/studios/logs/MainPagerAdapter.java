package com.hearthborn.studios.logs;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * Returns: 0 -> com.hearthborn.studios.logs.TasksFragment (already in your project)
 *          1 -> com.hearthborn.studios.logs.NotesFragment / Memories page (already in your project)
 *          2 -> com.hearthborn.studios.logs.PlaceholderFragment ("Calendar")
 *          3 -> com.hearthborn.studios.logs.PlaceholderFragment ("Todo")
 *
 * Make sure com.hearthborn.studios.logs.TasksFragment and com.hearthborn.studios.logs.NotesFragment class names match your project.
 */
public class MainPagerAdapter extends FragmentStateAdapter {

    public MainPagerAdapter(@NonNull FragmentActivity fa) {
        super(fa);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new TasksFragment();
            case 1: return new NotesFragment();
            case 2: return new CalendarFragment();
            case 3: return new TodoFragment();  // Changed from com.hearthborn.studios.logs.PlaceholderFragment
            default: return PlaceholderFragment.newInstance("Page");
        }
    }
    @Override
    public int getItemCount() {
        return 4;
    }
}
