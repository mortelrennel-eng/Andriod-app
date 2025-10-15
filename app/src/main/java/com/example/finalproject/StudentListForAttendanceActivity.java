package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
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
    private Map<String, String> studentNameToUidMap;
    private String sectionName;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_list_for_attendance);

        sectionName = getIntent().getStringExtra("SECTION_NAME");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Select Student");

        searchView = findViewById(R.id.searchView);
        studentsListView = findViewById(R.id.studentsListView);
        
        studentDisplayList = new ArrayList<>();
        studentNameToUidMap = new HashMap<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, studentDisplayList);
        studentsListView.setAdapter(adapter);

        setupSearchView();
        loadStudents();

        studentsListView.setOnItemClickListener((parent, view, position, id) -> {
            String studentName = (String) parent.getItemAtPosition(position);
            String studentUid = studentNameToUidMap.get(studentName);

            Intent intent = new Intent(this, AttendanceHistoryForStudentActivity.class);
            intent.putExtra("STUDENT_UID", studentUid);
            intent.putExtra("STUDENT_NAME", studentName);
            startActivity(intent);
        });
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return true;
            }
        });
    }

    private void loadStudents() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.orderByChild("section").equalTo(sectionName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                studentDisplayList.clear();
                studentNameToUidMap.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    if ("student".equals(userSnapshot.child("role").getValue(String.class))) {
                        String uid = userSnapshot.getKey();
                        String name = userSnapshot.child("firstName").getValue(String.class) + " " + userSnapshot.child("lastName").getValue(String.class);
                        studentDisplayList.add(name);
                        studentNameToUidMap.put(name, uid);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StudentListForAttendanceActivity.this, "Failed to load students.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { onBackPressed(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
