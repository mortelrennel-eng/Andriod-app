package com.example.finalproject;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class AdminUploadActivity extends AppCompatActivity {
    private static final String TAG = "AdminUpload";
    private static final int PICK_PDF = 9001;
    private EditText etTitle;
    private Button btnPick, btnUpload;
    private ProgressBar progress;
    private Uri pickedUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_upload);

        // guard: ensure user is admin/superadmin
        if (!checkAdmin()) return;

        etTitle = findViewById(R.id.etEbookTitle);
        btnPick = findViewById(R.id.btnPickPdf);
        btnUpload = findViewById(R.id.btnUploadPdf);
        progress = findViewById(R.id.progressUpload);

        btnPick.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.setType("application/pdf");
            startActivityForResult(Intent.createChooser(i, "Select PDF"), PICK_PDF);
        });

        btnUpload.setOnClickListener(v -> upload());
    }

    private boolean checkAdmin() {
        if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Not signed in", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, AdminLoginActivity.class));
            finish();
            return false;
        }
        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference usersRef = FirebaseDatabase.getInstance("https://finalproject-b08f4-default-rtdb.firebaseio.com/").getReference("users");
        usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String role = snapshot.child("role").getValue(String.class);
                if (role == null || !(role.equals("admin") || role.equals("superadmin"))) {
                    Toast.makeText(AdminUploadActivity.this, "Access denied", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(AdminUploadActivity.this, AdminLoginActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminUploadActivity.this, "Database error", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PDF && resultCode == Activity.RESULT_OK && data != null) {
            pickedUri = data.getData();
            Toast.makeText(this, "Picked: " + pickedUri.getLastPathSegment(), Toast.LENGTH_SHORT).show();
        }
    }

    private void upload() {
        String title = etTitle.getText().toString().trim();
        if (title.isEmpty() || pickedUri == null) {
            Toast.makeText(this, "Title and PDF required", Toast.LENGTH_SHORT).show();
            return;
        }
        progress.setVisibility(View.VISIBLE);

        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("ebooks/")
                .child(System.currentTimeMillis() + ".pdf");
    // disable upload button while uploading
    btnUpload.setEnabled(false);
    storageRef.putFile(pickedUri)
        .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    // write to RTDB
                    DatabaseReference ebooksRef = FirebaseDatabase.getInstance("https://finalproject-b08f4-default-rtdb.firebaseio.com/").getReference("ebooks");
                    String key = ebooksRef.push().getKey();
                    Map<String, Object> map = new HashMap<>();
                    map.put("title", title);
                    map.put("url", uri.toString());
                    ebooksRef.child(key).setValue(map)
                            .addOnSuccessListener(aVoid -> {
                                progress.setVisibility(View.GONE);
                                btnUpload.setEnabled(true);
                                new androidx.appcompat.app.AlertDialog.Builder(AdminUploadActivity.this)
                                        .setTitle("Upload successful")
                                        .setMessage("E-book uploaded and saved to database.")
                                        .setPositiveButton("OK", null)
                                        .show();
                            })
                            .addOnFailureListener(e -> {
                                progress.setVisibility(View.GONE);
                                btnUpload.setEnabled(true);
                                Toast.makeText(AdminUploadActivity.this, "Failed writing metadata", Toast.LENGTH_SHORT).show();
                            });
                }))
                .addOnFailureListener(e -> {
                    progress.setVisibility(View.GONE);
                    btnUpload.setEnabled(true);
                    Toast.makeText(AdminUploadActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                });
    }
}
