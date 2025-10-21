package com.example.finalproject.model;

public class Ebook {
    private String title;
    private String category;
    private String fileUrl;

    public Ebook() {}

    public Ebook(String title, String category, String fileUrl) {
        this.title = title;
        this.category = category;
        this.fileUrl = fileUrl;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
}
