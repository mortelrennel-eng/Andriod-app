package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SuperAdminDashboard extends AppCompatActivity {

    private static final String TAG = "SuperAdminDashboard";
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_super_admin);

        mAuth = FirebaseAuth.getInstance();

        // Set up buttons safely
        setupButton(R.id.btnRegisterSuperAdmin, RegisterSuperAdminActivity.class);
        setupButton(R.id.btnRegisterAdmin, RegisterAdminActivity.class);
        setupButton(R.id.btnViewAllUsers, UsersListActivity.class);
        setupButton(R.id.btnUploadEbook, AdminUploadActivity.class);
        setupButton(R.id.btnManageEbooks, EbookManagerActivity.class);
        setupButton(R.id.btnManageStudents, ManageStudentsActivity.class);
        setupButton(R.id.btnManageAttendance, UsersListActivity.class);

        Button btnSetAttendanceDay = findViewById(R.id.btnSetAttendanceDay);
        if (btnSetAttendanceDay != null) {
            btnSetAttendanceDay.setOnClickListener(v -> showSetAttendanceDayDialog());
        } else {
            Log.e(TAG, "Button not found: btnSetAttendanceDay");
        }

        Button btnLogout = findViewById(R.id.btnLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                mAuth.signOut();
                Intent intent = new Intent(SuperAdminDashboard.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        } else {
            Log.e(TAG, "Button not found: btnLogout");
        }
    }

    private void setupButton(int buttonId, Class<?> targetActivity) {
        Button button = findViewById(buttonId);
        if (button != null) {
            button.setOnClickListener(v -> checkConnectionAndNavigate(targetActivity));
        } else {
            Log.e(TAG, "Button not found for ID: " + getResources().getResourceEntryName(buttonId));
        }
    }

    private void checkConnectionAndNavigate(Class<?> targetActivity) {
        // ... (existing code)
    }

    private void showSetAttendanceDayDialog() {
        // Inflate the custom dialog layout
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_set_attendance, null);

        final EditText edtTitle = dialogView.findViewById(R.id.edtAttendanceTitle);
        final EditText edtStartTime = dialogView.findViewById(R.id.edtStartTime);
        final EditText edtLateTime = dialogView.findViewById(R.id.edtLateTime);
        final EditText edtEndTime = dialogView.findViewById(R.id.edtEndTime);

        new android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Set", (dialog, which) -> {
                String title = edtTitle.getText().toString().trim();
                String startTime = edtStartTime.getText().toString().trim();
                String lateTime = edtLateTime.getText().toString().trim();
                String endTime = edtEndTime.getText().toString().trim();

                if (TextUtils.isEmpty(title) || TextUtils.isEmpty(startTime) || TextUtils.isEmpty(lateTime) || TextUtils.isEmpty(endTime)) {
                    Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Automatically get the current date
                String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                try {
                    JSONObject attendanceData = new JSONObject();
                    attendanceData.put("date", currentDate);
                    attendanceData.put("title", title);
                    attendanceData.put("startTime", startTime);
                    attendanceData.put("lateTime", lateTime);
                    attendanceData.put("endTime", endTime);

                    // Save the JSON string to Firebase
                    DatabaseReference attendanceDayRef = FirebaseDatabase.getInstance("https://finalproject-b08f4-default-rtdb.firebaseio.com/").getReference("attendanceDay");
                    attendanceDayRef.setValue(attendanceData.toString())
                        .addOnSuccessListener(aVoid -> Toast.makeText(this, "Attendance day set successfully!", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(this, "Failed to set attendance day: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                } catch (Exception e) {
                    Log.e(TAG, "Error creating JSON for attendance day", e);
                    Toast.makeText(this, "An error occurred.", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel())
            .show();
    }
}
