package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ManageAttendanceActivity extends AppCompatActivity {

    private ListView studentsListView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> studentDisplayList;
    private Map<String, User> studentNameToUserMap;
    private DatabaseReference rootRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // We need a simple list layout, so we can reuse activity_manage_sections which has a ListView.
        setContentView(R.layout.activity_manage_sections);

        rootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Select Student to View Attendance");

        studentsListView = findViewById(R.id.sectionsListView); // Reusing the ListView from this layout
        
        studentDisplayList = new ArrayList<>();
        studentNameToUserMap = new HashMap<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, studentDisplayList);
        studentsListView.setAdapter(adapter);

        loadAdminSectionAndStudents();

        studentsListView.setOnItemClickListener((parent, view, position, id) -> {
            String studentName = studentDisplayList.get(position);
            User selectedStudent = studentNameToUserMap.get(studentName);
            
            if (selectedStudent != null) {
                Intent intent = new Intent(this, AttendanceHistoryForStudentActivity.class);
                intent.putExtra("STUDENT_UID", selectedStudent.getUid());
                intent.putExtra("STUDENT_NAME", studentName);
                startActivity(intent);
            }
        });
    }

    private void loadAdminSectionAndStudents() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        rootRef.child("users").child(currentUser.getUid()).child("section").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String sectionName = snapshot.getValue(String.class);
                if (sectionName != null) {
                    loadStudents(sectionName);
                } else {
                    Toast.makeText(ManageAttendanceActivity.this, "You are not assigned to a section.", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void loadStudents(String sectionName) {
        rootRef.child("users").orderByChild("section").equalTo(sectionName).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                studentDisplayList.clear();
                studentNameToUserMap.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user != null && "student".equals(user.getRole())) {
                        user.setUid(userSnapshot.getKey());
                        String name = user.getFirstName() + " " + user.getLastName();
                        studentDisplayList.add(name);
                        studentNameToUserMap.put(name, user);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageAttendanceActivity.this, "Failed to load students.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { onBackPressed(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
