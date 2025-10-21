package com.example.finalproject.superadmin;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.finalproject.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegisterSuperAdminActivity extends AppCompatActivity {

    private EditText superAdminEmail, superAdminPassword, superAdminFirstName, superAdminLastName;
    private Button registerBtn;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_super_admin);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance("https://finalproject-b08f4-default-rtdb.firebaseio.com/").getReference("users");

        superAdminEmail = findViewById(R.id.superAdminEmail);
        superAdminPassword = findViewById(R.id.superAdminPassword);
        superAdminFirstName = findViewById(R.id.superAdminFirstName);
        superAdminLastName = findViewById(R.id.superAdminLastName);
        registerBtn = findViewById(R.id.registerBtn);

        registerBtn.setOnClickListener(v -> {
            // ... (Registration logic)
        });
    }
}
