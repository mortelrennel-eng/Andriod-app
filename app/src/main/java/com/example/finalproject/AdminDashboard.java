package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;

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

        // --- THIS IS THE FIX: Pointing the button to the new, correct Activity ---
        findViewById(R.id.btnScanQr).setOnClickListener(v -> startActivity(new Intent(this, QRScannerActivity.class)));
        findViewById(R.id.btnViewAttendance).setOnClickListener(v -> startActivity(new Intent(AdminDashboard.this, AdminViewAttendanceActivity.class))); // Changed this line
        findViewById(R.id.btnViewMySection).setOnClickListener(v -> startActivity(new Intent(AdminDashboard.this, ViewMySectionActivity.class)));
        findViewById(R.id.btnAdminSignOut).setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(AdminDashboard.this, AdminLoginActivity.class));
            finish();
        });

        loadAdminName();
    }

    private void loadAdminName() {
        // ... (This method remains the same)
    }

    private boolean checkAdminRole() {
        // ... (This method remains the same)
        return true;
    }
}
