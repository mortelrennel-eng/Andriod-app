package com.example.finalproject.admin;

import android.Manifest;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class QRScannerActivity extends AppCompatActivity {
    private CodeScanner mCodeScanner;
    private DatabaseReference rootRef;
    private FirebaseAuth mAuth;
    private String adminSection;
    private static final String TAG = "QRScannerActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);

        rootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        loadAdminSection();
        setupPermissions();
        CodeScannerView scannerView = findViewById(R.id.scanner_view);
        mCodeScanner = new CodeScanner(this, scannerView);

        mCodeScanner.setDecodeCallback(result -> runOnUiThread(() -> {
            mCodeScanner.stopPreview();
            String scannedUid = result.getText();
            markAttendance(scannedUid);
        }));

        scannerView.setOnClickListener(view -> mCodeScanner.startPreview());
    }

    private void loadAdminSection(){
        FirebaseUser adminUser = mAuth.getCurrentUser();
        if(adminUser != null){
            rootRef.child("users").child(adminUser.getUid()).child("section").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    adminSection = snapshot.getValue(String.class);
                    if(adminSection == null){
                        showToastAndFinish("You are not assigned to a section. Cannot scan.");
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    showToastAndFinish("Failed to get your section details.");
                }
            });
        }
    }

    private void markAttendance(String uid) {
        if(adminSection == null){
            showToastAndResume("Cannot verify section. Please restart the scanner.");
            return;
        }

        rootRef.child("attendanceDay").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot attendanceDaySnap) {
                if (!attendanceDaySnap.exists() || attendanceDaySnap.child("title").getValue(String.class) == null) {
                    showToastAndFinish("No active attendance session set by Super Admin.");
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
                        rootRef.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot userSnap) {
                                if (!userSnap.exists()) {
                                    showToastAndResume("Scanned QR is not a valid user.");
                                    return;
                                }

                                String studentSection = userSnap.child("section").getValue(String.class);
                                if(!adminSection.equals(studentSection)){
                                    showToastAndResume("Access Denied: Student is not in your section.");
                                    return;
                                }
                                
                                String studentName = userSnap.child("firstName").getValue(String.class);

                                if (!recordSnap.exists()) {
                                    String status = "On-Time";
                                    if (lateTimeString != null && currentTime.substring(0, 5).compareTo(lateTimeString) > 0) {
                                        status = "Late";
                                    }
                                    Map<String, Object> attendanceData = new HashMap<>();
                                    attendanceData.put("status", status);
                                    attendanceData.put("time_in", currentTime);
                                    final String finalStatus = status;
                                    recordRef.setValue(attendanceData).addOnCompleteListener(task -> {
                                        showToastAndFinish(studentName + " - Time In: " + finalStatus);
                                    });
                                } else if (!recordSnap.hasChild("time_out")) {
                                    recordRef.child("time_out").setValue(currentTime).addOnCompleteListener(task -> {
                                        showToastAndFinish(studentName + " - Time Out Recorded");
                                    });
                                } else {
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

    private void setupPermissions() {
        int cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
             ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }
    }

    private void showToastAndResume(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        new android.os.Handler().postDelayed(() -> mCodeScanner.startPreview(), 2000);
    }
    
    private void showToastAndFinish(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCodeScanner != null) {
            mCodeScanner.startPreview();
        }
    }

    @Override
    protected void onPause() {
        if (mCodeScanner != null) {
            mCodeScanner.releaseResources();
        }
        super.onPause();
    }
}
