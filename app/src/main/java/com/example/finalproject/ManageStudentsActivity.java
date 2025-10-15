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

public class ManageStudentsActivity extends AppCompatActivity {
    private static final String TAG = "ManageStudents";
    private ListView lvStudents;
    private ProgressBar progressBar;
    private ArrayList<String> studentNames;
    private ArrayList<String> studentUids;
    private ArrayAdapter<String> adapter;
    private android.widget.TextView tvAssignedSection;
    private android.widget.Button btnChangeSection;

    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_students);

        lvStudents = findViewById(R.id.lvStudents);
        progressBar = findViewById(R.id.progressStudents);
    tvAssignedSection = findViewById(R.id.tvAssignedSection);
    btnChangeSection = findViewById(R.id.btnChangeSection);

        studentNames = new ArrayList<>();
        studentUids = new ArrayList<>();
        adapter = new ArrayAdapter<String>(this, R.layout.item_student_row, R.id.tvStudentNameRow, studentNames) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                android.widget.Button btnView = v.findViewById(R.id.btnViewStudent);
                final String uid = studentUids.get(position);
                btnView.setOnClickListener(view -> {
                    Intent intent = new Intent(ManageStudentsActivity.this, StudentProfileActivity.class);
                    intent.putExtra("uid", uid);
                    startActivityForResult(intent, 1001);
                });
                return v;
            }
        };
        lvStudents.setAdapter(adapter);

        usersRef = FirebaseDatabase.getInstance("https://finalproject-b08f4-default-rtdb.firebaseio.com/").getReference("users");

        btnChangeSection.setOnClickListener(v -> {
            // open simple picker from sections node
            DatabaseReference sectionsRef = FirebaseDatabase.getInstance("https://finalproject-b08f4-default-rtdb.firebaseio.com/").getReference("sections");
            sectionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot sectionsSnap) {
                    ArrayList<String> items = new ArrayList<>();
                    for (DataSnapshot s : sectionsSnap.getChildren()) items.add(s.getKey());
                    if (items.isEmpty()) {
                        Toast.makeText(ManageStudentsActivity.this, "No sections available.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String[] arr = items.toArray(new String[0]);
                    new androidx.appcompat.app.AlertDialog.Builder(ManageStudentsActivity.this)
                            .setTitle("Choose section")
                            .setItems(arr, (dialog, which) -> {
                                String chosen = arr[which];
                                String currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
                                usersRef.child(currentUid).child("section").setValue(chosen).addOnSuccessListener(aVoid -> {
                                    Toast.makeText(ManageStudentsActivity.this, "Section updated.", Toast.LENGTH_SHORT).show();
                                    tvAssignedSection.setText("Section: " + chosen);
                                    // refresh list for new section
                                    loadStudents();
                                }).addOnFailureListener(e -> Toast.makeText(ManageStudentsActivity.this, "Failed to update section.", Toast.LENGTH_SHORT).show());
                            }).show();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ManageStudentsActivity.this, "Failed to load sections.", Toast.LENGTH_SHORT).show();
                }
            });
        });

        lvStudents.setOnItemClickListener((parent, view, position, id) -> {
            String uid = studentUids.get(position);
            Intent intent = new Intent(ManageStudentsActivity.this, StudentProfileActivity.class);
            intent.putExtra("uid", uid);
            startActivityForResult(intent, 1001);
        });

        // load students when the activity starts
        loadStudents();

    }

    private void loadAllUsers(String role, String adminSection) {
        progressBar.setVisibility(View.VISIBLE);
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                studentNames.clear();
                studentUids.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    String r = child.child("role").getValue(String.class);
                    if (r == null) {
                        Log.d(TAG, "Skipping user " + child.getKey() + " because role is null");
                        continue;
                    }
                    if (!r.equalsIgnoreCase("student")) continue;
                    // if current user is admin, only include students in their section
                    if ("admin".equals(role)) {
                        if (adminSection == null || adminSection.trim().isEmpty()) {
                            Log.w(TAG, "Admin user has no section assigned");
                            continue;
                        }
                        String studentSection = child.child("section").getValue(String.class);
                        if (studentSection == null || !studentSection.equals(adminSection)) {
                            Log.d(TAG, "Skipping student " + child.getKey() + " because section mismatch");
                            continue;
                        }
                    }
                    String fn = child.child("firstName").getValue(String.class);
                    String ln = child.child("lastName").getValue(String.class);
                    String name = (fn != null ? fn : "") + " " + (ln != null ? ln : "");
                    if (name.trim().isEmpty()) name = child.child("name").getValue(String.class);
                    if (name == null) name = child.child("email").getValue(String.class);
                    studentNames.add(name != null ? name : "(no name)");
                    studentUids.add(child.getKey());
                }
                adapter.notifyDataSetChanged();
                if ("admin".equals(role) && studentNames.isEmpty()) {
                    Toast.makeText(ManageStudentsActivity.this, "No students found for your assigned section.", Toast.LENGTH_LONG).show();
                }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            // profile updated, refresh list
            loadStudents();
        }
    }

    private void loadStudents() {
        progressBar.setVisibility(View.VISIBLE);
        // check current user's role and section to filter if admin
        String currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null ?
                com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (currentUid == null) {
            Toast.makeText(this, "Not signed in", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        usersRef.child(currentUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot currentUserSnap) {
                String role = currentUserSnap.child("role").getValue(String.class);
                String adminSection = currentUserSnap.child("section").getValue(String.class);
                final String roleLower = role != null ? role.toLowerCase() : null;
                final String adminSectionFinal = adminSection;
                final String currentUidFinal = currentUid;

                // helpful logs for debugging
                Log.d(TAG, "Current user role=" + roleLower + " section=" + adminSectionFinal);

                // update header
                if (adminSectionFinal != null && !adminSectionFinal.trim().isEmpty()) {
                    tvAssignedSection.setText("Section: " + adminSectionFinal);
                } else {
                    tvAssignedSection.setText("Section: (not assigned)");
                }
                // If current user is admin but has no section assigned in their user node,
                // try to find a section where they are the assigned admin.
                if ("admin".equals(roleLower) && (adminSectionFinal == null || adminSectionFinal.trim().isEmpty())) {
                    DatabaseReference sectionsRef = FirebaseDatabase.getInstance("https://finalproject-b08f4-default-rtdb.firebaseio.com/").getReference("sections");
                    sectionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot sectionsSnap) {
                            String foundSection = null;
                            for (DataSnapshot s : sectionsSnap.getChildren()) {
                                String adminUid = s.child("adminUid").getValue(String.class);
                                if (currentUidFinal.equals(adminUid)) {
                                    foundSection = s.getKey();
                                    break;
                                }
                            }
                            if (foundSection != null) {
                                Log.d(TAG, "Found admin section via sections node: " + foundSection);
                            }
                            // proceed to load users with foundSection (may be null)
                            loadAllUsers(roleLower, foundSection);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.w(TAG, "sections lookup cancelled", error.toException());
                            // fallback to loading users with whatever adminSection is (null)
                            loadAllUsers(roleLower, adminSectionFinal);
                        }
                    });
                } else {
                    // normal path
                    loadAllUsers(roleLower, adminSectionFinal);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "loadStudents:getCurrentUser:onCancelled", error.toException());
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}
