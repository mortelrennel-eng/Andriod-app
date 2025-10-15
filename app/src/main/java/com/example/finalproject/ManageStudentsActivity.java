package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class ManageStudentsActivity extends AppCompatActivity {

    private ListView sectionsListView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> sectionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_students);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Manage Students by Section");

        sectionsListView = findViewById(R.id.sectionsListView);
        sectionList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, sectionList);
        sectionsListView.setAdapter(adapter);

        loadSections();

        sectionsListView.setOnItemClickListener((parent, view, position, id) -> {
            String sectionName = sectionList.get(position);
            Intent intent = new Intent(ManageStudentsActivity.this, StudentListBySectionActivity.class);
            intent.putExtra("SECTION_NAME", sectionName);
            startActivity(intent);
        });
    }

    private void loadSections() {
        DatabaseReference sectionsRef = FirebaseDatabase.getInstance("https://finalproject-b08f4-default-rtdb.firebaseio.com/").getReference("sections");
        sectionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                sectionList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot sectionSnapshot : snapshot.getChildren()) {
                        sectionList.add(sectionSnapshot.getKey());
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(ManageStudentsActivity.this, "No sections found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageStudentsActivity.this, "Failed to load sections.", Toast.LENGTH_SHORT).show();
            }
        });
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
