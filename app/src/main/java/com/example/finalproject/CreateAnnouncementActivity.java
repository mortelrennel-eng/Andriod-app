package com.example.finalproject;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

        etAnnouncementTitle = findViewById(R.id.etAnnouncementTitle);
        etAnnouncementContent = findViewById(R.id.etAnnouncementContent);
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
            Announcement announcement = new Announcement(title, content, timestamp);
            announcementsRef.child(announcementId).setValue(announcement)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(CreateAnnouncementActivity.this, "Announcement posted.", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(CreateAnnouncementActivity.this, "Failed to post announcement.", Toast.LENGTH_SHORT).show());
        }
    }
}