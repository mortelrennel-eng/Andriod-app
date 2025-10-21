package com.example.finalproject.superadmin;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.R;
import com.example.finalproject.adapter.AbsenteesAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AbsenteesListActivity extends AppCompatActivity {

    private RecyclerView absenteesRecyclerView;
    private AbsenteesAdapter adapter;
    private ArrayList<AbsenteesAdapter.AbsenteeRecord> absenteeList;
    private DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_absentees_list);

        rootRef = FirebaseDatabase.getInstance().getReference();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Students with 2+ Absences");

        absenteesRecyclerView = findViewById(R.id.absenteesRecyclerView);
        absenteesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        absenteeList = new ArrayList<>();
        adapter = new AbsenteesAdapter(absenteeList);
        absenteesRecyclerView.setAdapter(adapter);

        loadAbsentees();
    }

    private void loadAbsentees() {
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DataSnapshot usersSnap = snapshot.child("users");
                DataSnapshot attendanceSnap = snapshot.child("attendance_by_day");
                Map<String, Integer> absenceCount = new HashMap<>();
                Map<String, String> studentNames = new HashMap<>();

                for (DataSnapshot user : usersSnap.getChildren()) {
                    if ("student".equals(user.child("role").getValue(String.class))) {
                        studentNames.put(user.getKey(), user.child("firstName").getValue(String.class) + " " + user.child("lastName").getValue(String.class));
                    }
                }

                for (DataSnapshot day : attendanceSnap.getChildren()) {
                    for (DataSnapshot session : day.getChildren()) {
                        for (DataSnapshot record : session.getChildren()) {
                            String studentUid = record.getKey();
                            String status = record.child("status").getValue(String.class);
                            if ("Absent".equalsIgnoreCase(status)) {
                                absenceCount.put(studentUid, absenceCount.getOrDefault(studentUid, 0) + 1);
                            }
                        }
                    }
                }

                absenteeList.clear();
                for (Map.Entry<String, Integer> entry : absenceCount.entrySet()) {
                    if (entry.getValue() >= 2) {
                        String studentName = studentNames.get(entry.getKey());
                        if (studentName != null) {
                            absenteeList.add(new AbsenteesAdapter.AbsenteeRecord(studentName, entry.getValue()));
                        }
                    }
                }
                adapter.notifyDataSetChanged();

                if (absenteeList.isEmpty()) {
                    Toast.makeText(AbsenteesListActivity.this, "No students with 2 or more absences found.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AbsenteesListActivity.this, "Failed to load data.", Toast.LENGTH_SHORT).show();
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
