package com.example.finalproject.superadmin;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.R;
import com.example.finalproject.adapter.StudentAdapter;
import com.example.finalproject.adapter.StudentAdapterListener;
import com.example.finalproject.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

// --- THIS IS THE FIX: Implementing the listener interface ---
public class StudentListBySectionActivity extends AppCompatActivity implements StudentAdapterListener {

    private RecyclerView studentsRecyclerView;
    private StudentAdapter studentAdapter;
    private ArrayList<User> studentList;
    private String sectionName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_list_by_section);

        sectionName = getIntent().getStringExtra("SECTION_NAME");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(sectionName + " Students");

        studentsRecyclerView = findViewById(R.id.studentsRecyclerView);
        studentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        studentList = new ArrayList<>();
        studentAdapter = new StudentAdapter(studentList, this); // This now works because this class implements the listener
        studentsRecyclerView.setAdapter(studentAdapter);

        loadStudents();
    }

    private void loadStudents() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.orderByChild("section").equalTo(sectionName).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                studentList.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user != null && "student".equals(user.getRole())) {
                        user.setUid(userSnapshot.getKey());
                        studentList.add(user);
                    }
                }
                studentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StudentListBySectionActivity.this, "Failed to load students.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    // These methods are required by the StudentAdapterListener interface
    @Override
    public void deleteStudent(User student) {
        if (student.getUid() != null) {
            FirebaseDatabase.getInstance().getReference("users").child(student.getUid()).removeValue();
        }
    }

    @Override
    public void editStudent(User student) {
        Intent intent = new Intent(this, com.example.finalproject.superadmin.EditStudentActivity.class);
        intent.putExtra("STUDENT_UID", student.getUid());
        startActivity(intent);
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
