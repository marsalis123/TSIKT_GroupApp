package com.example.test_1;

public class CalendarEvent {
    public int id, groupId, createdBy;
    public String title, description, date, color, createdAt;
    public boolean notify;

    public CalendarEvent(int id, int groupId, int createdBy, String title, String description, String date, String color, boolean notify, String createdAt) {
        this.id = id; this.groupId = groupId; this.createdBy = createdBy;
        this.title = title; this.description = description; this.date = date; this.color = color;
        this.notify = notify; this.createdAt = createdAt;
    }
}
