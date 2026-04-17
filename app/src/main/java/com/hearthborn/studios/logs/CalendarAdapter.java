package com.hearthborn.studios.logs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.DayViewHolder> {

    private Context context;
    private List<CalendarDay> days;

    public CalendarAdapter(Context context, List<CalendarDay> days) {
        this.context = context;
        this.days = days;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_calendar_day, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        CalendarDay day = days.get(position);

        holder.dayNumber.setText(String.valueOf(day.getDayNumber()));
        holder.monthText.setText(day.getMonth());

        if (day.hasEvents()) {
            holder.eventsContainer.setVisibility(View.VISIBLE);
            holder.noEventsText.setVisibility(View.GONE);

            // Clear previous events
            holder.eventsContainer.removeAllViews();

            // Add events
            List<CalendarEvent> events = day.getEvents();
            for (int i = 0; i < events.size(); i++) {
                View eventView = createEventView(events.get(i), i, events.size());
                holder.eventsContainer.addView(eventView);
            }
        } else {
            holder.eventsContainer.setVisibility(View.GONE);
            holder.noEventsText.setVisibility(View.VISIBLE);
        }
    }

    private View createEventView(CalendarEvent event, int position, int totalEvents) {
        View eventView = LayoutInflater.from(context).inflate(R.layout.item_calendar_event, null, false);

        TextView eventTitle = eventView.findViewById(R.id.eventTitle);
        TextView eventTime = eventView.findViewById(R.id.eventTime);
        LinearLayout eventContainer = eventView.findViewById(R.id.eventContainer);

        eventTitle.setText(event.getTitle());
        eventTime.setText(event.getTime());

        // Set background based on position
        int backgroundRes;
        if (totalEvents == 1) {
            backgroundRes = R.drawable.event_box_single;
        } else if (position == 0) {
            backgroundRes = R.drawable.event_box_first;
        } else if (position == totalEvents - 1) {
            backgroundRes = R.drawable.event_box_last;
        } else {
            backgroundRes = R.drawable.event_box_middle;
        }

        eventContainer.setBackground(ContextCompat.getDrawable(context, backgroundRes));

        // Add margin only between events (not before first)
        if (position > 0) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.topMargin = (int) (2 * context.getResources().getDisplayMetrics().density);
            eventView.setLayoutParams(params);
        }

        return eventView;
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    public void addDays(List<CalendarDay> newDays) {
        int startPosition = days.size();
        days.addAll(newDays);
        notifyItemRangeInserted(startPosition, newDays.size());
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {
        TextView dayNumber, monthText, noEventsText;
        LinearLayout eventsContainer;

        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            dayNumber = itemView.findViewById(R.id.dayNumber);
            monthText = itemView.findViewById(R.id.monthText);
            noEventsText = itemView.findViewById(R.id.noEventsText);
            eventsContainer = itemView.findViewById(R.id.eventsContainer);
        }
    }
}