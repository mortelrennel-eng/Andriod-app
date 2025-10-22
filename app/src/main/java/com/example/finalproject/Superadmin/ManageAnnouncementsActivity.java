package com.example.finalproject.superadmin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.R;
import com.example.finalproject.adapter.ManageAnnouncementsAdapter;
import com.example.finalproject.model.Announcement;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class ManageAnnouncementsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ManageAnnouncementsAdapter adapter;
    private ArrayList<Announcement> announcementList;
    private DatabaseReference announcementsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_announcements);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        announcementsRef = FirebaseDatabase.getInstance().getReference("announcements");

        recyclerView = findViewById(R.id.announcementsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        announcementList = new ArrayList<>();
        adapter = new ManageAnnouncementsAdapter(announcementList, this);
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabAddAnnouncement);
        fab.setOnClickListener(v -> showAnnouncementDialog(null));

        loadAnnouncements();
    }

    private void loadAnnouncements() {
        announcementsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                announcementList.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Announcement announcement = postSnapshot.getValue(Announcement.class);
                    if (announcement != null) {
                        announcement.setId(postSnapshot.getKey());
                        announcementList.add(announcement);
                    }
                }
                Collections.reverse(announcementList);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageAnnouncementsActivity.this, "Failed to load announcements.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void showAnnouncementDialog(final Announcement announcement) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_announcement, null);
        EditText edtTitle = dialogView.findViewById(R.id.edtAnnouncementTitle);
        EditText edtContent = dialogView.findViewById(R.id.edtAnnouncementContent);

        String dialogTitle = announcement == null ? "Add New Announcement" : "Edit Announcement";

        if (announcement != null) {
            edtTitle.setText(announcement.getTitle());
            edtContent.setText(announcement.getContent());
        }

        new AlertDialog.Builder(this)
            .setTitle(dialogTitle)
            .setView(dialogView)
            .setPositiveButton("Save", (dialog, which) -> {
                String title = edtTitle.getText().toString().trim();
                String content = edtContent.getText().toString().trim();

                if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
                    Toast.makeText(this, "Title and content cannot be empty.", Toast.LENGTH_SHORT).show();
                    return;
                }

                saveAnnouncement(announcement, title, content);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void saveAnnouncement(Announcement announcement, String title, String content) {
        long timestamp = new Date().getTime();
        String id;
        if (announcement == null) { // New announcement
            id = announcementsRef.push().getKey();
        } else { // Editing existing one
            id = announcement.getId();
        }

        if (id == null) return;
        Announcement newAnnouncement = new Announcement(id, title, content, timestamp);
        announcementsRef.child(id).setValue(newAnnouncement)
            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Announcement saved.", Toast.LENGTH_SHORT).show())
            .addOnFailureListener(e -> Toast.makeText(this, "Failed to save announcement.", Toast.LENGTH_SHORT).show());
    }

    public void deleteAnnouncement(Announcement announcement) {
        if (announcement.getId() == null) return;
        new AlertDialog.Builder(this)
            .setTitle("Delete Announcement")
            .setMessage("Are you sure you want to delete this announcement?")
            .setPositiveButton("Delete", (dialog, which) -> 
                announcementsRef.child(announcement.getId()).removeValue()
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Announcement deleted.", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to delete.", Toast.LENGTH_SHORT).show()))
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
