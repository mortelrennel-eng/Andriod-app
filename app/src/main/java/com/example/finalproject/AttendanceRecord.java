package com.example.finalproject;

// This is now its own class, making it easier to use across the app.
public class AttendanceRecord {
    private String studentName;
    private String status;

    // Required empty public constructor for Firebase
    public AttendanceRecord() {}

    public AttendanceRecord(String studentName, String status) {
        this.studentName = studentName;
        this.status = status;
    }

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
