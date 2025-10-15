package com.example.finalproject;

import java.io.Serializable;

public class User implements Serializable { // Make sure it implements Serializable

    private String uid;
    private String firstName;
    private String lastName;
    private String studentId;
    private String email;
    private String contactNumber;
    private String parentName;
    private String parentContactNumber;
    private String role;
    private String section;
    private String qrCodeUrl;

    // Default constructor is needed for Firebase
    public User() {}

    // Getters and setters for all fields

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

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

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public String getQrCodeUrl() { return qrCodeUrl; }
    public void setQrCodeUrl(String qrCodeUrl) { this.qrCodeUrl = qrCodeUrl; }
}
