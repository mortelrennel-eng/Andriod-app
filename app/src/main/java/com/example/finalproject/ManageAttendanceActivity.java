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

public class ManageAttendanceActivity extends AppCompatActivity {

    private ListView sectionsListView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> sectionList;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_attendance);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Attendance: Select Section");

        searchView = findViewById(R.id.searchView);
        sectionsListView = findViewById(R.id.sectionsListView);
        
        sectionList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, sectionList);
        sectionsListView.setAdapter(adapter);

        setupSearchView();
        loadSections();

        sectionsListView.setOnItemClickListener((parent, view, position, id) -> {
            String sectionName = (String) parent.getItemAtPosition(position);
            // This now opens the new activity to show a list of students
            Intent intent = new Intent(ManageAttendanceActivity.this, StudentListForAttendanceActivity.class);
            intent.putExtra("SECTION_NAME", sectionName);
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

    private void loadSections() {
        DatabaseReference sectionsRef = FirebaseDatabase.getInstance().getReference("sections");
        sectionsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                sectionList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot sectionSnapshot : snapshot.getChildren()) {
                        sectionList.add(sectionSnapshot.getKey());
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(ManageAttendanceActivity.this, "No sections found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageAttendanceActivity.this, "Failed to load sections.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { onBackPressed(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
