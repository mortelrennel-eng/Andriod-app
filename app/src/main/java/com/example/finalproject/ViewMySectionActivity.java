package com.example.finalproject;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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

public class ViewMySectionActivity extends AppCompatActivity {

    private RecyclerView studentsRecyclerView;
    private RemovableStudentAdapter studentAdapter;
    private ArrayList<User> studentList;
    private DatabaseReference rootRef;
    private FirebaseAuth mAuth;
    private String sectionName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_my_section);

        rootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("My Section Students");

        studentsRecyclerView = findViewById(R.id.studentsRecyclerView);
        studentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        studentList = new ArrayList<>();
        studentAdapter = new RemovableStudentAdapter(studentList, this);
        studentsRecyclerView.setAdapter(studentAdapter);

        loadMySectionStudents();
    }

    private void loadMySectionStudents() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        rootRef.child("users").child(currentUser.getUid()).child("section").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                sectionName = snapshot.getValue(String.class);
                if (sectionName != null) {
                    getSupportActionBar().setTitle(sectionName + " Students");
                    loadStudents(sectionName);
                } else {
                    Toast.makeText(ViewMySectionActivity.this, "You are not assigned to a section.", Toast.LENGTH_LONG).show();
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
                Toast.makeText(ViewMySectionActivity.this, "Failed to load students.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void removeStudentFromSection(User student) {
        new AlertDialog.Builder(this)
            .setTitle("Remove Student")
            .setMessage("Are you sure you want to remove " + student.getFirstName() + " from this section?")
            .setPositiveButton("Remove", (dialog, which) -> {
                String studentUid = student.getUid();
                if (studentUid == null) {
                    Toast.makeText(this, "Cannot remove student without UID.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, Object> updates = new HashMap<>();
                // Remove student from the section's student list
                updates.put("/sections/" + sectionName + "/students/" + studentUid, null);
                // Remove the section field from the student's user profile
                updates.put("/users/" + studentUid + "/section", null);

                rootRef.updateChildren(updates).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Student removed from section.", Toast.LENGTH_SHORT).show();
                        // The ValueEventListener in loadStudents will automatically refresh the list
                    } else {
                        Toast.makeText(this, "Failed to remove student.", Toast.LENGTH_SHORT).show();
                    }
                });
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
