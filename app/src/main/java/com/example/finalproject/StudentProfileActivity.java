package com.example.finalproject;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StudentProfileActivity extends AppCompatActivity {
    private TextView tvProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_profile);

        tvProfile = findViewById(R.id.tvProfile);

        String uid = getIntent().getStringExtra("uid");
        if (uid == null) {
            Toast.makeText(this, "No student selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        DatabaseReference ref = FirebaseDatabase.getInstance("https://finalproject-b08f4-default-rtdb.firebaseio.com/").getReference("users").child(uid);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                StringBuilder sb = new StringBuilder();
                sb.append("UID: ").append(uid).append("\n");
                sb.append("Name: ").append(snapshot.child("firstName").getValue(String.class)).append(" ").append(snapshot.child("lastName").getValue(String.class)).append("\n");
                sb.append("Email: ").append(snapshot.child("email").getValue(String.class)).append("\n");
                sb.append("Student ID: ").append(snapshot.child("studentId").getValue(String.class)).append("\n");
                sb.append("Contact: ").append(snapshot.child("contact").getValue(String.class)).append("\n");
                tvProfile.setText(sb.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StudentProfileActivity.this, "Failed to load profile.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}
