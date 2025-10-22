package com.example.finalproject.model;

public class User {
    private String firstName, lastName, studentId, gender, email, contactNumber, parentName, parentContactNumber, role, uid, section;

    // Required empty constructor for Firebase
    public User() {}

    // --- THIS IS THE FIX: The missing all-arguments constructor ---
    public User(String firstName, String lastName, String studentId, String gender, String email, String contactNumber, String parentName, String parentContactNumber, String role, String uid) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.studentId = studentId;
        this.gender = gender;
        this.email = email;
        this.contactNumber = contactNumber;
        this.parentName = parentName;
        this.parentContactNumber = parentContactNumber;
        this.role = role;
        this.uid = uid;
        this.section = null; // Default to null
    }

    // Getters and Setters for all fields...
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
    public String getParentName() { return parentName; }
    public void setParentName(String parentName) { this.parentName = parentName; }
    public String getParentContactNumber() { return parentContactNumber; }
    public void setParentContactNumber(String parentContactNumber) { this.parentContactNumber = parentContactNumber; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }
}
