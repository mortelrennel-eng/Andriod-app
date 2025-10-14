package com.example.finalproject;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ViewAdminsActivity extends AppCompatActivity {

    ListView adminList;
    DatabaseReference usersRef;
    List<String> admins = new ArrayList<>();
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_admins);

        adminList = findViewById(R.id.adminList);
    FirebaseDatabase db = FirebaseDatabase.getInstance("https://finalproject-b08f4-default-rtdb.firebaseio.com/");
    usersRef = db.getReference("users");
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, admins);
        adminList.setAdapter(adapter);

        loadAdmins();
    }

    private void loadAdmins() {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                admins.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    String role = child.child("role").getValue(String.class);
                    if ("admin".equals(role)) {
                        String name = child.child("name").getValue(String.class);
                        String email = child.child("email").getValue(String.class);
                        String dept = child.child("department").getValue(String.class);
                        admins.add((name != null ? name : "") + " (" + (dept != null ? dept : "") + ")\n" + (email != null ? email : ""));
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }
}
