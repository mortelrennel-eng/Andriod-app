package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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

    private static final String TAG = "SuperAdminDashboard";
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private TextView tvTotalSections, tvTotalStudents, tvFrequentAbsentees;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_super_admin);

        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance("https://finalproject-b08f4-default-rtdb.firebaseio.com/").getReference();

        tvTotalSections = findViewById(R.id.tvTotalSections);
        tvTotalStudents = findViewById(R.id.tvTotalStudents);
        tvFrequentAbsentees = findViewById(R.id.tvFrequentAbsentees);

        findViewById(R.id.btnRegisterSuperAdmin).setOnClickListener(v -> startActivity(new Intent(this, RegisterSuperAdminActivity.class)));
        findViewById(R.id.btnRegisterAdmin).setOnClickListener(v -> startActivity(new Intent(this, RegisterAdminActivity.class)));
        findViewById(R.id.btnManageAdmins).setOnClickListener(v -> startActivity(new Intent(this, ManageAdminsActivity.class)));
        findViewById(R.id.btnViewAllUsers).setOnClickListener(v -> startActivity(new Intent(this, UsersListActivity.class)));
        findViewById(R.id.btnAddEbook).setOnClickListener(v -> startActivity(new Intent(this, AddEbookActivity.class)));
        findViewById(R.id.btnManageEbooks).setOnClickListener(v -> startActivity(new Intent(this, EbookManagerActivity.class)));
        findViewById(R.id.btnManageStudents).setOnClickListener(v -> startActivity(new Intent(this, ManageStudentsActivity.class)));
        findViewById(R.id.btnManageAttendance).setOnClickListener(v -> startActivity(new Intent(this, ManageAttendanceActivity.class)));
        findViewById(R.id.btnManageSections).setOnClickListener(v -> startActivity(new Intent(this, ManageSectionsActivity.class)));
        findViewById(R.id.btnViewAbsences).setOnClickListener(v -> startActivity(new Intent(this, AbsenteesListActivity.class)));
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

    private void loadDashboardStats() {
        rootRef.child("sections").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(tvTotalSections != null) tvTotalSections.setText(String.valueOf(snapshot.getChildrenCount()));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        rootRef.child("users").orderByChild("role").equalTo("student").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(tvTotalStudents != null) tvTotalStudents.setText(String.valueOf(snapshot.getChildrenCount()));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        rootRef.child("attendance_by_day").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Integer> absenceCount = new HashMap<>();
                for (DataSnapshot day : snapshot.getChildren()) {
                    for (DataSnapshot session : day.getChildren()) {
                        for (DataSnapshot record : session.getChildren()) {
                            String studentUid = record.getKey();
                            String status = record.child("status").getValue(String.class);
                            if ("Absent".equalsIgnoreCase(status)) {
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
                if(tvFrequentAbsentees != null) tvFrequentAbsentees.setText(String.valueOf(frequentAbsentees));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void showSetAttendanceDayDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_set_attendance, null);
        builder.setView(dialogView);

        final EditText edtTitle = dialogView.findViewById(R.id.edtAttendanceTitle);
        final EditText edtLateTime = dialogView.findViewById(R.id.edtLateTime);

        builder.setPositiveButton("Set", (dialog, which) -> {
            String title = edtTitle.getText().toString().trim();
            String lateTime = edtLateTime.getText().toString().trim();
            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(lateTime)) {
                Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show();
                return;
            }
            markPreviousAbsenteesAndSetNewDay(title, lateTime);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void markPreviousAbsenteesAndSetNewDay(final String newTitle, final String newLateTime) {
        DatabaseReference attendanceDayRef = rootRef.child("attendanceDay");

        attendanceDayRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String previousTitle = snapshot.child("title").getValue(String.class);
                    String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                    if (previousTitle != null && !previousTitle.isEmpty()) {
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
                            public void onCancelled(@NonNull DatabaseError error) {
                                setNewAttendanceDay(newTitle, newLateTime);
                            }
                        });
                    } else {
                         setNewAttendanceDay(newTitle, newLateTime);
                    }
                } else {
                    setNewAttendanceDay(newTitle, newLateTime);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                setNewAttendanceDay(newTitle, newLateTime);
            }
        });
    }

    private void setNewAttendanceDay(String title, String lateTime) {
        Map<String, Object> attendanceData = new HashMap<>();
        attendanceData.put("title", title);
        attendanceData.put("lateTime", lateTime);

        rootRef.child("attendanceDay").setValue(attendanceData)
            .addOnSuccessListener(aVoid -> Toast.makeText(SuperAdminDashboard.this, "New attendance day set!", Toast.LENGTH_LONG).show())
            .addOnFailureListener(e -> Toast.makeText(SuperAdminDashboard.this, "Failed to set new attendance day.", Toast.LENGTH_SHORT).show());
    }
}
