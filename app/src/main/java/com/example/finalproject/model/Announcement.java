package com.example.finalproject.model;

public class Announcement {
    private String id;
    private String title;
    private String content;
    private long timestamp;

    // Required empty constructor for Firebase
    public Announcement() {}

    // --- THIS IS THE FIX: The constructor now includes the ID ---
    public Announcement(String id, String title, String content, long timestamp) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
    }

    // --- AND THE FIX: Getters and Setters for the ID ---
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
