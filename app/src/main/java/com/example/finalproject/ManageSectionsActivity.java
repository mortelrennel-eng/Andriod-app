package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageSectionsActivity extends AppCompatActivity {

    private RecyclerView sectionsRecyclerView;
    private SectionAdapter sectionAdapter;
    private ArrayList<Section> sectionList;
    private Map<String, String> adminNameToUidMap;
    private DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_sections);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Manage Sections");

        adminNameToUidMap = new HashMap<>();
        rootRef = FirebaseDatabase.getInstance("https://finalproject-b08f4-default-rtdb.firebaseio.com/").getReference();

        sectionsRecyclerView = findViewById(R.id.sectionsRecyclerView);
        sectionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        sectionList = new ArrayList<>();
        sectionAdapter = new SectionAdapter(sectionList, this);
        sectionsRecyclerView.setAdapter(sectionAdapter);

        FloatingActionButton fabAddSection = findViewById(R.id.fabAddSection);
        fabAddSection.setOnClickListener(view -> showAddSectionDialog());

        loadSections();
    }

    private void loadSections() {
        rootRef.child("sections").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                sectionList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot sectionSnapshot : snapshot.getChildren()) {
                        String sectionName = sectionSnapshot.getKey();
                        String managedBy = sectionSnapshot.child("managedBy").getValue(String.class);
                        sectionList.add(new Section(sectionName, managedBy != null ? managedBy : "Not Assigned"));
                    }
                } else {
                    showToast("No sections found. Click the '+' button to add one.");
                }
                sectionAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showToast("Failed to load sections.");
            }
        });
    }

    private void showAddSectionDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_section, null);
        EditText edtSectionName = dialogView.findViewById(R.id.edtSectionName);
        Spinner spinnerAdmins = dialogView.findViewById(R.id.spinnerAdmins);

        loadAdminsIntoSpinner(spinnerAdmins);

        new AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Add", (dialog, which) -> {
                String sectionName = edtSectionName.getText().toString().trim();
                String selectedAdminName = (String) spinnerAdmins.getSelectedItem();

                if (TextUtils.isEmpty(sectionName) || selectedAdminName == null) {
                    showToast("Section name and admin are required.");
                    return;
                }
                createNewSection(sectionName, selectedAdminName);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void loadAdminsIntoSpinner(Spinner spinner) {
        List<String> adminNames = new ArrayList<>();
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, adminNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        rootRef.child("users").orderByChild("role").equalTo("admin").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                adminNames.clear();
                adminNameToUidMap.clear();
                for (DataSnapshot adminSnapshot : snapshot.getChildren()) {
                    String name = adminSnapshot.child("firstName").getValue(String.class);
                    String uid = adminSnapshot.getKey();
                    adminNames.add(name);
                    adminNameToUidMap.put(name, uid);
                }
                spinnerAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void createNewSection(String sectionName, String adminName) {
        String adminUid = adminNameToUidMap.get(adminName);
        if (adminUid == null) {
            showToast("Could not find UID for selected admin.");
            return;
        }

        Map<String, Object> sectionData = new HashMap<>();
        sectionData.put("managedBy", adminName);

        rootRef.child("sections").child(sectionName).setValue(sectionData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                rootRef.child("users").child(adminUid).child("section").setValue(sectionName);
                showToast("Section created and assigned.");
            } else {
                showToast("Failed to create section.");
            }
        });
    }

    public void addStudentsToSection(String sectionName) {
        Intent intent = new Intent(this, AddStudentsToSectionActivity.class);
        intent.putExtra("SECTION_NAME", sectionName);
        startActivity(intent);
    }

    public void viewStudentsInSection(String sectionName) {
        Intent intent = new Intent(this, ViewStudentsInSectionActivity.class);
        intent.putExtra("SECTION_NAME", sectionName);
        startActivity(intent);
    }

    public void deleteSection(String sectionName) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Section")
            .setMessage("Are you sure you want to delete section '" + sectionName + "'? All students in this section will become unassigned.")
            .setPositiveButton("Delete", (dialog, which) -> {
                rootRef.child("users").orderByChild("section").equalTo(sectionName).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("/sections/" + sectionName, null);

                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            updates.put("/users/" + userSnapshot.getKey() + "/section", null);
                        }
                        
                        rootRef.updateChildren(updates).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                showToast("Section deleted and students unassigned.");
                            } else {
                                showToast("An error occurred.");
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        showToast("Failed to find students to unassign.");
                    }
                });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { onBackPressed(); return true; }
        return super.onOptionsItemSelected(item);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
