package com.example.finalproject;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
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
import java.util.HashMap;
import java.util.Map;

public class AttendanceListBySectionActivity extends AppCompatActivity {

    private static final String TAG = "AttendanceListBySection";

    public static class AttendanceRecord {
        public String studentName, status, date, sessionTitle, studentUid;
        public AttendanceRecord(String studentUid, String studentName, String status, String date, String sessionTitle) {
            this.studentUid = studentUid;
            this.studentName = studentName;
            this.status = status;
            this.date = date;
            this.sessionTitle = sessionTitle;
        }
        public String getStudentUid() { return studentUid; }
        public String getStudentName() { return studentName; }
        public String getStatus() { return status; }
        public String getDate() { return date; }
        public String getSessionTitle() { return sessionTitle; }
    }

    private RecyclerView attendanceRecyclerView;
    private SectionAttendanceDetailAdapter attendanceAdapter;
    private ArrayList<AttendanceRecord> fullList;
    private ArrayList<AttendanceRecord> filteredList;
    private String sectionName;
    private DatabaseReference rootRef;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_list_by_section);

        sectionName = getIntent().getStringExtra("SECTION_NAME");
        rootRef = FirebaseDatabase.getInstance().getReference();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Attendance for " + sectionName);

        TextView tvManagedBy = findViewById(R.id.tvManagedBy);
        searchView = findViewById(R.id.searchView);
        attendanceRecyclerView = findViewById(R.id.attendanceRecyclerView);
        attendanceRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        fullList = new ArrayList<>();
        filteredList = new ArrayList<>();
        attendanceAdapter = new SectionAttendanceDetailAdapter(filteredList, this);
        attendanceRecyclerView.setAdapter(attendanceAdapter);

        setupSearchView();
        loadSectionDetails(tvManagedBy);
        loadAttendanceForSection();
    }
    
    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });
    }

    private void filter(String text) {
        filteredList.clear();
        if (text.isEmpty()) {
            filteredList.addAll(fullList);
        } else {
            text = text.toLowerCase();
            for (AttendanceRecord record : fullList) {
                if (record.getStudentName().toLowerCase().contains(text) || record.getSessionTitle().toLowerCase().contains(text)) {
                    filteredList.add(record);
                }
            }
        }
        attendanceAdapter.notifyDataSetChanged();
    }

    private void loadSectionDetails(TextView tvManagedBy) { /* ... same as before ... */ }
    private void loadAttendanceForSection() { /* ... same as before, but populates fullList ... */ }

    public void editAttendance(AttendanceRecord record) {
        final String[] statuses = {"On-Time", "Late", "Absent"};

        new AlertDialog.Builder(this)
            .setTitle("Update Status for " + record.getStudentName())
            .setItems(statuses, (dialog, which) -> {
                String newStatus = statuses[which];
                DatabaseReference recordRef = rootRef.child("attendance_by_day")
                    .child(record.getDate())
                    .child(record.getSessionTitle())
                    .child(record.getStudentUid())
                    .child("status");
                
                recordRef.setValue(newStatus).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Status updated to " + newStatus, Toast.LENGTH_SHORT).show();
                        loadAttendanceForSection(); // Refresh the list
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
