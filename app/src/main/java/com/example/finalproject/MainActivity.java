package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

// Correctly importing the moved activities
import com.example.finalproject.admin.AdminLoginActivity;
import com.example.finalproject.student.StudentLoginActivity;
import com.example.finalproject.superadmin.SuperAdminLoginActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // --- THIS IS THE FIX: Ensuring all buttons are found and listeners are set correctly ---

        Button btnSuperAdmin = findViewById(R.id.btnSuperAdmin);
        Button btnAdmin = findViewById(R.id.btnAdmin);
        Button btnStudent = findViewById(R.id.btnStudent);

        if (btnSuperAdmin != null) {
            btnSuperAdmin.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, SuperAdminLoginActivity.class));
            });
        }

        if (btnAdmin != null) {
            btnAdmin.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, AdminLoginActivity.class));
            });
        }

        if (btnStudent != null) {
            btnStudent.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, StudentLoginActivity.class));
            });
        }
    }
}
