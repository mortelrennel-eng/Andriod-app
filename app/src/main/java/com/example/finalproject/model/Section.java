package com.example.finalproject.model;

public class Section {
    private String sectionName;
    private String managedBy;

    public Section() {}

    public Section(String sectionName, String managedBy) {
        this.sectionName = sectionName;
        this.managedBy = managedBy;
    }

    public String getSectionName() { return sectionName; }
    public void setSectionName(String sectionName) { this.sectionName = sectionName; }
    public String getManagedBy() { return managedBy; }
    public void setManagedBy(String managedBy) { this.managedBy = managedBy; }
}
