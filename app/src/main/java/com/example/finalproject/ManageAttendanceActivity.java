package com.example.finalproject;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
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
import java.util.List;
import java.util.Map;

public class ManageAttendanceActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SectionedAttendanceAdapter adapter;
    private List<Object> itemList; // This will hold SectionHeaders and AdminAttendanceRecords
    private List<Object> fullItemList; // For filtering
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

        recyclerView = findViewById(R.id.attendanceRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        itemList = new ArrayList<>();
        fullItemList = new ArrayList<>();
        adapter = new SectionedAttendanceAdapter(itemList);
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
            for (Object item : fullItemList) {
                if (item instanceof SectionHeader) {
                    currentHeader = (SectionHeader) item;
                } else if (item instanceof AdminAttendanceRecord) {
                    AdminAttendanceRecord record = (AdminAttendanceRecord) item;
                    boolean matches = record.studentName.toLowerCase().contains(text) || 
                                      record.status.toLowerCase().contains(text);
                    if (matches) {
                        if (currentHeader != null && !itemList.contains(currentHeader)) {
                            itemList.add(currentHeader);
                        }
                        itemList.add(record);
                    }
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void loadAllData() {
        // This is a complex, multi-step data loading process
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DataSnapshot sectionsSnap = snapshot.child("sections");
                DataSnapshot usersSnap = snapshot.child("users");
                DataSnapshot attendanceSnap = snapshot.child("attendance_by_day");

                Map<String, List<AdminAttendanceRecord>> sectionRecords = new HashMap<>();

                // 1. Prepare student map {studentUid -> {sectionName, studentName}}
                Map<String, Map<String, String>> studentInfo = new HashMap<>();
                for(DataSnapshot user : usersSnap.getChildren()) {
                    if("student".equals(user.child("role").getValue(String.class))) {
                        String section = user.child("section").getValue(String.class);
                        if(section != null) {
                            Map<String, String> info = new HashMap<>();
                            info.put("section", section);
                            info.put("name", user.child("firstName").getValue(String.class) + " " + user.child("lastName").getValue(String.class));
                            studentInfo.put(user.getKey(), info);
                        }
                    }
                }

                // 2. Iterate through all attendance records
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

                // 3. Build the final display list
                fullItemList.clear();
                for (DataSnapshot section : sectionsSnap.getChildren()) {
                    String sectionName = section.getKey();
                    String adminName = section.child("managedBy").getValue(String.class);
                    
                    fullItemList.add(new SectionHeader(sectionName + " (Admin: " + adminName + ")"));
                    
                    if (sectionRecords.containsKey(sectionName)) {
                        fullItemList.addAll(sectionRecords.get(sectionName));
                    }
                }
                
                filter(searchView.getQuery().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageAttendanceActivity.this, "Failed to load data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { onBackPressed(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
