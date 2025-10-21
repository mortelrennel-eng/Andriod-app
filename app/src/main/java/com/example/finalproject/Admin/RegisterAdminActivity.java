package com.example.finalproject.admin;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.finalproject.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterAdminActivity extends AppCompatActivity {

    private EditText adminFirstName, adminLastName, adminEmail, adminPassword, adminSection;
    private Button registerBtn;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_admin);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance("https://finalproject-b08f4-default-rtdb.firebaseio.com/").getReference("users");

        adminFirstName = findViewById(R.id.adminFirstName);
        adminLastName = findViewById(R.id.adminLastName);
        adminEmail = findViewById(R.id.adminEmail);
        adminPassword = findViewById(R.id.adminPassword);
        adminSection = findViewById(R.id.adminSection);
        registerBtn = findViewById(R.id.registerBtn);
        progressBar = findViewById(R.id.progressBarAdminRegister);

        registerBtn.setOnClickListener(v -> {
            // ... (Registration logic)
        });
    }
}
