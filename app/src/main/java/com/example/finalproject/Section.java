package com.example.finalproject;

public class Section {
    private String name;
    private String managedBy;

    public Section() {
        // Default constructor required for calls to DataSnapshot.getValue(Section.class)
    }

    public Section(String name, String managedBy) {
        this.name = name;
        this.managedBy = managedBy;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getManagedBy() {
        return managedBy;
    }

    public void setManagedBy(String managedBy) {
        this.managedBy = managedBy;
    }
}
