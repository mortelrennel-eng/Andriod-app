package com.example.finalproject;

import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
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

    // ... (rest of the methods like loadSections, showAddSectionDialog, etc. remain the same)

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // This will take the user back to the previous screen
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    // All other methods from the previous correct version of this file should be here.
    // I am omitting them for brevity, but they are essential.
    private void loadSections() {
        rootRef.child("sections").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                sectionList.clear();
                for (DataSnapshot sectionSnapshot : snapshot.getChildren()) {
                    String sectionName = sectionSnapshot.getKey();
                    String managedBy = sectionSnapshot.child("managedBy").getValue(String.class);
                    sectionList.add(new Section(sectionName, managedBy != null ? managedBy : "Not Assigned"));
                }
                sectionAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { showToast("Failed to load sections."); }
        });
    }

    private void showAddSectionDialog() {
        // ... Implementation from before
    }

    public void addStudentsToSection(String sectionName) {
        // ... Implementation from before
    }

    private void assignStudentsToSection(String sectionName, List<String> studentUids) {
       // ... Implementation from before
    }

    public void deleteSection(String sectionName) {
       // ... Implementation from before
    }

    private void loadAdminsIntoSpinner(Spinner spinner) {
        // ... Implementation from before
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
