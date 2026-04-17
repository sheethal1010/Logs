package com.hearthborn.studios.logs;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Small placeholder fragment with centered title text.
 */
public class PlaceholderFragment extends Fragment {

    private static final String ARG_TITLE = "arg_title";

    public static PlaceholderFragment newInstance(String title) {
        PlaceholderFragment f = new PlaceholderFragment();
        Bundle b = new Bundle();
        b.putString(ARG_TITLE, title);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        FrameLayout root = new FrameLayout(requireContext());
        root.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        TextView tv = new TextView(requireContext());
        tv.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
        ));

        String title = (getArguments() != null) ? getArguments().getString(ARG_TITLE) : "Page";
        tv.setText(title);
        tv.setTextSize(22f);
        tv.setAlpha(0.45f);
        tv.setTextColor(getResources().getColor(R.color.nav_unselected, null));

        root.addView(tv);
        return root;
    }
}
