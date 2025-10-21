package com.example.finalproject.admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finalproject.MainActivity;
import com.example.finalproject.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class AdminDashboard extends AppCompatActivity {
    private static final String TAG = "AdminDashboard";

    private TextView adminTitle, tvTotalStudents, tvFrequentAbsentees;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance("https://finalproject-b08f4-default-rtdb.firebaseio.com/").getReference();

        adminTitle = findViewById(R.id.tvAdminWelcome);
        tvTotalStudents = findViewById(R.id.tvTotalStudentsInSection);
        tvFrequentAbsentees = findViewById(R.id.tvFrequentAbsenteesInSection);

        findViewById(R.id.btnScanQr).setOnClickListener(v -> startActivity(new Intent(this, QRScannerActivity.class)));
        findViewById(R.id.btnViewAttendance).setOnClickListener(v -> startActivity(new Intent(this, AdminViewAttendanceActivity.class)));
        findViewById(R.id.btnViewMySection).setOnClickListener(v -> startActivity(new Intent(this, ViewMySectionActivity.class)));
        findViewById(R.id.btnAdminSignOut).setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(this, AdminLoginActivity.class));
            finish();
        });

        loadAdminData();
    }

    private void loadAdminData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            finish(); // Should not happen, but as a safeguard
            return;
        }

        rootRef.child("users").child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    adminTitle.setText("Welcome, " + snapshot.child("firstName").getValue(String.class));
                    String mySection = snapshot.child("section").getValue(String.class);
                    if (mySection != null) {
                        loadDashboardStats(mySection);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void loadDashboardStats(String sectionName) {
        rootRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Get all students in the admin's section
                Map<String, String> myStudents = new HashMap<>();
                for (DataSnapshot userSnap : snapshot.child("users").getChildren()) {
                    if (sectionName.equals(userSnap.child("section").getValue(String.class))) {
                        myStudents.put(userSnap.getKey(), "");
                    }
                }
                tvTotalStudents.setText(String.valueOf(myStudents.size()));

                // Count absences for those students
                Map<String, Integer> absenceCount = new HashMap<>();
                for (DataSnapshot daySnap : snapshot.child("attendance_by_day").getChildren()) {
                    for (DataSnapshot sessionSnap : daySnap.getChildren()) {
                        for (DataSnapshot recordSnap : sessionSnap.getChildren()) {
                            String studentUid = recordSnap.getKey();
                            if (myStudents.containsKey(studentUid) && "Absent".equalsIgnoreCase(recordSnap.child("status").getValue(String.class))) {
                                absenceCount.put(studentUid, absenceCount.getOrDefault(studentUid, 0) + 1);
                            }
                        }
                    }
                }
                int frequentAbsentees = 0;
                for (int count : absenceCount.values()) {
                    if (count >= 2) {
                        frequentAbsentees++;
                    }
                }
                tvFrequentAbsentees.setText(String.valueOf(frequentAbsentees));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
}
