package com.example.finalproject.superadmin;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.finalproject.R;
import com.example.finalproject.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StudentListForAttendanceActivity extends AppCompatActivity {

    private ListView studentsListView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> studentDisplayList;
    private Map<String, User> studentNameToUserMap;
    private String sectionName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_list_for_attendance);

        sectionName = getIntent().getStringExtra("SECTION_NAME");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Students in " + sectionName);

        studentsListView = findViewById(R.id.studentsListView);
        studentDisplayList = new ArrayList<>();
        studentNameToUserMap = new HashMap<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, studentDisplayList);
        studentsListView.setAdapter(adapter);

        loadStudents();

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

    private void loadStudents() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.orderByChild("section").equalTo(sectionName).addValueEventListener(new ValueEventListener() {
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
                // Handle error
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
