package com.example.finalproject.superadmin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
public class ManageStudentsActivity extends AppCompatActivity implements StudentAdapterListener {

    private RecyclerView studentsRecyclerView;
    private StudentAdapter studentAdapter;
    private ArrayList<User> studentList;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_students);

        usersRef = FirebaseDatabase.getInstance("https://finalproject-b08f4-default-rtdb.firebaseio.com/").getReference("users");

        studentsRecyclerView = findViewById(R.id.rvStudents);
        studentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        studentList = new ArrayList<>();
        studentAdapter = new StudentAdapter(studentList, this); // This now works because this class implements the listener
        studentsRecyclerView.setAdapter(studentAdapter);

        loadStudents();
    }

    private void loadStudents() {
        usersRef.orderByChild("role").equalTo("student").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                studentList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User student = dataSnapshot.getValue(User.class);
                    if (student != null) {
                        student.setUid(dataSnapshot.getKey());
                        studentList.add(student);
                    }
                }
                studentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageStudentsActivity.this, "Failed to load students.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // These methods are required by the StudentAdapterListener interface
    @Override
    public void deleteStudent(User student) {
        if (student.getUid() != null) {
            usersRef.child(student.getUid()).removeValue()
                .addOnSuccessListener(aVoid -> Toast.makeText(ManageStudentsActivity.this, "Student deleted successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(ManageStudentsActivity.this, "Failed to delete student.", Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public void editStudent(User student) {
        Intent intent = new Intent(this, EditStudentActivity.class);
        intent.putExtra("STUDENT_UID", student.getUid());
        startActivity(intent);
    }
}
