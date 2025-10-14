package com.example.finalproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SavedEbooksActivity extends AppCompatActivity {
    private ListView lvSaved;
    private Button btnBack;
    private List<String> paths = new ArrayList<>();
    private List<String> titles = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_ebooks);

        lvSaved = findViewById(R.id.lvSavedEbooks);
        btnBack = findViewById(R.id.btnBackSaved);

        btnBack.setOnClickListener(v -> finish());

        loadSaved();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, titles);
        lvSaved.setAdapter(adapter);

        lvSaved.setOnItemClickListener((parent, view, position, id) -> {
            String path = paths.get(position);
            File f = new File(path);
            if (!f.exists()) {
                Toast.makeText(this, "File not found.", Toast.LENGTH_SHORT).show();
                return;
            }
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", f);
            Intent open = new Intent(Intent.ACTION_VIEW);
            open.setDataAndType(uri, "application/pdf");
            open.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                startActivity(open);
            } catch (Exception e) {
                Toast.makeText(this, "No app to open PDF.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadSaved() {
        File ebooksDir = new File(getFilesDir(), "ebooks");
        titles.clear(); paths.clear();
        if (!ebooksDir.exists()) return;
        for (File f : ebooksDir.listFiles()) {
            titles.add(f.getName());
            paths.add(f.getAbsolutePath());
        }
    }
}
