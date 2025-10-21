package com.example.finalproject.model;

// This is a data model class. It represents a single attendance record.
public class AttendanceRecord {

    private String studentName;
    private String status;

    // Required empty constructor for Firebase
    public AttendanceRecord() {}

    public AttendanceRecord(String studentName, String status) {
        this.studentName = studentName;
        this.status = status;
    }

    // Getters and Setters
    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
