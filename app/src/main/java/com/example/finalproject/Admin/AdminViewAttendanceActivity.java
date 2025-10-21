package com.example.finalproject.admin;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.R;
import com.example.finalproject.adapter.AdminAttendanceAdapter;
import com.example.finalproject.model.AdminAttendanceRecord;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AdminViewAttendanceActivity extends AppCompatActivity {

    private RecyclerView attendanceRecyclerView;
    private AdminAttendanceAdapter adapter;
    private ArrayList<AdminAttendanceRecord> attendanceList;
    private DatabaseReference rootRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_view_attendance);

        rootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("My Section's Attendance");

        attendanceRecyclerView = findViewById(R.id.attendanceRecyclerView);
        attendanceRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        attendanceList = new ArrayList<>();
        adapter = new AdminAttendanceAdapter(attendanceList);
        attendanceRecyclerView.setAdapter(adapter);

        loadAdminSectionAndAttendance();
    }

    private void loadAdminSectionAndAttendance() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        rootRef.child("users").child(currentUser.getUid()).child("section").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String sectionName = snapshot.getValue(String.class);
                if (sectionName != null) {
                    loadFilteredAttendance(sectionName);
                } else {
                    Toast.makeText(AdminViewAttendanceActivity.this, "You are not assigned to a section.", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void loadFilteredAttendance(String sectionName) {
        rootRef.child("users").orderByChild("section").equalTo(sectionName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot usersSnapshot) {
                Map<String, String> sectionStudents = new HashMap<>();
                for (DataSnapshot userSnap : usersSnapshot.getChildren()) {
                    sectionStudents.put(userSnap.getKey(), userSnap.child("firstName").getValue(String.class) + " " + userSnap.child("lastName").getValue(String.class));
                }

                rootRef.child("attendance_by_day").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot attendanceSnapshot) {
                        attendanceList.clear();
                        for (DataSnapshot daySnap : attendanceSnapshot.getChildren()) {
                            for (DataSnapshot sessionSnap : daySnap.getChildren()) {
                                for (DataSnapshot studentSnap : sessionSnap.getChildren()) {
                                    String studentUid = studentSnap.getKey();
                                    if (sectionStudents.containsKey(studentUid)) {
                                        String studentName = sectionStudents.get(studentUid);
                                        String status = studentSnap.child("status").getValue(String.class);
                                        attendanceList.add(new AdminAttendanceRecord(studentUid, studentName, status, daySnap.getKey(), sessionSnap.getKey()));
                                    }
                                }
                            }
                        }
                        Collections.reverse(attendanceList);
                        adapter.notifyDataSetChanged();
                        if (attendanceList.isEmpty()) {
                            Toast.makeText(AdminViewAttendanceActivity.this, "No attendance records found for your section.", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { onBackPressed(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
