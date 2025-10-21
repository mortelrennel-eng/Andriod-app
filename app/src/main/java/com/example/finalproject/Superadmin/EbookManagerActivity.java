package com.example.finalproject.superadmin;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.R;
import com.example.finalproject.adapter.EbookManagerAdapter;
import com.example.finalproject.model.Ebook;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class EbookManagerActivity extends AppCompatActivity {

    private RecyclerView ebookRecyclerView;
    private EbookManagerAdapter adapter;
    private ArrayList<Ebook> ebookList;
    private DatabaseReference ebooksRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ebook_manager);

        ebookRecyclerView = findViewById(R.id.ebookManagerRecyclerView);
        ebookRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ebookList = new ArrayList<>();
        adapter = new EbookManagerAdapter(ebookList, this);
        ebookRecyclerView.setAdapter(adapter);

        ebooksRef = FirebaseDatabase.getInstance().getReference("ebooks");
        loadEbooks();
    }

    private void loadEbooks() {
        ebooksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ebookList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Ebook ebook = dataSnapshot.getValue(Ebook.class);
                    ebookList.add(ebook);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EbookManagerActivity.this, "Failed to load ebooks.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void deleteEbook(Ebook ebook) {
        // Find the ebook by title and delete it
        ebooksRef.orderByChild("title").equalTo(ebook.getTitle()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    dataSnapshot.getRef().removeValue();
                }
                Toast.makeText(EbookManagerActivity.this, "Ebook deleted.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EbookManagerActivity.this, "Failed to delete ebook.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
