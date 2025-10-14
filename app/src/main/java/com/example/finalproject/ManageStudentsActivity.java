package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class ManageStudentsActivity extends AppCompatActivity {
    private static final String TAG = "ManageStudents";
    private ListView lvStudents;
    private ProgressBar progressBar;
    private ArrayList<String> studentNames;
    private ArrayList<String> studentUids;
    private ArrayAdapter<String> adapter;

    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_students);

        lvStudents = findViewById(R.id.lvStudents);
        progressBar = findViewById(R.id.progressStudents);

        studentNames = new ArrayList<>();
        studentUids = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, studentNames);
        lvStudents.setAdapter(adapter);

        usersRef = FirebaseDatabase.getInstance("https://finalproject-b08f4-default-rtdb.firebaseio.com/").getReference("users");

        lvStudents.setOnItemClickListener((parent, view, position, id) -> {
            String uid = studentUids.get(position);
            // open simple profile view (reuse existing or implement quick view)
            Intent intent = new Intent(ManageStudentsActivity.this, StudentProfileActivity.class);
            intent.putExtra("uid", uid);
            startActivity(intent);
        });

        loadStudents();
    }

    private void loadStudents() {
        progressBar.setVisibility(View.VISIBLE);
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                studentNames.clear();
                studentUids.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    String role = child.child("role").getValue(String.class);
                    if ("student".equals(role)) {
                        String fn = child.child("firstName").getValue(String.class);
                        String ln = child.child("lastName").getValue(String.class);
                        String name = (fn != null ? fn : "") + " " + (ln != null ? ln : "");
                        if (name.trim().isEmpty()) name = child.child("name").getValue(String.class);
                        if (name == null) name = child.child("email").getValue(String.class);
                        studentNames.add(name != null ? name : "(no name)");
                        studentUids.add(child.getKey());
                    }
                }
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "loadStudents:onCancelled", error.toException());
                Toast.makeText(ManageStudentsActivity.this, "Failed to load students.", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}
