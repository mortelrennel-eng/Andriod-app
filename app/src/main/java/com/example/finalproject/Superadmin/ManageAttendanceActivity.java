package com.example.finalproject.superadmin;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.R;
import com.example.finalproject.adapter.SectionedAttendanceAdapter;
import com.example.finalproject.model.AdminAttendanceRecord;
import com.example.finalproject.model.SectionHeader;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageAttendanceActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SectionedAttendanceAdapter adapter;
    private List<Object> itemList;
    private List<Object> fullItemList;
    private SearchView searchView;
    private DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_attendance);

        rootRef = FirebaseDatabase.getInstance().getReference();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("All Attendance Records");

        recyclerView = findViewById(R.id.attendanceRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        itemList = new ArrayList<>();
        fullItemList = new ArrayList<>();
        adapter = new SectionedAttendanceAdapter(itemList, this);
        recyclerView.setAdapter(adapter);

        searchView = findViewById(R.id.searchView);
        setupSearchView();

        loadAllData();
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
        itemList.clear();
        if (text.isEmpty()) {
            itemList.addAll(fullItemList);
        } else {
            text = text.toLowerCase();
            SectionHeader currentHeader = null;
            boolean headerAdded = false;
            for (Object item : fullItemList) {
                if (item instanceof SectionHeader) {
                    currentHeader = (SectionHeader) item;
                    headerAdded = false;
                } else if (item instanceof AdminAttendanceRecord) {
                    AdminAttendanceRecord record = (AdminAttendanceRecord) item;
                    boolean matches = record.studentName.toLowerCase().contains(text) ||
                                      (currentHeader != null && currentHeader.getTitle().toLowerCase().contains(text)) ||
                                      record.status.toLowerCase().contains(text);
                    if (matches) {
                        if (currentHeader != null && !headerAdded) {
                            itemList.add(currentHeader);
                            headerAdded = true;
                        }
                        itemList.add(record);
                    }
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void loadAllData() {
        // Using addListenerForSingleValueEvent for a clean, one-time data load
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DataSnapshot sectionsSnap = snapshot.child("sections");
                DataSnapshot usersSnap = snapshot.child("users");
                DataSnapshot attendanceSnap = snapshot.child("attendance_by_day");

                if (!attendanceSnap.exists()) {
                    Toast.makeText(ManageAttendanceActivity.this, "No attendance records found.", Toast.LENGTH_LONG).show();
                    return;
                }

                Map<String, List<AdminAttendanceRecord>> sectionRecords = new HashMap<>();
                Map<String, Map<String, String>> studentInfo = new HashMap<>();

                for (DataSnapshot user : usersSnap.getChildren()) {
                    if ("student".equals(user.child("role").getValue(String.class))) {
                        String section = user.child("section").getValue(String.class);
                        if (section != null) {
                            Map<String, String> info = new HashMap<>();
                            info.put("section", section);
                            info.put("name", user.child("firstName").getValue(String.class) + " " + user.child("lastName").getValue(String.class));
                            studentInfo.put(user.getKey(), info);
                        }
                    }
                }

                for (DataSnapshot day : attendanceSnap.getChildren()) {
                    for (DataSnapshot session : day.getChildren()) {
                        for (DataSnapshot record : session.getChildren()) {
                            String studentUid = record.getKey();
                            if (studentInfo.containsKey(studentUid)) {
                                Map<String, String> info = studentInfo.get(studentUid);
                                String sectionName = info.get("section");
                                String studentName = info.get("name");
                                String status = record.child("status").getValue(String.class);
                                AdminAttendanceRecord newRecord = new AdminAttendanceRecord(studentUid, studentName, status, day.getKey(), session.getKey());

                                if (!sectionRecords.containsKey(sectionName)) {
                                    sectionRecords.put(sectionName, new ArrayList<>());
                                }
                                sectionRecords.get(sectionName).add(newRecord);
                            }
                        }
                    }
                }

                fullItemList.clear();
                for (DataSnapshot section : sectionsSnap.getChildren()) {
                    String sectionName = section.getKey();
                    String adminName = section.child("managedBy").getValue(String.class);

                    fullItemList.add(new SectionHeader(sectionName + " (Admin: " + (adminName != null ? adminName : "N/A") + ")"));

                    if (sectionRecords.containsKey(sectionName)) {
                        List<AdminAttendanceRecord> records = sectionRecords.get(sectionName);
                        Collections.reverse(records); 
                        fullItemList.addAll(records);
                    }
                }
                
                if (fullItemList.isEmpty()) {
                    Toast.makeText(ManageAttendanceActivity.this, "No records to display. Sections might be empty.", Toast.LENGTH_LONG).show();
                }
                filter(searchView.getQuery().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageAttendanceActivity.this, "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void editAttendance(AdminAttendanceRecord record) {
        final String[] statuses = {"On-Time", "Late", "Absent"};
        new AlertDialog.Builder(this)
                .setTitle("Update Status for " + record.studentName)
                .setItems(statuses, (dialog, which) -> {
                    String newStatus = statuses[which];
                    DatabaseReference recordRef = rootRef.child("attendance_by_day").child(record.date).child(record.sessionTitle).child(record.studentUid).child("status");

                    recordRef.setValue(newStatus).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Status updated to " + newStatus, Toast.LENGTH_SHORT).show();
                            loadAllData(); 
                        } else {
                            Toast.makeText(this, "Failed to update status.", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .show();
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
