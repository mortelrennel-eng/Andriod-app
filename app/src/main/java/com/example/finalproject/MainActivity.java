package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // *** THIS IS THE FIX: Using the correct ID with a capital 'A' ***
        Button btnSuperAdmin = findViewById(R.id.btnSuperAdmin);
        Button btnAdmin = findViewById(R.id.btnAdmin);
        Button btnStudent = findViewById(R.id.btnStudent);

        btnSuperAdmin.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SuperAdminLoginActivity.class));
        });

        btnAdmin.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AdminLoginActivity.class));
        });

        btnStudent.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, StudentLoginActivity.class));
        });
    }
}
