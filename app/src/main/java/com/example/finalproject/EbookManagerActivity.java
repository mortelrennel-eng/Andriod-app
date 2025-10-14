package com.example.finalproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EbookManagerActivity extends AppCompatActivity {

    private Button uploadBtn;
    private ListView ebookListView;
    private ProgressBar progressBar;
    private EditText titleEditText;
    private EditText categoryEditText;

    private FirebaseStorage storage;
    private FirebaseDatabase realtimeDb;
    private DatabaseReference ebooksRef;

    private ArrayList<Ebook> ebookList;
    private ArrayAdapter<Ebook> ebookAdapter;

    private static final int FILE_SELECT_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ebook_manager);

    // Initialize Firebase
    storage = FirebaseStorage.getInstance();
    realtimeDb = FirebaseDatabase.getInstance("https://finalproject-b08f4-default-rtdb.firebaseio.com/");
    ebooksRef = realtimeDb.getReference("ebooks");

        // Initialize views
        uploadBtn = findViewById(R.id.uploadBtn);
        ebookListView = findViewById(R.id.ebookListView);
        titleEditText = findViewById(R.id.titleEditText);
        categoryEditText = findViewById(R.id.categoryEditText);
        // We will add a progress bar for better UX, assuming you add it to your XML
        // progressBar = findViewById(R.id.progressBar);

        // Setup list and adapter
        ebookList = new ArrayList<>();
        ebookAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, ebookList);
        ebookListView.setAdapter(ebookAdapter);

        // Set listeners
        uploadBtn.setOnClickListener(v -> selectFile());
        ebookListView.setOnItemClickListener((parent, view, position, id) -> {
            Ebook selectedEbook = ebookList.get(position);
            // Open the ebook URL in a browser
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(selectedEbook.getFileUrl()));
            startActivity(browserIntent);
        });

        // Load existing ebooks
        loadEbooks();
    }

    private void selectFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "Select a PDF to Upload"), FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_SELECT_CODE && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            uploadFile(fileUri);
        }
    }

    private void uploadFile(Uri fileUri) {
        if (fileUri == null) return;

        Toast.makeText(this, "Upload starting...", Toast.LENGTH_SHORT).show();
        String fileName = "ebook_" + System.currentTimeMillis() + ".pdf";
        StorageReference ref = storage.getReference().child("ebooks/" + fileName);

        ref.putFile(fileUri)
            .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                String title = titleEditText.getText().toString().trim();
                String category = categoryEditText.getText().toString().trim();
                if (title.isEmpty()) {
                    title = fileName; // fallback to filename
                }
                Map<String, Object> ebookData = new HashMap<>();
                ebookData.put("title", title);
                ebookData.put("category", category);
                ebookData.put("fileUrl", uri.toString());

                // save metadata to Realtime Database under /ebooks (push id)
                ebooksRef.push().setValue(ebookData)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(EbookManagerActivity.this, "Ebook uploaded successfully!", Toast.LENGTH_SHORT).show();
                            titleEditText.setText("");
                            categoryEditText.setText("");
                            loadEbooks();
                        })
                        .addOnFailureListener(e -> Toast.makeText(EbookManagerActivity.this, "Failed to save ebook metadata.", Toast.LENGTH_SHORT).show());
            }))
            .addOnFailureListener(e -> Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void loadEbooks() {
        ebooksRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                ebookList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    String title = child.child("title").getValue(String.class);
                    String fileUrl = child.child("fileUrl").getValue(String.class);
                    Ebook ebook = new Ebook(title, fileUrl);
                    ebookList.add(ebook);
                }
                ebookAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(EbookManagerActivity.this, "Failed to load ebooks.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Helper class to hold ebook data
    private static class Ebook {
        private String title;
        private String fileUrl;

        public Ebook(String title, String fileUrl) {
            this.title = title;
            this.fileUrl = fileUrl;
        }

        public String getFileUrl() {
            return fileUrl;
        }

        // This is what is displayed in the ListView
        @Override
        public String toString() {
            return title;
        }
    }
}
