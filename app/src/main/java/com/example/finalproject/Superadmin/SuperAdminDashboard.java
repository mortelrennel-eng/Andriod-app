package com.example.finalproject.superadmin;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finalproject.MainActivity;
import com.example.finalproject.R;
import com.example.finalproject.admin.RegisterAdminActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SuperAdminDashboard extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private TextView tvTotalStudents, tvTotalMale, tvTotalFemale, tvTotalSections, tvFrequentAbsentees, tvTotalEbooks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_super_admin);

        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance("https://finalproject-b08f4-default-rtdb.firebaseio.com/").getReference();

        tvTotalStudents = findViewById(R.id.tvTotalStudents);
        tvTotalMale = findViewById(R.id.tvTotalMale);
        tvTotalFemale = findViewById(R.id.tvTotalFemale);
        tvTotalSections = findViewById(R.id.tvTotalSections);
        tvFrequentAbsentees = findViewById(R.id.tvFrequentAbsentees);
        tvTotalEbooks = findViewById(R.id.tvTotalEbooks);

        findViewById(R.id.btnRegisterAdmin).setOnClickListener(v -> startActivity(new Intent(this, RegisterAdminActivity.class)));
        findViewById(R.id.btnManageAdmins).setOnClickListener(v -> startActivity(new Intent(this, com.example.finalproject.superadmin.ManageAdminsActivity.class)));
        findViewById(R.id.btnViewAllUsers).setOnClickListener(v -> startActivity(new Intent(this, com.example.finalproject.superadmin.UsersListActivity.class)));
        findViewById(R.id.btnAddEbook).setOnClickListener(v -> startActivity(new Intent(this, com.example.finalproject.superadmin.AddEbookActivity.class)));
        findViewById(R.id.btnManageEbooks).setOnClickListener(v -> startActivity(new Intent(this, com.example.finalproject.superadmin.EbookManagerActivity.class)));
        findViewById(R.id.btnManageStudents).setOnClickListener(v -> startActivity(new Intent(this, com.example.finalproject.superadmin.ManageStudentsActivity.class)));
        findViewById(R.id.btnManageAttendance).setOnClickListener(v -> startActivity(new Intent(this, com.example.finalproject.superadmin.ManageAttendanceActivity.class)));
        findViewById(R.id.btnManageSections).setOnClickListener(v -> startActivity(new Intent(this, com.example.finalproject.superadmin.ManageSectionsActivity.class)));
        findViewById(R.id.btnViewAbsences).setOnClickListener(v -> startActivity(new Intent(this, com.example.finalproject.superadmin.AbsenteesListActivity.class)));
        findViewById(R.id.btnPostAnnouncement).setOnClickListener(v -> startActivity(new Intent(this, com.example.finalproject.superadmin.ManageAnnouncementsActivity.class)));
        findViewById(R.id.btnSetAttendanceDay).setOnClickListener(v -> showSetAttendanceDayDialog());
        
        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
        
        loadDashboardStats();
    }

    // --- THIS IS THE FIX: The complete and correct method to load all stats ---
    private void loadDashboardStats() {
        rootRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long sectionsCount = snapshot.child("sections").getChildrenCount();
                tvTotalSections.setText(String.valueOf(sectionsCount));

                long ebooksCount = snapshot.child("ebooks").getChildrenCount();
                tvTotalEbooks.setText(String.valueOf(ebooksCount));

                DataSnapshot usersSnap = snapshot.child("users");
                long totalStudents = 0; int maleCount = 0; int femaleCount = 0;
                for (DataSnapshot userSnapshot : usersSnap.getChildren()) {
                    if ("student".equals(userSnapshot.child("role").getValue(String.class))) {
                        totalStudents++;
                        if ("Male".equalsIgnoreCase(userSnapshot.child("gender").getValue(String.class))) maleCount++;
                        else if ("Female".equalsIgnoreCase(userSnapshot.child("gender").getValue(String.class))) femaleCount++;
                    }
                }
                tvTotalStudents.setText(String.valueOf(totalStudents));
                tvTotalMale.setText(String.valueOf(maleCount));
                tvTotalFemale.setText(String.valueOf(femaleCount));

                DataSnapshot attendanceSnap = snapshot.child("attendance_by_day");
                Map<String, Integer> absenceCount = new HashMap<>();
                for (DataSnapshot day : attendanceSnap.getChildren()) {
                    for (DataSnapshot session : day.getChildren()) {
                        for (DataSnapshot record : session.getChildren()) {
                            if ("Absent".equalsIgnoreCase(record.child("status").getValue(String.class))) {
                                absenceCount.put(record.getKey(), absenceCount.getOrDefault(record.getKey(), 0) + 1);
                            }
                        }
                    }
                }
                int frequentAbsentees = 0;
                for (int count : absenceCount.values()) {
                    if (count >= 2) frequentAbsentees++;
                }
                tvFrequentAbsentees.setText(String.valueOf(frequentAbsentees));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { Toast.makeText(SuperAdminDashboard.this, "Failed to load dashboard stats.", Toast.LENGTH_SHORT).show(); }
        });
    }

    // --- AND THE FIX: All other methods restored ---
    private void showSetAttendanceDayDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_set_attendance, null);
        final EditText edtTitle = dialogView.findViewById(R.id.edtAttendanceTitle);
        final EditText edtLateTime = dialogView.findViewById(R.id.edtLateTime);
        new AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Set", (dialog, which) -> {
                String title = edtTitle.getText().toString().trim();
                String lateTime = edtLateTime.getText().toString().trim();
                if (TextUtils.isEmpty(title) || TextUtils.isEmpty(lateTime)) {
                    Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show();
                    return;
                }
                markPreviousAbsenteesAndSetNewDay(title, lateTime);
            })
            .setNegativeButton("Cancel", null).show();
    }

    private void markPreviousAbsenteesAndSetNewDay(final String newTitle, final String newLateTime) {
        DatabaseReference attendanceDayRef = rootRef.child("attendanceDay");
        attendanceDayRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.child("title").exists()) {
                    String previousTitle = snapshot.child("title").getValue(String.class);
                    String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                    DatabaseReference previousRecordsRef = rootRef.child("attendance_by_day").child(today).child(previousTitle);
                    previousRecordsRef.orderByChild("time_out").equalTo(null).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot recordsSnapshot) {
                            for (DataSnapshot absenteeSnapshot : recordsSnapshot.getChildren()) {
                                absenteeSnapshot.getRef().child("status").setValue("Absent");
                            }
                            setNewAttendanceDay(newTitle, newLateTime);
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) { setNewAttendanceDay(newTitle, newLateTime); }
                    });
                } else {
                     setNewAttendanceDay(newTitle, newLateTime);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void setNewAttendanceDay(String title, String lateTime) {
        Map<String, Object> attendanceData = new HashMap<>();
        attendanceData.put("title", title);
        attendanceData.put("lateTime", lateTime);
        rootRef.child("attendanceDay").setValue(attendanceData)
            .addOnSuccessListener(aVoid -> Toast.makeText(SuperAdminDashboard.this, "New attendance session set!", Toast.LENGTH_LONG).show());
    }
}
