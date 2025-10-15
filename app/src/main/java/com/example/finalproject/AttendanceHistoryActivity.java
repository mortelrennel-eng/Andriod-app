package com.example.finalproject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AttendanceHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AttendanceAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_history);

        recyclerView = findViewById(R.id.rvAttendanceHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadAttendanceHistory();
    }

    private void loadAttendanceHistory() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference attendanceRef = FirebaseDatabase.getInstance("https://finalproject-b08f4-default-rtdb.firebaseio.com/")
            .getReference("attendance").child(uid);

        attendanceRef.orderByChild("dateStr").limitToLast(100).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<AttendanceItem> items = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    String title = child.child("attendanceTitle").getValue(String.class);
                    String status = child.child("status").getValue(String.class);
                    String date = child.child("dateStr").getValue(String.class);
                    items.add(new AttendanceItem(title, status, date));
                }
                // Reverse the list to show the most recent first
                java.util.Collections.reverse(items);
                adapter = new AttendanceAdapter(items);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    // ViewHolder and Adapter for RecyclerView
    private static class AttendanceItem {
        String title, status, date;
        AttendanceItem(String title, String status, String date) {
            this.title = title;
            this.status = status;
            this.date = date;
        }
    }

    private static class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.ViewHolder> {
        private final List<AttendanceItem> attendanceList;

        AttendanceAdapter(List<AttendanceItem> list) {
            this.attendanceList = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AttendanceItem item = attendanceList.get(position);
            holder.text1.setText(item.title != null ? item.title : "General Attendance");
            holder.text2.setText((item.date != null ? item.date : "") + " - Status: " + (item.status != null ? item.status : "N/A"));
        }

        @Override
        public int getItemCount() {
            return attendanceList.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView text1, text2;
            ViewHolder(View itemView) {
                super(itemView);
                text1 = itemView.findViewById(android.R.id.text1);
                text2 = itemView.findViewById(android.R.id.text2);
            }
        }
    }
}
