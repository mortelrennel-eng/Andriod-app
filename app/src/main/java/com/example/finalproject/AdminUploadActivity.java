package com.example.finalproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.HashMap;
import java.util.Map;

public class AdminUploadActivity extends AppCompatActivity {

    private static final int FILE_SELECT_CODE = 100;
    private EditText edtEbookTitle, edtEbookCategory;
    private Button btnSelectFile, btnUpload;
    private ProgressBar progressBar;
    private Uri selectedFileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_upload);

        // Toolbar with back button
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Upload E-Book");

        edtEbookTitle = findViewById(R.id.edtEbookTitle);
        edtEbookCategory = findViewById(R.id.edtEbookCategory);
        btnSelectFile = findViewById(R.id.btnSelectFile);
        btnUpload = findViewById(R.id.btnUpload);
        progressBar = findViewById(R.id.progressBar);

        btnSelectFile.setOnClickListener(v -> selectFile());
        btnUpload.setOnClickListener(v -> uploadEbook());
    }

    private void selectFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "Select a PDF"), FILE_SELECT_CODE);
        } catch (Exception e) {
            Toast.makeText(this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_SELECT_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedFileUri = data.getData();
            Toast.makeText(this, "File selected: " + selectedFileUri.getLastPathSegment(), Toast.LENGTH_SHORT).show();
            btnUpload.setEnabled(true); // Enable upload button only when a file is selected
        }
    }

    private void uploadEbook() {
        String title = edtEbookTitle.getText().toString().trim();
        String category = edtEbookCategory.getText().toString().trim();

        if (selectedFileUri == null) {
            Toast.makeText(this, "Please select a file first.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter a title.", Toast.LENGTH_SHORT).show();
            return;
        }

        setInProgress(true);

        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("ebooks/" + System.currentTimeMillis() + "_" + selectedFileUri.getLastPathSegment());
        
        storageRef.putFile(selectedFileUri).addOnSuccessListener(taskSnapshot -> {
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String fileUrl = uri.toString();
                saveEbookMetadata(title, category, fileUrl);
            }).addOnFailureListener(e -> showError("Failed to get download URL."));
        }).addOnFailureListener(e -> showError("Upload Failed. Please check Firebase Storage Rules."));
    }

    private void saveEbookMetadata(String title, String category, String fileUrl) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance("https://finalproject-b08f4-default-rtdb.firebaseio.com/").getReference("ebooks");
        String ebookId = dbRef.push().getKey();

        if (ebookId != null) {
            Map<String, Object> ebookData = new HashMap<>();
            ebookData.put("title", title);
            ebookData.put("category", category);
            ebookData.put("fileUrl", fileUrl);

            dbRef.child(ebookId).setValue(ebookData).addOnSuccessListener(aVoid -> {
                Toast.makeText(AdminUploadActivity.this, "E-book uploaded successfully!", Toast.LENGTH_LONG).show();
                setInProgress(false);
                finish(); // Go back to the dashboard
            }).addOnFailureListener(e -> showError("Failed to save e-book details."));
        } else {
            showError("Failed to generate e-book ID.");
        }
    }

    private void setInProgress(boolean inProgress) {
        progressBar.setVisibility(inProgress ? View.VISIBLE : View.GONE);
        btnUpload.setEnabled(!inProgress);
        btnSelectFile.setEnabled(!inProgress);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        setInProgress(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
