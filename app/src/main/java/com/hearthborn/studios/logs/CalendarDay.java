package com.hearthborn.studios.logs;

import java.util.ArrayList;
import java.util.List;

public class CalendarDay {
    private int dayNumber;
    private String month;
    private List<CalendarEvent> events;

    public CalendarDay(int dayNumber, String month) {
        this.dayNumber = dayNumber;
        this.month = month;
        this.events = new ArrayList<>();
    }

    public int getDayNumber() {
        return dayNumber;
    }

    public String getMonth() {
        return month;
    }

    public List<CalendarEvent> getEvents() {
        return events;
    }

    public void addEvent(CalendarEvent event) {
        if (events.size() < 3) {  // Max 3 events
            events.add(event);
        }
    }

    public boolean hasEvents() {
        return !events.isEmpty();
    }
}