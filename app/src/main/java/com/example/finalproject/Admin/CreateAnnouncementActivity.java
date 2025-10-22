package com.example.finalproject.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.finalproject.R;
import com.example.finalproject.model.Announcement;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

public class CreateAnnouncementActivity extends AppCompatActivity {

    private EditText etAnnouncementTitle, etAnnouncementContent;
    private Button btnPostAnnouncement;

    private FirebaseDatabase realtimeDb;
    private DatabaseReference announcementsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_announcement);

        realtimeDb = FirebaseDatabase.getInstance("https://finalproject-b08f4-default-rtdb.firebaseio.com/");
        announcementsRef = realtimeDb.getReference("announcements");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        etAnnouncementTitle = findViewById(R.id.edtAnnouncementTitle);
        etAnnouncementContent = findViewById(R.id.edtAnnouncementContent);
        btnPostAnnouncement = findViewById(R.id.btnPostAnnouncement);

        btnPostAnnouncement.setOnClickListener(v -> postAnnouncement());
    }

    private void postAnnouncement() {
        String title = etAnnouncementTitle.getText().toString().trim();
        String content = etAnnouncementContent.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
            Toast.makeText(this, "Please fill out all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        String announcementId = announcementsRef.push().getKey();
        if (announcementId != null) {
            long timestamp = new Date().getTime();

            // --- THIS IS THE FIX: Passing the ID to the constructor ---
            Announcement announcement = new Announcement(announcementId, title, content, timestamp);

            announcementsRef.child(announcementId).setValue(announcement)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(CreateAnnouncementActivity.this, "Announcement posted.", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(CreateAnnouncementActivity.this, "Failed to post announcement.", Toast.LENGTH_SHORT).show());
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
