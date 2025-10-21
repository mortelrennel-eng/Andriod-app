package com.example.finalproject.admin;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.example.finalproject.R;
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

public class QRScannerActivity extends AppCompatActivity {
    private CodeScanner mCodeScanner;
    private DatabaseReference rootRef;
    private static final int CAMERA_REQUEST_CODE = 101;
    private static final String TAG = "QRScannerActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);

        rootRef = FirebaseDatabase.getInstance("https://finalproject-b08f4-default-rtdb.firebaseio.com/").getReference();

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        }

        CodeScannerView scannerView = findViewById(R.id.scanner_view);
        mCodeScanner = new CodeScanner(this, scannerView);

        mCodeScanner.setDecodeCallback(result -> runOnUiThread(() -> {
            mCodeScanner.stopPreview(); // Stop scanning to prevent multiple triggers
            String scannedUid = result.getText();
            markAttendance(scannedUid);
        }));

        scannerView.setOnClickListener(view -> mCodeScanner.startPreview());
    }

    private void markAttendance(String uid) {
        rootRef.child("attendanceDay").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot attendanceDaySnap) {
                if (!attendanceDaySnap.exists()) {
                    showToastAndFinish("No attendance session set by Super Admin.");
                    return;
                }

                String sessionTitle = attendanceDaySnap.child("title").getValue(String.class);
                String lateTimeString = attendanceDaySnap.child("lateTime").getValue(String.class);
                String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

                DatabaseReference recordRef = rootRef.child("attendance_by_day").child(today).child(sessionTitle).child(uid);

                recordRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot recordSnap) {
                        rootRef.child("users").child(uid).child("firstName").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot nameSnap) {
                                String studentName = nameSnap.getValue(String.class) != null ? nameSnap.getValue(String.class) : "Unknown Student";

                                if (!recordSnap.exists()) {
                                    // --- FIRST SCAN (TIME-IN) ---
                                    String status = "On-Time";
                                    if (lateTimeString != null && currentTime.substring(0, 5).compareTo(lateTimeString) > 0) {
                                        status = "Late";
                                    }
                                    Map<String, Object> attendanceData = new HashMap<>();
                                    attendanceData.put("status", status);
                                    attendanceData.put("time_in", currentTime);
                                    final String finalStatus = status;
                                    recordRef.setValue(attendanceData).addOnCompleteListener(task -> 
                                        showToastAndFinish(studentName + " - Time In: " + finalStatus));
                                } else if (!recordSnap.hasChild("time_out")) {
                                    // --- SECOND SCAN (TIME-OUT) ---
                                    recordRef.child("time_out").setValue(currentTime).addOnCompleteListener(task -> 
                                        showToastAndFinish(studentName + " - Time Out Recorded"));
                                } else {
                                    // --- THIRD SCAN (ALREADY COMPLETED) ---
                                    showToastAndResume("Attendance for " + studentName + " already completed.");
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) { showToastAndResume("Failed to get student name."); }
                        });
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { showToastAndResume("Database read error."); }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { showToastAndResume("Failed to get attendance session."); }
        });
    }

    private void showToastAndResume(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        new android.os.Handler().postDelayed(() -> mCodeScanner.startPreview(), 2000);
    }
    
    private void showToastAndFinish(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish(); // Go back to the previous activity (AdminDashboard)
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCodeScanner.startPreview();
    }

    @Override
    protected void onPause() {
        mCodeScanner.releaseResources();
        super.onPause();
    }
}
