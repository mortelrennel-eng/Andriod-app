package com.example.finalproject;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.OutputStream;

public class StudentDashboard extends AppCompatActivity {
    private static final String TAG = "StudentDashboard";
    private static final String RTDB_URL = "https://finalproject-b08f4-default-rtdb.firebaseio.com/";

    private TextView tvName, tvStudentId, tvContact;
    private ImageView qrImage;
    private String currentStudentName = "student";

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

                String fullName = (first != null ? first : "") + " " + (last != null ? last : "");
                currentStudentName = fullName.trim();

                tvName.setText(fullName);
                tvStudentId.setText(sid != null ? "ID: " + sid : "");
                tvContact.setText(contact != null ? "Contact: " + contact : "");

                // The QR code for scanning should only contain the UID
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
            BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, size, size);
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
        qrImage.setDrawingCacheEnabled(true);
        qrImage.buildDrawingCache(true);
        Bitmap qrBitmap = qrImage.getDrawingCache();

        if (qrBitmap == null) {
            Toast.makeText(this, "QR Code not available yet.", Toast.LENGTH_SHORT).show();
            return;
        }

        int templateWidth = 800;
        int templateHeight = 1100;
        Bitmap templateBitmap = Bitmap.createBitmap(templateWidth, templateHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(templateBitmap);
        canvas.drawColor(Color.WHITE);

        float qrX = (templateWidth - qrBitmap.getWidth()) / 2f;
        float qrY = 150f;
        canvas.drawBitmap(qrBitmap, qrX, qrY, null);

        String name = tvName.getText().toString();
        String studentId = tvStudentId.getText().toString();
        String contact = tvContact.getText().toString();

        Paint namePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        namePaint.setColor(Color.BLACK);
        namePaint.setTextSize(55);
        namePaint.setTextAlign(Paint.Align.CENTER);

        Paint detailsPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        detailsPaint.setColor(Color.DKGRAY);
        detailsPaint.setTextSize(35);
        detailsPaint.setTextAlign(Paint.Align.CENTER);

        float textX = templateWidth / 2f;
        float nameY = qrY + qrBitmap.getHeight() + 120;
        canvas.drawText(name, textX, nameY, namePaint);
        canvas.drawText(studentId, textX, nameY + 70, detailsPaint);
        canvas.drawText(contact, textX, nameY + 120, detailsPaint);

        String filename = currentStudentName.replace(" ", "_") + "_ID_Card.png";
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
                    templateBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear();
                    values.put(MediaStore.Images.Media.IS_PENDING, 0);
                    getContentResolver().update(uri, values, null, null);
                }
                Toast.makeText(this, "ID Card saved to gallery", Toast.LENGTH_LONG).show();
            } else {
                throw new Exception("Content resolver returned null URI");
            }
        } catch (Exception e) {
            Log.e(TAG, "saveQr:fail", e);
            Toast.makeText(this, "Failed to save ID Card.", Toast.LENGTH_SHORT).show();
        }
    }
}
