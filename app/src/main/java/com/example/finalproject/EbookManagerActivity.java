package com.example.finalproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class EbookManagerActivity extends AppCompatActivity {

    private RecyclerView ebooksRecyclerView;
    private EbookAdapter ebookAdapter;
    private ArrayList<Ebook> ebookList;
    private ArrayList<Ebook> filteredList;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ebook_manager);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Manage E-Books");

        ebooksRecyclerView = findViewById(R.id.ebooksRecyclerView);
        ebooksRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        ebookList = new ArrayList<>();
        filteredList = new ArrayList<>();
        ebookAdapter = new EbookAdapter(filteredList, this);
        ebooksRecyclerView.setAdapter(ebookAdapter);

        searchView = findViewById(R.id.searchView);
        setupSearchView();

        loadEbooks();
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });
    }

    private void filter(String text) {
        filteredList.clear();
        if (text.isEmpty()) {
            filteredList.addAll(ebookList);
        } else {
            text = text.toLowerCase();
            for (Ebook ebook : ebookList) {
                if (ebook.getTitle().toLowerCase().contains(text) || (ebook.getCategory() != null && ebook.getCategory().toLowerCase().contains(text))) {
                    filteredList.add(ebook);
                }
            }
        }
        ebookAdapter.notifyDataSetChanged();
    }

    private void loadEbooks() {
        DatabaseReference ebooksRef = FirebaseDatabase.getInstance().getReference("ebooks");
        ebooksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ebookList.clear();
                for (DataSnapshot ebookSnapshot : snapshot.getChildren()) {
                    Ebook ebook = ebookSnapshot.getValue(Ebook.class);
                    if (ebook != null) {
                        ebook.setKey(ebookSnapshot.getKey());
                        ebookList.add(ebook);
                    }
                }
                filter(searchView.getQuery().toString()); // Apply current filter
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EbookManagerActivity.this, "Failed to load ebooks.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void viewEbook(Ebook ebook) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(ebook.getFileUrl()));
        startActivity(intent);
    }

    public void editEbook(Ebook ebook) {
        // To-do: Create a new EditEbookActivity
        Toast.makeText(this, "Editing: " + ebook.getTitle(), Toast.LENGTH_SHORT).show();
    }

    public void deleteEbook(Ebook ebook) {
        new AlertDialog.Builder(this)
            .setTitle("Delete E-book")
            .setMessage("Are you sure you want to delete '" + ebook.getTitle() + "'?")
            .setPositiveButton("Delete", (dialog, which) -> {
                DatabaseReference ebookRef = FirebaseDatabase.getInstance().getReference("ebooks").child(ebook.getKey());
                ebookRef.removeValue();
                Toast.makeText(this, "E-book deleted.", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
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
