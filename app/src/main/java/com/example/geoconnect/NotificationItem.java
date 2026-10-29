package com.example.geoconnect;

public class NotificationItem {
    private String id;
    private String title;
    private String content;
    private String timeFormatted;

    public NotificationItem() {

    }

    public NotificationItem(String id, String title, String content, String timeFormatted) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.timeFormatted = timeFormatted;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getTimeFormatted() {
        return timeFormatted;
    }
}