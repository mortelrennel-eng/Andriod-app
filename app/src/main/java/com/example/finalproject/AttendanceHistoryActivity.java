package com.example.finalproject;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;

public class AttendanceHistoryActivity extends AppCompatActivity {

    public static class HistoryItem {
        private String date, title, timeIn, timeOut, status;

        public HistoryItem(String date, String title, String timeIn, String timeOut, String status) {
            this.date = date;
            this.title = title;
            this.timeIn = timeIn;
            this.timeOut = timeOut;
            this.status = status;
        }

        public String getDate() { return date; }
        public String getTitle() { return title; }
        public String getTimeIn() { return timeIn; }
        public String getTimeOut() { return timeOut; }
        public String getStatus() { return status; }
    }

    private RecyclerView historyRecyclerView;
    private HistoryAdapter adapter;
    private ArrayList<HistoryItem> historyList;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_history);

        historyRecyclerView = findViewById(R.id.rvAttendanceHistory);
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        historyList = new ArrayList<>();
        adapter = new HistoryAdapter(historyList);
        historyRecyclerView.setAdapter(adapter);

        mAuth = FirebaseAuth.getInstance();

        loadAttendanceHistory();
    }

    private void loadAttendanceHistory() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in to view history.", Toast.LENGTH_SHORT).show();
            return;
        }
        String studentUid = currentUser.getUid();

        DatabaseReference attendanceRef = FirebaseDatabase.getInstance("https://finalproject-b08f4-default-rtdb.firebaseio.com/").getReference("attendance_by_day");

        attendanceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                historyList.clear();
                if (!snapshot.exists()) {
                    Toast.makeText(AttendanceHistoryActivity.this, "No attendance history found.", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (DataSnapshot daySnapshot : snapshot.getChildren()) {
                    String date = daySnapshot.getKey();
                    for (DataSnapshot titleSnapshot : daySnapshot.getChildren()) {
                        String title = titleSnapshot.getKey();
                        if (titleSnapshot.hasChild(studentUid)) {
                            DataSnapshot record = titleSnapshot.child(studentUid);
                            String timeIn = record.child("time_in").getValue(String.class);
                            String timeOut = record.child("time_out").getValue(String.class);
                            String status = record.child("status").getValue(String.class);
                            historyList.add(new HistoryItem(date, title, timeIn, timeOut != null ? timeOut : "--:--", status));
                        }
                    }
                }

                Collections.reverse(historyList);
                adapter.notifyDataSetChanged();

                if (historyList.isEmpty()) {
                    Toast.makeText(AttendanceHistoryActivity.this, "No attendance records found for you.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AttendanceHistoryActivity.this, "Failed to load history.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
