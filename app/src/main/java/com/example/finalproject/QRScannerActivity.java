package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class QRScannerActivity extends AppCompatActivity {

    private static final String TAG = "QRScannerActivity";
    private FirebaseDatabase realtimeDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realtimeDb = FirebaseDatabase.getInstance("https://finalproject-b08f4-default-rtdb.firebaseio.com/");
        new IntentIntegrator(this).setPrompt("Scan student QR code").setBeepEnabled(true).setOrientationLocked(true).initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                recordAttendance(result.getContents());
            } else {
                Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void recordAttendance(String studentUid) {
        DatabaseReference attendanceDayRef = realtimeDb.getReference("attendanceDay");
        attendanceDayRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot daySnap) {
                String attendanceDayJson = daySnap.getValue(String.class);
                if (attendanceDayJson == null || attendanceDayJson.trim().isEmpty()) {
                    Toast.makeText(QRScannerActivity.this, "Attendance day not set. Please contact superadmin.", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                recordAttendanceForDay(studentUid, attendanceDayJson);
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(QRScannerActivity.this, "Error checking attendance day: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void recordAttendanceForDay(String studentUid, String attendanceDayJson) {
        try {
            JSONObject obj = new JSONObject(attendanceDayJson);
            final String dayDate = obj.optString("date");
            final String dayTitle = obj.optString("title");
            final String lateTimeStr = obj.optString("lateTime");

            realtimeDb.getReference("users").child(studentUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot userSnapshot) {
                    if (!userSnapshot.exists()) {
                        Toast.makeText(QRScannerActivity.this, "Invalid QR Code: Student not found.", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }

                    final String studentName = userSnapshot.child("firstName").getValue(String.class) + " " + userSnapshot.child("lastName").getValue(String.class);
                    DatabaseReference attendanceRef = realtimeDb.getReference("attendance").child(studentUid);

                    attendanceRef.orderByChild("attendanceDay").equalTo(dayDate).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snap) {
                            boolean hasIn = false;
                            boolean hasOut = false;
                            for (DataSnapshot child : snap.getChildren()) {
                                String status = child.child("status").getValue(String.class);
                                if (status != null) {
                                    if (status.equals("IN") || status.equals("LATE")) hasIn = true;
                                    if (status.equals("OUT")) hasOut = true;
                                }
                            }

                            if (hasIn && hasOut) {
                                Toast.makeText(QRScannerActivity.this, "Attendance already completed for today (IN and OUT).", Toast.LENGTH_LONG).show();
                                finish();
                            } else if (hasIn) {
                                // Student is logging OUT
                                writeAttendanceRecord(attendanceRef, studentUid, studentName, "OUT", dayDate, dayTitle);
                            } else {
                                // Student is logging IN, check if LATE
                                try {
                                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                                    Date now = new Date();
                                    Date lateTime = timeFormat.parse(lateTimeStr);
                                    Date currentTime = timeFormat.parse(timeFormat.format(now));

                                    if (currentTime.after(lateTime)) {
                                        writeAttendanceRecord(attendanceRef, studentUid, studentName, "LATE", dayDate, dayTitle);
                                    } else {
                                        writeAttendanceRecord(attendanceRef, studentUid, studentName, "IN", dayDate, dayTitle);
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Time parsing error", e);
                                    Toast.makeText(QRScannerActivity.this, "Error checking time. Recording as IN.", Toast.LENGTH_SHORT).show();
                                    writeAttendanceRecord(attendanceRef, studentUid, studentName, "IN", dayDate, dayTitle);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            Toast.makeText(QRScannerActivity.this, "Error reading attendance: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(QRScannerActivity.this, "Error fetching student data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error parsing attendanceDay JSON", e);
            Toast.makeText(this, "Critical error with attendance day data.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void writeAttendanceRecord(DatabaseReference ref, String uid, String name, String status, String day, String title) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        DatabaseReference newRec = ref.push();
        newRec.child("studentUid").setValue(uid);
        newRec.child("studentName").setValue(name);
        newRec.child("status").setValue(status);
        newRec.child("dateStr").setValue(timestamp);
        newRec.child("attendanceDay").setValue(day);
        newRec.child("attendanceTitle").setValue(title);
        Toast.makeText(QRScannerActivity.this, "Attendance " + status + " recorded for " + name, Toast.LENGTH_LONG).show();
        finish();
    }
}
