package com.example.finalproject.superadmin;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.finalproject.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class AttendanceHistoryForStudentActivity extends AppCompatActivity {

    private ListView attendanceListView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> attendanceList;
    private DatabaseReference attendanceRef;
    private String studentUid, studentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_history_for_student);

        studentUid = getIntent().getStringExtra("STUDENT_UID");
        studentName = getIntent().getStringExtra("STUDENT_NAME");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Attendance for " + studentName);

        attendanceListView = findViewById(R.id.attendanceListView);
        attendanceList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, attendanceList);
        attendanceListView.setAdapter(adapter);

        if (studentUid != null) {
            attendanceRef = FirebaseDatabase.getInstance().getReference("attendance_by_day");
            loadAttendanceHistory(studentUid);
        }
    }

    private void loadAttendanceHistory(String studentUid) {
        attendanceRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                attendanceList.clear();
                for (DataSnapshot daySnapshot : snapshot.getChildren()) {
                    for (DataSnapshot sessionSnapshot : daySnapshot.getChildren()) {
                        if (sessionSnapshot.hasChild(studentUid)) {
                            String status = sessionSnapshot.child(studentUid).child("status").getValue(String.class);
                            String record = daySnapshot.getKey() + " - " + sessionSnapshot.getKey() + ": " + status;
                            attendanceList.add(record);
                        }
                    }
                }
                Collections.reverse(attendanceList);
                adapter.notifyDataSetChanged();
                if (attendanceList.isEmpty()) {
                    Toast.makeText(AttendanceHistoryForStudentActivity.this, "No records found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AttendanceHistoryForStudentActivity.this, "Failed to load history.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
