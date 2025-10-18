package com.example.finalproject;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ViewAttendanceActivity extends AppCompatActivity {

    private RecyclerView attendanceRecyclerView;
    private AttendanceAdapter attendanceAdapter;
    private ArrayList<String> attendanceList;

    private FirebaseDatabase realtimeDb;
    private DatabaseReference attendanceRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_attendance);

        realtimeDb = FirebaseDatabase.getInstance("https://finalproject-b08f4-default-rtdb.firebaseio.com/");
        attendanceRef = realtimeDb.getReference("attendance");

        attendanceRecyclerView = findViewById(R.id.rvAttendanceRecords);
        attendanceRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        attendanceList = new ArrayList<>();

        // Initialize the adapter and set it to the RecyclerView
        attendanceAdapter = new AttendanceAdapter(attendanceList);
        attendanceRecyclerView.setAdapter(attendanceAdapter);

        loadAttendanceRecords();
    }

    private void loadAttendanceRecords() {
        attendanceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                attendanceList.clear();
                for (DataSnapshot studentSnap : snapshot.getChildren()) {
                    // Assuming each student has multiple records, iterate through them
                    for (DataSnapshot recordSnap : studentSnap.getChildren()) {
                        String studentName = recordSnap.child("studentName").getValue(String.class);
                        String status = recordSnap.child("status").getValue(String.class);
                        String dateStr = recordSnap.child("dateStr").getValue(String.class);
                        
                        String record = (studentName != null ? studentName : "Unknown") + " - " +
                                        (status != null ? status : "") + " - " +
                                        (dateStr != null ? dateStr : "");
                        attendanceList.add(record);
                    }
                }
                attendanceAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ViewAttendanceActivity.this, "Failed to load attendance.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
