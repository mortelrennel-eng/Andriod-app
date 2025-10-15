package com.example.finalproject;

import android.content.Intent;
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

public class StudentListBySectionActivity extends AppCompatActivity {

    private RecyclerView studentsRecyclerView;
    private StudentAdapter studentAdapter;
    private ArrayList<User> studentList;
    private String sectionName;
    private DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_list_by_section);

        rootRef = FirebaseDatabase.getInstance().getReference();
        sectionName = getIntent().getStringExtra("SECTION_NAME");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Students in " + sectionName);

        studentsRecyclerView = findViewById(R.id.studentsRecyclerView);
        studentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        studentList = new ArrayList<>();
        studentAdapter = new StudentAdapter(studentList, this);
        studentsRecyclerView.setAdapter(studentAdapter);

        loadStudents();
    }

    private void loadStudents() {
        rootRef.child("users").orderByChild("section").equalTo(sectionName).addValueEventListener(new ValueEventListener() {
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
    
    public void viewStudent(User student) {
        // ... (This logic is correct)
    }
    
    public void editStudent(User student) {
        Intent intent = new Intent(this, EditStudentActivity.class);
        intent.putExtra("STUDENT_DATA", student);
        startActivity(intent);
    }
    
    public void deleteStudent(User student) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Student")
            .setMessage("Are you sure you want to delete " + student.getFirstName() + "? This will remove them from the database and section.")
            .setPositiveButton("Delete", (dialog, which) -> {
                String studentUid = student.getUid();
                if (studentUid == null) {
                    Toast.makeText(this, "Cannot delete student without UID.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Remove student from the section list
                rootRef.child("sections").child(sectionName).child("students").child(studentUid).removeValue();

                // Remove student from the users node
                rootRef.child("users").child(studentUid).removeValue();

                // NOTE: This does NOT delete the user from Firebase Authentication.
                // That is a more complex process requiring re-authentication.

                Toast.makeText(this, "Student deleted.", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
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
