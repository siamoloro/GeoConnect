package com.example.geoconnect;

public class SupportCase {
    private String caseId;
    private String userId;
    private String title;
    private String description;
    private long timestamp;

    public SupportCase() {}

    public SupportCase(String caseId, String userId, String title, String description, long timestamp) {
        this.caseId = caseId;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.timestamp = timestamp;
    }

    // Getters
    public String getCaseId() { return caseId; }
    public String getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public long getTimestamp() { return timestamp; }
}