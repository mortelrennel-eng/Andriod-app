package com.example.finalproject;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class AddStudentsToSectionActivity extends AppCompatActivity {

    private ListView studentsListView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> studentList;
    private ArrayList<String> studentIds;
    private String sectionName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_students_to_section);

        studentsListView = findViewById(R.id.studentsListView);
        studentList = new ArrayList<>();
        studentIds = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, studentList);
        studentsListView.setAdapter(adapter);

        sectionName = getIntent().getStringExtra("SECTION_NAME");

        loadStudentsWithoutSection();

        studentsListView.setOnItemClickListener((parent, view, position, id) -> {
            String studentId = studentIds.get(position);
            addStudentToSection(studentId);
        });
    }

    private void loadStudentsWithoutSection() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance("https://finalproject-b08f4-default-rtdb.firebaseio.com/").getReference("users");
        usersRef.orderByChild("role").equalTo("student").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                studentList.clear();
                studentIds.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    if (!userSnapshot.hasChild("section")) {
                        String name = userSnapshot.child("firstName").getValue(String.class) + " " + userSnapshot.child("lastName").getValue(String.class);
                        studentList.add(name);
                        studentIds.add(userSnapshot.getKey());
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddStudentsToSectionActivity.this, "Failed to load students.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addStudentToSection(String studentId) {
        DatabaseReference sectionRef = FirebaseDatabase.getInstance("https://finalproject-b08f4-default-rtdb.firebaseio.com/").getReference("sections").child(sectionName).child("students");
        sectionRef.child(studentId).setValue(true);

        DatabaseReference userRef = FirebaseDatabase.getInstance("https://finalproject-b08f4-default-rtdb.firebaseio.com/").getReference("users").child(studentId);
        userRef.child("section").setValue(sectionName);

        Toast.makeText(this, "Student added to section.", Toast.LENGTH_SHORT).show();
        finish();
    }
}
