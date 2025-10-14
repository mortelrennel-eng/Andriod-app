package com.example.finalproject;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    private TextView tvName;
    private TextView tvStudentId;
    private TextView tvContact;
    private LinearLayout ebooksContainer;
    private LinearLayout attendanceContainer;
    private ImageView qrImage;
    private Button btnDownloadQr;
    private Button btnSignOut;
    private Button btnOpenEbooks;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        tvName = findViewById(R.id.tvStudentName);
        tvStudentId = findViewById(R.id.tvStudentId);
        tvContact = findViewById(R.id.tvStudentContact);
        ebooksContainer = findViewById(R.id.containerEbooks);
        attendanceContainer = findViewById(R.id.containerAttendance);
        qrImage = findViewById(R.id.ivQrStudent);
        btnDownloadQr = findViewById(R.id.btnDownloadQr);
    btnOpenEbooks = findViewById(R.id.btnOpenEbooks);
        btnSignOut = findViewById(R.id.btnStudentSignOut);

        mAuth = FirebaseAuth.getInstance();
    FirebaseDatabase db = FirebaseDatabase.getInstance(RTDB_URL);
        usersRef = db.getReference("users");

        btnSignOut.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(StudentDashboard.this, AdminLoginActivity.class));
            finish();
        });

        btnDownloadQr.setOnClickListener(v -> saveQrToGallery());
    btnOpenEbooks.setOnClickListener(v -> startActivity(new Intent(StudentDashboard.this, EbookListActivity.class)));
    Button btnSaved = findViewById(R.id.btnSavedEbooks);
    btnSaved.setOnClickListener(v -> startActivity(new Intent(StudentDashboard.this, SavedEbooksActivity.class)));

        loadProfileAndData();
    }

    private void loadProfileAndData() {
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, AdminLoginActivity.class));
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
                tvStudentId.setText(sid != null ? sid : "");
                tvContact.setText(contact != null ? contact : "");

                // generate QR using uid
                generateQr(uid);

                // load ebooks list (from /ebooks)
                DatabaseReference ebooksRef = FirebaseDatabase.getInstance(RTDB_URL)
                        .getReference("ebooks");
                ebooksRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ebooksContainer.removeAllViews();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            String title = child.child("title").getValue(String.class);
                            // url is available if you want to open or download the ebook
                            TextView t = new TextView(StudentDashboard.this);
                            t.setText(title != null ? title : "Untitled");
                            t.setPadding(8,8,8,8);
                            ebooksContainer.addView(t);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.w(TAG, "loadEbooks:onCancelled", error.toException());
                    }
                });

                // load attendance (last 20)
                DatabaseReference attendanceRef = FirebaseDatabase.getInstance(RTDB_URL)
                        .getReference("attendance").child(uid);
                attendanceRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        attendanceContainer.removeAllViews();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            String status = child.child("status").getValue(String.class);
                            String when = child.child("dateStr").getValue(String.class);
                            TextView t = new TextView(StudentDashboard.this);
                            t.setText((when != null ? when : "") + " — " + (status != null ? status : ""));
                            t.setPadding(8,8,8,8);
                            attendanceContainer.addView(t);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.w(TAG, "loadAttendance:onCancelled", error.toException());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
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
                OutputStream out = getContentResolver().openOutputStream(uri);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                if (out != null) out.close();
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

