package com.example.finalproject.superadmin;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import com.example.finalproject.R;
import com.example.finalproject.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddStudentsToSectionActivity extends AppCompatActivity {

    private ListView studentsListView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> studentDisplayList;
    private ArrayList<User> allUnassignedStudents;
    private ArrayList<String> selectedStudentIds = new ArrayList<>();
    private String sectionName;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_students_to_section);

        sectionName = getIntent().getStringExtra("SECTION_NAME");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Add to " + sectionName);

        searchView = findViewById(R.id.searchView);
        studentsListView = findViewById(R.id.studentsListView);
        studentsListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        
        studentDisplayList = new ArrayList<>();
        allUnassignedStudents = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, studentDisplayList);
        studentsListView.setAdapter(adapter);

        setupSearchView();
        loadStudentsWithoutSection();

        Button btnAddSelected = findViewById(R.id.btnAddSelectedStudents);
        btnAddSelected.setOnClickListener(v -> addSelectedStudentsToSection());
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

    private void loadStudentsWithoutSection() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.orderByChild("role").equalTo("student").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allUnassignedStudents.clear();
                studentDisplayList.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    if (!userSnapshot.hasChild("section")) {
                        User user = userSnapshot.getValue(User.class);
                        if (user != null) {
                            user.setUid(userSnapshot.getKey());
                            allUnassignedStudents.add(user);
                            studentDisplayList.add(user.getFirstName() + " " + user.getLastName());
                        }
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

    private void addSelectedStudentsToSection() {
        selectedStudentIds.clear();
        android.util.SparseBooleanArray checked = studentsListView.getCheckedItemPositions();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (checked.get(i)) {
                String name = adapter.getItem(i);
                for(User user : allUnassignedStudents) {
                    if((user.getFirstName() + " " + user.getLastName()).equals(name)){
                        selectedStudentIds.add(user.getUid());
                        break;
                    }
                }
            }
        }

        if (selectedStudentIds.isEmpty()) {
            Toast.makeText(this, "Please select at least one student.", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        Map<String, Object> updates = new HashMap<>();
        for (String studentId : selectedStudentIds) {
            updates.put("/sections/" + sectionName + "/students/" + studentId, true);
            updates.put("/users/" + studentId + "/section", sectionName);
        }

        rootRef.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, selectedStudentIds.size() + " students added.", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to add students.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { onBackPressed(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
