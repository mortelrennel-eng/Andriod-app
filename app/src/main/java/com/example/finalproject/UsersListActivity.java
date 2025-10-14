package com.example.finalproject;

import android.os.Bundle;
import android.util.Log;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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
import java.util.List;

public class UsersListActivity extends AppCompatActivity {
    private static final String TAG = "UsersListActivity";
    private RecyclerView recyclerView;
    private UsersAdapter adapter;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);

        // guard: only admin/superadmin allowed
        if (!ensureAdmin()) return;

        recyclerView = findViewById(R.id.rvUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UsersAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        FirebaseDatabase db = FirebaseDatabase.getInstance("https://finalproject-b08f4-default-rtdb.firebaseio.com/");
        usersRef = db.getReference("users");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<UserItem> items = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    String uid = child.getKey();
                    String email = child.child("email").getValue(String.class);
                    String role = child.child("role").getValue(String.class);
                    String name;

                    // Handle both new format (firstName, lastName) and old format (name)
                    if (child.hasChild("firstName") && child.hasChild("lastName")) {
                        String first = child.child("firstName").getValue(String.class);
                        String last = child.child("lastName").getValue(String.class);
                        name = ((first != null ? first : "") + " " + (last != null ? last : "")).trim();
                    } else {
                        name = child.child("name").getValue(String.class);
                    }

                    items.add(new UserItem(uid, name, email, role));
                }
                adapter.update(items);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "loadUsers:onCancelled", error.toException());
            }
        });
    }

    private boolean ensureAdmin() {
        if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Not signed in", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, AdminLoginActivity.class));
            finish();
            return false;
        }
        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance("https://finalproject-b08f4-default-rtdb.firebaseio.com/").getReference("users");
        ref.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String role = snapshot.child("role").getValue(String.class);
                if (role == null || !(role.equals("admin") || role.equals("superadmin"))) {
                    Toast.makeText(UsersListActivity.this, "Access denied", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(UsersListActivity.this, AdminLoginActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UsersListActivity.this, "Database error", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        return true;
    }

    static class UserItem {
        String uid, name, email, role;

        UserItem(String uid, String name, String email, String role) {
            this.uid = uid;
            this.name = name;
            this.email = email;
            this.role = role;
        }
    }

    static class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.VH> {
        private List<UserItem> items;

        UsersAdapter(List<UserItem> items) { this.items = items; }

        void update(List<UserItem> newItems) {
            this.items = newItems;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            UserItem it = items.get(position);
            holder.title.setText(it.name != null && !it.name.isEmpty() ? it.name : (it.email != null ? it.email : it.uid));
            holder.subtitle.setText((it.role != null ? it.role : "") + (it.email != null ? " — " + it.email : ""));
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView title, subtitle;
            VH(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(android.R.id.text1);
                subtitle = itemView.findViewById(android.R.id.text2);
            }
        }
    }
}
