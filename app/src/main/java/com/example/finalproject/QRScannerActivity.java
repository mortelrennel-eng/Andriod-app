package com.example.finalproject;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class QRScannerActivity extends AppCompatActivity {

    private static final String TAG = "QRScannerActivity";
    private FirebaseDatabase realtimeDb;
    private DecoratedBarcodeView barcodeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);

        realtimeDb = FirebaseDatabase.getInstance("https://finalproject-b08f4-default-rtdb.firebaseio.com/");
        barcodeView = findViewById(R.id.zxing_barcode_scanner);

        barcodeView.decodeSingle(result -> {
            if (result.getText() != null) {
                barcodeView.pause(); // Pause scanner after a result is found
                recordAttendance(result.getText());
            } else {
                 Toast.makeText(this, "Scan failed. Please try again.", Toast.LENGTH_SHORT).show();
                 finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        barcodeView.resume(); // Ensure scanner is running on resume
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeView.pause(); // Pause scanner to save resources
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

                    final String studentName = (userSnapshot.child("firstName").getValue(String.class) == null ? "" : userSnapshot.child("firstName").getValue(String.class))
                            + " " + (userSnapshot.child("lastName").getValue(String.class) == null ? "" : userSnapshot.child("lastName").getValue(String.class));
                    final String studentSection = userSnapshot.child("section").getValue(String.class);

                    DatabaseReference attendanceRef = realtimeDb.getReference("attendance").child(studentUid);

                    // If current user is an admin, ensure they can only scan students in their section
                    String currentUid = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
                    if (currentUid != null) {
                        realtimeDb.getReference("users").child(currentUid).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot currentUserSnap) {
                                String role = currentUserSnap.child("role").getValue(String.class);
                                String adminSection = currentUserSnap.child("section").getValue(String.class);
                                if ("admin".equals(role)) {
                                    if (adminSection == null || studentSection == null || !adminSection.equals(studentSection)) {
                                        Toast.makeText(QRScannerActivity.this, "You are not allowed to scan this student.", Toast.LENGTH_LONG).show();
                                        finish();
                                        return;
                                    }
                                }
                                // allowed -> proceed
                                proceedAttendanceCheck(attendanceRef, studentUid, studentName, dayDate, dayTitle, lateTimeStr);
                            }

                            @Override
                            public void onCancelled(DatabaseError error) {
                                Toast.makeText(QRScannerActivity.this, "Error checking user role: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });
                    } else {
                        // no signed in user (likely student); proceed
                        proceedAttendanceCheck(attendanceRef, studentUid, studentName, dayDate, dayTitle, lateTimeStr);
                    }
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

    private void proceedAttendanceCheck(DatabaseReference attendanceRef, String studentUid, String studentName, String dayDate, String dayTitle, String lateTimeStr) {
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
