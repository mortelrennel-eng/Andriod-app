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
    private Button btnDownloadQr, btnSignOut, btnOpenEbooks, btnViewAttendance, btnSavedEbooks;

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
        btnDownloadQr = findViewById(R.id.btnDownloadQr);
        btnOpenEbooks = findViewById(R.id.btnOpenEbooks);
        btnSignOut = findViewById(R.id.btnStudentSignOut);
        btnViewAttendance = findViewById(R.id.btnViewAttendance);
        btnSavedEbooks = findViewById(R.id.btnSavedEbooks);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance(RTDB_URL).getReference("users");

        btnSignOut.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(StudentDashboard.this, MainActivity.class));
            finish();
        });

        btnDownloadQr.setOnClickListener(v -> saveQrToGallery());
        btnOpenEbooks.setOnClickListener(v -> startActivity(new Intent(StudentDashboard.this, EbookListActivity.class)));
        btnSavedEbooks.setOnClickListener(v -> startActivity(new Intent(StudentDashboard.this, SavedEbooksActivity.class)));

        // Set listener for the new attendance button
        btnViewAttendance.setOnClickListener(v -> {
            startActivity(new Intent(StudentDashboard.this, AttendanceHistoryActivity.class));
        });

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

                // Generate and display the QR code
                generateQr(uid);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "loadProfile:onCancelled", error.toException());
            }
        });
    }

    private void generateQr(String data) {
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
            qrImage.setImageBitmap(bmp);
        } catch (WriterException e) {
            Log.w(TAG, "generateQr:fail", e);
        }
    }

    private void saveQrToGallery() {
        qrImage.setDrawingCacheEnabled(true);
        qrImage.buildDrawingCache(true);
        Bitmap bitmap = qrImage.getDrawingCache();
        if (bitmap == null) {
            Toast.makeText(this, "QR not available", Toast.LENGTH_SHORT).show();
            return;
        }

        String filename = "student_qr_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".png";
        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.put(MediaStore.Images.Media.IS_PENDING, 1);
            }
            Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                try (OutputStream out = getContentResolver().openOutputStream(uri)) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear();
                    values.put(MediaStore.Images.Media.IS_PENDING, 0);
                    getContentResolver().update(uri, values, null, null);
                }
                Toast.makeText(this, "QR saved to gallery", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.w(TAG, "saveQr:fail", e);
            Toast.makeText(this, "Failed saving QR", Toast.LENGTH_SHORT).show();
        }
    }
}
