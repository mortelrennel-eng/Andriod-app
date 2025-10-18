package com.example.finalproject;

public class AdminAttendanceRecord {
    public String studentUid, studentName, status, date, sessionTitle;

    public AdminAttendanceRecord(String studentUid, String studentName, String status, String date, String sessionTitle) {
        this.studentUid = studentUid;
        this.studentName = studentName;
        this.status = status;
        this.date = date;
        this.sessionTitle = sessionTitle;
    }
}
