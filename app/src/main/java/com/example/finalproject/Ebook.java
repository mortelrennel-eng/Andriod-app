package com.example.finalproject;

public class Ebook {
    private String key;
    private String title;
    private String category;
    private String fileUrl;

    public Ebook() {
        // Default constructor required for calls to DataSnapshot.getValue(Ebook.class)
    }

    public Ebook(String key, String title, String category, String fileUrl) {
        this.key = key;
        this.title = title;
        this.category = category;
        this.fileUrl = fileUrl;
    }

    // Getters and Setters
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }
}
