package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminDashboard extends AppCompatActivity {
    private static final String TAG = "AdminDashboard";

    private TextView adminTitle;
    private MaterialButton btnManageStudents;
    private MaterialButton btnSignOut;
    private MaterialButton btnScanQr;
    private MaterialButton btnManageEbooks;
    private MaterialButton btnViewUsers;
    private MaterialButton btnConnection;
    private MaterialButton btnManageAttendance;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // initialize Firebase first so role guard can access the database
        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase db = FirebaseDatabase.getInstance("https://finalproject-b08f4-default-rtdb.firebaseio.com/");
        usersRef = db.getReference("users");

        // guard: ensure signed-in user is admin or superadmin
        if (!checkAdminRole()) return;

        adminTitle = findViewById(R.id.tvAdminTitle);
        btnManageStudents = findViewById(R.id.btnManageStudents);
        btnSignOut = findViewById(R.id.btnAdminSignOut);
        btnScanQr = findViewById(R.id.btnScanQr);
        btnManageAttendance = findViewById(R.id.btnManageAttendance);
        btnViewUsers = findViewById(R.id.btnViewUsers);
        btnConnection = findViewById(R.id.btnConnection);

        btnSignOut.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(AdminDashboard.this, AdminLoginActivity.class));
            finish();
        });

        btnManageStudents.setOnClickListener(v -> startActivity(new Intent(AdminDashboard.this, ManageStudentsActivity.class)));

        btnScanQr.setOnClickListener(v -> startActivity(new Intent(this, QRScannerActivity.class)));
        btnManageAttendance.setOnClickListener(v -> startActivity(new Intent(AdminDashboard.this, ManageAttendanceActivity.class)));

        btnViewUsers.setOnClickListener(v -> startActivity(new Intent(AdminDashboard.this, UsersListActivity.class)));

        btnConnection.setOnClickListener(v -> checkConnection());

        loadAdminName();
    }

    private void checkConnection() {
        // quick read attempt to verify RTDB connectivity
        DatabaseReference pingRef = usersRef.getRoot().child("ping");
        pingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Toast.makeText(AdminDashboard.this, "Connected to database", Toast.LENGTH_SHORT).show();
                adminTitle.setText("Admin Dashboard — Connected");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminDashboard.this, "Connection failed: " + (error.getMessage() != null ? error.getMessage() : "unknown"), Toast.LENGTH_LONG).show();
                adminTitle.setText("Admin Dashboard — Disconnected");
            }
        });
    }

    private void loadAdminName() {
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, AdminLoginActivity.class));
            finish();
            return;
        }

        String uid = mAuth.getCurrentUser().getUid();
        usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String first = snapshot.child("firstName").getValue(String.class);
                    String last = snapshot.child("lastName").getValue(String.class);
                    String name = (first != null ? first : "") + " " + (last != null ? last : "");
                    adminTitle.setText(!name.trim().isEmpty() ? "Admin: " + name.trim() : "Admin Dashboard");
                } else {
                    adminTitle.setText("Admin Dashboard");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "loadAdmin:onCancelled", error.toException());
                Toast.makeText(AdminDashboard.this, "Failed loading admin profile.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean checkAdminRole() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Not signed in", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, AdminLoginActivity.class));
            finish();
            return false;
        }
        String uid = mAuth.getCurrentUser().getUid();
        usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String role = snapshot.child("role").getValue(String.class);
                if (role == null || !(role.equals("admin") || role.equals("superadmin"))) {
                    Toast.makeText(AdminDashboard.this, "Access denied", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(AdminDashboard.this, AdminLoginActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminDashboard.this, "Database error", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(AdminDashboard.this, AdminLoginActivity.class));
                finish();
            }
        });
        return true; // optimistic; actual finish happens in callbacks
    }
}
