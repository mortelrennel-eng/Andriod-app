package com.example.finalproject;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;

public class AttendanceHistoryForStudentActivity extends AppCompatActivity {

    public static class StudentAttendanceRecord {
        public String date, sessionTitle, status;
        public StudentAttendanceRecord(String date, String sessionTitle, String status) { this.date = date; this.sessionTitle = sessionTitle; this.status = status; }
    }

    private RecyclerView historyRecyclerView;
    private StudentHistoryAdapter adapter;
    private ArrayList<StudentAttendanceRecord> historyList;
    private String studentUid, studentName;
    private DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_history_for_student);

        studentUid = getIntent().getStringExtra("STUDENT_UID");
        studentName = getIntent().getStringExtra("STUDENT_NAME");
        rootRef = FirebaseDatabase.getInstance().getReference();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(studentName);

        historyRecyclerView = findViewById(R.id.historyRecyclerView);
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        historyList = new ArrayList<>();
        adapter = new StudentHistoryAdapter(historyList, this);
        historyRecyclerView.setAdapter(adapter);

        loadHistory();
    }

    private void loadHistory() {
        rootRef.child("attendance_by_day").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                historyList.clear();
                for (DataSnapshot daySnap : snapshot.getChildren()) {
                    for (DataSnapshot sessionSnap : daySnap.getChildren()) {
                        if (sessionSnap.hasChild(studentUid)) {
                            String date = daySnap.getKey();
                            String title = sessionSnap.getKey();
                            String status = sessionSnap.child(studentUid).child("status").getValue(String.class);
                            historyList.add(new StudentAttendanceRecord(date, title, status));
                        }
                    }
                }
                Collections.reverse(historyList);
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
    
    public void editRecord(StudentAttendanceRecord record) {
        final String[] statuses = {"On-Time", "Late", "Absent"};
        new AlertDialog.Builder(this)
            .setTitle("Update Status")
            .setItems(statuses, (dialog, which) -> {
                String newStatus = statuses[which];
                DatabaseReference recordRef = rootRef.child("attendance_by_day")
                    .child(record.date)
                    .child(record.sessionTitle)
                    .child(studentUid)
                    .child("status");
                
                recordRef.setValue(newStatus).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Status updated to " + newStatus, Toast.LENGTH_SHORT).show();
                        // The ValueEventListener will automatically refresh the list.
                    } else {
                        Toast.makeText(this, "Failed to update status.", Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { onBackPressed(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
