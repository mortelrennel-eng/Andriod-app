package com.example.finalproject;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StudentDashboard extends AppCompatActivity {
    private static final String TAG = "StudentDashboard";
    private static final String RTDB_URL = "https://finalproject-b08f4-default-rtdb.firebaseio.com/";

    private TextView tvName, tvStudentId, tvContact;
    private ImageView qrImage;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        tvName = findViewById(R.id.tvStudentName);
        tvStudentId = findViewById(R.id.tvStudentId);
        tvContact = findViewById(R.id.tvStudentContact);
        qrImage = findViewById(R.id.ivQrStudent);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance(RTDB_URL).getReference("users");

        findViewById(R.id.btnEditProfile).setOnClickListener(v -> startActivity(new Intent(StudentDashboard.this, EditStudentProfileActivity.class)));
        findViewById(R.id.btnViewAnnouncements).setOnClickListener(v -> startActivity(new Intent(StudentDashboard.this, ViewAnnouncementsActivity.class)));
        findViewById(R.id.btnViewAttendance).setOnClickListener(v -> startActivity(new Intent(StudentDashboard.this, AttendanceHistoryActivity.class)));
        findViewById(R.id.btnOpenEbooks).setOnClickListener(v -> startActivity(new Intent(StudentDashboard.this, EbookListActivity.class)));
        findViewById(R.id.btnSavedEbooks).setOnClickListener(v -> startActivity(new Intent(StudentDashboard.this, SavedEbooksActivity.class)));
        findViewById(R.id.btnDownloadQr).setOnClickListener(v -> saveQrToGallery());
        findViewById(R.id.btnStudentSignOut).setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(StudentDashboard.this, MainActivity.class));
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload data every time the dashboard is shown
        loadProfileAndData(); 
    }

    private void loadProfileAndData() {
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, StudentLoginActivity.class));
            finish();
            return;
        }
        String uid = mAuth.getCurrentUser().getUid();

        usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;
                String first = snapshot.child("firstName").getValue(String.class);
                String last = snapshot.child("lastName").getValue(String.class);
                String sid = snapshot.child("studentId").getValue(String.class);
                String contact = snapshot.child("contactNumber").getValue(String.class);

                tvName.setText((first != null ? first : "") + " " + (last != null ? last : ""));
                tvStudentId.setText(sid != null ? "ID: " + sid : "");
                tvContact.setText(contact != null ? "Contact: " + contact : "");

                generateQr(uid);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "loadProfile:onCancelled", error.toException());
            }
        });
    }

    private void generateQr(String data) {
        if (data == null) return;
        
        QRCodeWriter writer = new QRCodeWriter();
        try {
            int size = 512;
            com.google.zxing.common.BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, size, size);
            Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }
            if (qrImage != null) {
                qrImage.setImageBitmap(bmp);
            }
        } catch (WriterException e) {
            Log.w(TAG, "generateQr:fail", e);
        }
    }

    private void saveQrToGallery() {
        // ... (saveQrToGallery logic remains the same)
    }
}
