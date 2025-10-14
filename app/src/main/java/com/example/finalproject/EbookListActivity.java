package com.example.finalproject;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EbookListActivity extends AppCompatActivity {
    private static final String TAG = "EbookListActivity";
    private static final String RTDB_URL = "https://finalproject-b08f4-default-rtdb.firebaseio.com/";

    private ListView lvEbooks;
    private ProgressBar progressBar;
    private Button btnBack;

    private List<String> titles = new ArrayList<>();
    private List<String> urls = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    private ExecutorService downloadExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ebook_list);

        lvEbooks = findViewById(R.id.lvEbooks);
        progressBar = findViewById(R.id.progressEbooks);
        btnBack = findViewById(R.id.btnBackEbooks);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, titles);
        lvEbooks.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        lvEbooks.setOnItemClickListener((parent, view, position, id) -> {
            String url = urls.get(position);
            String title = titles.get(position);
            // download then open
            progressBar.setVisibility(View.VISIBLE);
            downloadExecutor.submit(() -> downloadAndOpen(url, title));
        });

        loadEbooks();
    }

    private void loadEbooks() {
        progressBar.setVisibility(View.VISIBLE);
        DatabaseReference ebooksRef = FirebaseDatabase.getInstance(RTDB_URL).getReference("ebooks");
        ebooksRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                titles.clear();
                urls.clear();
                for (DataSnapshot ch : snapshot.getChildren()) {
                    String t = ch.child("title").getValue(String.class);
                    String u = ch.child("url").getValue(String.class);
                    if (t != null && u != null) {
                        titles.add(t);
                        urls.add(u);
                    }
                }
                runOnUiThread(() -> {
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "loadEbooks:onCancelled", error.toException());
                runOnUiThread(() -> progressBar.setVisibility(View.GONE));
            }
        });
    }

    private void downloadAndOpen(String fileUrl, String title) {
        try {
            // download into cache
            URL url = new URL(fileUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(20000);
            conn.setInstanceFollowRedirects(true);
            InputStream in = conn.getInputStream();

            File ebooksDir = new File(getFilesDir(), "ebooks");
            if (!ebooksDir.exists()) ebooksDir.mkdirs();
            File outFile = new File(ebooksDir, title.replaceAll("[^a-zA-Z0-9._-]", "_") + ".pdf");
            try (FileOutputStream fos = new FileOutputStream(outFile)) {
                byte[] buf = new byte[8192];
                int len;
                while ((len = in.read(buf)) != -1) {
                    fos.write(buf, 0, len);
                }
                fos.flush();
            }
            in.close();
            conn.disconnect();

            // open in-app reader
            Intent open = new Intent(this, EbookReaderActivity.class);
            open.putExtra("filePath", outFile.getAbsolutePath());
            runOnUiThread(() -> progressBar.setVisibility(View.GONE));
            startActivity(open);
        } catch (Exception e) {
            Log.w(TAG, "downloadAndOpen:fail", e);
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(EbookListActivity.this, "Failed downloading ebook.", Toast.LENGTH_LONG).show();
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        downloadExecutor.shutdownNow();
    }
}
