package com.example.finalproject;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;

public class ViewAnnouncementsActivity extends AppCompatActivity {

    private static final String TAG = "ViewAnnouncements";

    private RecyclerView announcementsRecyclerView;
    private AnnouncementAdapter announcementAdapter;
    private ArrayList<Announcement> announcementList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_announcements);

        try {
            // 1. Initialize RecyclerView
            announcementsRecyclerView = findViewById(R.id.rvAnnouncements);
            if (announcementsRecyclerView == null) {
                Log.e(TAG, "RecyclerView with ID 'rvAnnouncements' not found in layout.");
                showErrorToast("A critical view is missing. Cannot display announcements.");
                return; // Exit if the RecyclerView is not found
            }
            announcementsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            
            // 2. Initialize List and Adapter
            announcementList = new ArrayList<>();
            announcementAdapter = new AnnouncementAdapter(announcementList);
            
            // 3. Set Adapter to RecyclerView
            announcementsRecyclerView.setAdapter(announcementAdapter);

            // 4. Load Data
            loadAnnouncements();

        } catch (Exception e) {
            Log.e(TAG, "An unexpected error occurred in onCreate", e);
            showErrorToast("An unexpected error occurred.");
        }
    }

    private void loadAnnouncements() {
        DatabaseReference announcementsRef = FirebaseDatabase.getInstance("https://finalproject-b08f4-default-rtdb.firebaseio.com/").getReference("announcements");

        announcementsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                announcementList.clear();
                if (!snapshot.exists()) {
                    showToast("No announcements available.");
                } else {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Announcement announcement = dataSnapshot.getValue(Announcement.class);
                        if (announcement != null) {
                            announcementList.add(announcement);
                        }
                    }
                    Collections.reverse(announcementList); // Show newest first
                }
                announcementAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showErrorToast("Failed to load announcements.");
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showErrorToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
