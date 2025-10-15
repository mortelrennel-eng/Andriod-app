package com.example.finalproject;

public class Admin {
    private String name;
    private String email;
    // Add any other fields relevant to an admin user

    // Default constructor required for calls to DataSnapshot.getValue(Admin.class)
    public Admin() {
    }

    public Admin(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
