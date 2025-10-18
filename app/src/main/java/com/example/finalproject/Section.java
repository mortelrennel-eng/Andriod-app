package com.example.finalproject;

public class Section {
    private String sectionName;
    private String managedBy;

    public Section() {
        // Default constructor required for calls to DataSnapshot.getValue(Section.class)
    }

    public Section(String sectionName, String managedBy) {
        this.sectionName = sectionName;
        this.managedBy = managedBy;
    }

    // --- GETTERS AND SETTERS --- GETTERS ARE NEEDED BY THE ADAPTER ---

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public String getManagedBy() {
        return managedBy;
    }

    public void setManagedBy(String managedBy) {
        this.managedBy = managedBy;
    }
}
