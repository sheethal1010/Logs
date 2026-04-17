package com.hearthborn.studios.logs;

public class CalendarEvent {
    private String title;
    private String time;

    public CalendarEvent(String title, String time) {
        this.title = title;
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public String getTime() {
        return time;
    }
}