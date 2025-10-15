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

public class SectionAttendanceActivity extends AppCompatActivity {

    private ListView attendanceListView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> attendanceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_section_attendance);

        attendanceListView = findViewById(R.id.attendanceListView);
        attendanceList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, attendanceList);
        attendanceListView.setAdapter(adapter);

        String sectionId = getIntent().getStringExtra("SECTION_ID");
        if (sectionId != null) {
            loadAttendanceForSection(sectionId);
        }
    }

    private void loadAttendanceForSection(String sectionId) {
        DatabaseReference attendanceRef = FirebaseDatabase.getInstance("https://finalproject-b08f4-default-rtdb.firebaseio.com/").getReference("attendance").child(sectionId);
        attendanceRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                attendanceList.clear();
                for (DataSnapshot studentSnapshot : snapshot.getChildren()) {
                    String studentName = studentSnapshot.child("name").getValue(String.class);
                    String status = studentSnapshot.child("status").getValue(String.class);
                    attendanceList.add(studentName + " - " + status);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SectionAttendanceActivity.this, "Failed to load attendance.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
