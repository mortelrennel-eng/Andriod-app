package com.example.finalproject;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ManageAttendanceActivity extends AppCompatActivity {

    private ListView attendanceListView;
    private ArrayList<String> attendanceList;
    private ArrayAdapter<String> attendanceAdapter;

    private FirebaseDatabase realtimeDb;
    private DatabaseReference attendanceRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_attendance);

        realtimeDb = FirebaseDatabase.getInstance("https://finalproject-b08f4-default-rtdb.firebaseio.com/");
        attendanceRef = realtimeDb.getReference("attendance");

        attendanceListView = findViewById(R.id.attendanceListView);
        attendanceList = new ArrayList<>();
        attendanceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, attendanceList);
        attendanceListView.setAdapter(attendanceAdapter);

        loadAllAttendance();
    }

    private void loadAllAttendance() {
        attendanceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                attendanceList.clear();
                for (DataSnapshot studentSnap : snapshot.getChildren()) {
                    String studentUid = studentSnap.getKey();
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
                Toast.makeText(ManageAttendanceActivity.this, "Failed to load attendance.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
