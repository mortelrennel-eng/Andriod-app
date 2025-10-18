package com.example.finalproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class EbookListActivity extends AppCompatActivity {

    private RecyclerView ebooksRecyclerView;
    private EbookListAdapter ebookAdapter;
    private ArrayList<Ebook> ebookList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ebook_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("E-Book Library");

        ebooksRecyclerView = findViewById(R.id.ebooksRecyclerView);
        ebooksRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        ebookList = new ArrayList<>();
        ebookAdapter = new EbookListAdapter(ebookList, this);
        ebooksRecyclerView.setAdapter(ebookAdapter);

        loadEbooks();
    }

    private void loadEbooks() {
        DatabaseReference ebooksRef = FirebaseDatabase.getInstance("https://finalproject-b08f4-default-rtdb.firebaseio.com/").getReference("ebooks");
        ebooksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ebookList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot ebookSnapshot : snapshot.getChildren()) {
                        Ebook ebook = ebookSnapshot.getValue(Ebook.class);
                        if (ebook != null) {
                            ebookList.add(ebook);
                        }
                    }
                    ebookAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(EbookListActivity.this, "No e-books available.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EbookListActivity.this, "Failed to load e-books.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void openEbook(Ebook ebook) {
        if (ebook.getFileUrl() != null && !ebook.getFileUrl().isEmpty()) {
            try {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ebook.getFileUrl()));
                startActivity(browserIntent);
            } catch (Exception e) {
                Toast.makeText(this, "Could not open link. Please ensure a web browser is installed.", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "E-book link is not available.", Toast.LENGTH_SHORT).show();
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
