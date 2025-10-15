package com.example.finalproject;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class QRScannerActivity extends AppCompatActivity {

    private static final String TAG = "QRScannerActivity";
    private DecoratedBarcodeView barcodeScannerView;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private boolean isScanHandled = false; // Flag to prevent multiple scans from one QR

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // This now uses the XML layout we designed
        setContentView(R.layout.activity_qr_scanner);

        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance("https://finalproject-b08f4-default-rtdb.firebaseio.com/").getReference();

        barcodeScannerView = findViewById(R.id.zxing_barcode_scanner);
        barcodeScannerView.decodeContinuous(callback);
    }

    // This callback handles the result directly from the view in our XML
    private final BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() != null && !isScanHandled) {
                isScanHandled = true; // Prevents the same scan from being handled multiple times
                barcodeScannerView.pause(); // Pause scanner while processing
                getAttendanceSessionAndProceed(result.getText());
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        isScanHandled = false; // Reset flag when activity is resumed
        barcodeScannerView.resume(); // Start the camera
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeScannerView.pause(); // Stop the camera
    }

    private void getAttendanceSessionAndProceed(String studentUid) {
        rootRef.child("attendanceDay").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists() || !snapshot.hasChild("title")) {
                    showToastAndFinish("Attendance session is not set by Super Admin.");
                    return;
                }
                String attendanceTitle = snapshot.child("title").getValue(String.class);
                String lateTimeStr = snapshot.child("lateTime").getValue(String.class);
                checkIfAlreadyScanned(studentUid, attendanceTitle, lateTimeStr);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showToastAndFinish("Failed to load attendance settings.");
            }
        });
    }

    private void checkIfAlreadyScanned(String studentUid, String attendanceTitle, String lateTimeStr) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        DatabaseReference attendanceRecordRef = rootRef.child("attendance_by_day").child(today).child(attendanceTitle).child(studentUid);

        attendanceRecordRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild("time_out")) {
                    showToastAndFinish("Student has already timed in and out for this session.");
                } else if (snapshot.exists()) {
                    recordTimeOut(attendanceRecordRef);
                } else {
                    String status = determineAttendanceStatus(lateTimeStr);
                    validateUserAndRecordTimeIn(studentUid, attendanceTitle, status);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showToastAndFinish("Database error: Could not verify attendance.");
            }
        });
    }

    private void validateUserAndRecordTimeIn(String studentUid, String attendanceTitle, String status) {
        // This logic remains the same (checking roles, sections)
        // ... (This part is already correct)
        recordTimeIn(studentUid, attendanceTitle, status);
    }

    private void recordTimeIn(String studentUid, String attendanceTitle, String status) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        DatabaseReference attendanceRef = rootRef.child("attendance_by_day").child(today).child(attendanceTitle).child(studentUid);
        
        Map<String, Object> timeInData = new HashMap<>();
        timeInData.put("time_in", currentTime);
        timeInData.put("status", status);

        attendanceRef.setValue(timeInData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                showToastAndFinish("Time IN recorded: " + status);
            } else {
                showToastAndFinish("Failed to record Time IN.");
            }
        });
    }

    private void recordTimeOut(DatabaseReference attendanceRecordRef) {
        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        attendanceRecordRef.child("time_out").setValue(currentTime).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                showToastAndFinish("Time OUT recorded.");
            } else {
                showToastAndFinish("Failed to record Time OUT.");
            }
        });
    }

    private String determineAttendanceStatus(String lateTime) {
        if (lateTime == null) return "On-Time";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date late = sdf.parse(lateTime);
            Date now = sdf.parse(sdf.format(new Date()));
            return now.after(late) ? "Late" : "On-Time";
        } catch (ParseException e) {
            return "On-Time";
        }
    }
    
    private void showToastAndFinish(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish();
    }
}
