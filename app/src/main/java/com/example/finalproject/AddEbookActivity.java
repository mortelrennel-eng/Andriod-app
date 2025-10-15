package com.example.finalproject;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

public class AddEbookActivity extends AppCompatActivity {

    private EditText edtEbookTitle, edtEbookCategory, edtEbookLink;
    private Button btnAddEbook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ebook);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Add E-Book Link");

        edtEbookTitle = findViewById(R.id.edtEbookTitle);
        edtEbookCategory = findViewById(R.id.edtEbookCategory);
        edtEbookLink = findViewById(R.id.edtEbookLink);
        btnAddEbook = findViewById(R.id.btnAddEbook);

        btnAddEbook.setOnClickListener(v -> saveEbookLink());
    }

    private void saveEbookLink() {
        String title = edtEbookTitle.getText().toString().trim();
        String category = edtEbookCategory.getText().toString().trim();
        String link = edtEbookLink.getText().toString().trim();

        if (title.isEmpty() || category.isEmpty() || link.isEmpty()) {
            Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference dbRef = FirebaseDatabase.getInstance("https://finalproject-b08f4-default-rtdb.firebaseio.com/").getReference("ebooks");
        String ebookId = dbRef.push().getKey();

        if (ebookId != null) {
            Map<String, Object> ebookData = new HashMap<>();
            ebookData.put("title", title);
            ebookData.put("category", category);
            ebookData.put("fileUrl", link); // We still use 'fileUrl' for consistency

            dbRef.child(ebookId).setValue(ebookData).addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "E-book added successfully!", Toast.LENGTH_LONG).show();
                finish();
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to add e-book.", Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(this, "Failed to generate e-book ID.", Toast.LENGTH_SHORT).show();
        }
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
