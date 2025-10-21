package com.example.finalproject.superadmin;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.R;
import com.example.finalproject.adapter.UserSectionAdapter;
import com.example.finalproject.model.SectionHeader;
import com.example.finalproject.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UsersListActivity extends AppCompatActivity {

    private RecyclerView usersRecyclerView;
    private UserSectionAdapter adapter;
    private List<Object> itemList;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);

        usersRecyclerView = findViewById(R.id.usersRecyclerView);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        itemList = new ArrayList<>();
        adapter = new UserSectionAdapter(itemList);
        usersRecyclerView.setAdapter(adapter);

        usersRef = FirebaseDatabase.getInstance("https://finalproject-b08f4-default-rtdb.firebaseio.com/").getReference("users");
        loadUsers();
    }

    private void loadUsers() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                itemList.clear();
                List<User> admins = new ArrayList<>();
                List<User> superAdmins = new ArrayList<>();
                List<User> students = new ArrayList<>();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        switch (user.getRole()) {
                            case "admin":
                                admins.add(user);
                                break;
                            case "superadmin":
                                superAdmins.add(user);
                                break;
                            case "student":
                                students.add(user);
                                break;
                        }
                    }
                }

                if (!superAdmins.isEmpty()) {
                    itemList.add(new SectionHeader("Super Admins"));
                    itemList.addAll(superAdmins);
                }
                if (!admins.isEmpty()) {
                    itemList.add(new SectionHeader("Admins"));
                    itemList.addAll(admins);
                }
                if (!students.isEmpty()) {
                    itemList.add(new SectionHeader("Students"));
                    itemList.addAll(students);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UsersListActivity.this, "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
