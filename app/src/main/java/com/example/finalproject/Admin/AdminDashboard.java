package com.example.finalproject.admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;

import com.example.finalproject.MainActivity;
import com.example.finalproject.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminDashboard extends AppCompatActivity {
    private static final String TAG = "AdminDashboard";

    private TextView adminTitle;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase db = FirebaseDatabase.getInstance("https://finalproject-b08f4-default-rtdb.firebaseio.com/");
        usersRef = db.getReference("users");

        if (!checkAdminRole()) return;

        adminTitle = findViewById(R.id.tvAdminWelcome);

        findViewById(R.id.btnScanQr).setOnClickListener(v -> startActivity(new Intent(this, QRScannerActivity.class)));
        findViewById(R.id.btnViewAttendance).setOnClickListener(v -> startActivity(new Intent(this, AdminViewAttendanceActivity.class)));
        findViewById(R.id.btnViewMySection).setOnClickListener(v -> startActivity(new Intent(this, ViewMySectionActivity.class)));
        findViewById(R.id.btnAdminSignOut).setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(this, AdminLoginActivity.class));
            finish();
        });

        loadAdminName();
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
                    if (adminTitle != null) {
                        adminTitle.setText(!name.trim().isEmpty() ? "Welcome, " + name.trim() : "Admin Dashboard");
                    }
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
                if (!("admin".equals(role) || "superadmin".equals(role))) {
                    Toast.makeText(AdminDashboard.this, "Access denied", Toast.LENGTH_LONG).show();
                    mAuth.signOut();
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
        return true;
    }
}
