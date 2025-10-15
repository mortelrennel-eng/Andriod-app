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
import java.util.HashMap;
import java.util.Map;

public class ManageAdminsActivity extends AppCompatActivity {

    private static final String TAG = "ManageAdminsActivity";

    private RecyclerView adminRecyclerView;
    private AdminAdapter adminAdapter;
    private ArrayList<Admin> adminList;
    private Map<String, String> adminKeyMap;

    private DatabaseReference adminsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_admins);

        // Use a more specific path if your admins are not under the "admins" node
        // For example, if they are under "users" with a role of "admin"
        adminsRef = FirebaseDatabase.getInstance("https://finalproject-b08f4-default-rtdb.firebaseio.com/").getReference("users");

        adminRecyclerView = findViewById(R.id.rvAdmins);
        adminRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adminList = new ArrayList<>();
        adminKeyMap = new HashMap<>(); 

        adminAdapter = new AdminAdapter(adminList, this);
        adminRecyclerView.setAdapter(adminAdapter);

        loadAdmins();
    }

    private void loadAdmins() {
        // Query for users with the role "admin"
        adminsRef.orderByChild("role").equalTo("admin").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                adminList.clear();
                adminKeyMap.clear();
                
                if (!snapshot.exists()) {
                    Log.d(TAG, "No users with role 'admin' found.");
                    Toast.makeText(ManageAdminsActivity.this, "No admins found.", Toast.LENGTH_SHORT).show();
                    adminAdapter.notifyDataSetChanged(); // Ensure the adapter is cleared
                    return;
                }

                Log.d(TAG, "Found " + snapshot.getChildrenCount() + " admins.");

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Admin admin = dataSnapshot.getValue(Admin.class);
                    if (admin != null) {
                        String adminKey = dataSnapshot.getKey();
                        adminList.add(admin);
                        // Use a unique, non-null field for the key map, like email
                        if (admin.getEmail() != null) {
                            adminKeyMap.put(admin.getEmail(), adminKey);
                        }
                    }
                }
                adminAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load admins: " + error.getMessage());
                Toast.makeText(ManageAdminsActivity.this, "Failed to load admins.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void deleteAdmin(Admin admin) {
        if (admin.getEmail() == null) {
             Toast.makeText(this, "Cannot delete admin without an email.", Toast.LENGTH_SHORT).show();
             return;
        }

        String adminKey = adminKeyMap.get(admin.getEmail()); 
        if (adminKey != null) {
            adminsRef.child(adminKey).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ManageAdminsActivity.this, "Admin deleted successfully", Toast.LENGTH_SHORT).show();
                    // No need to manually remove, the ValueEventListener will trigger a refresh
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ManageAdminsActivity.this, "Failed to delete admin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        } else {
            Toast.makeText(ManageAdminsActivity.this, "Admin not found for deletion.", Toast.LENGTH_SHORT).show();
        }
    }
}
