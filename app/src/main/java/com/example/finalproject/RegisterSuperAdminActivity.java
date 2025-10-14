package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegisterSuperAdminActivity extends AppCompatActivity {

    EditText superAdminFirstName, superAdminLastName, superAdminEmail, superAdminSection, superAdminPassword;
    Button registerSuperAdminBtn;
    FirebaseAuth auth;
    FirebaseDatabase realtimeDb;
    DatabaseReference usersRef;
    android.widget.ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_super_admin);

        superAdminFirstName = findViewById(R.id.superAdminFirstName);
        superAdminLastName = findViewById(R.id.superAdminLastName);
        superAdminEmail = findViewById(R.id.superAdminEmail);
        superAdminSection = findViewById(R.id.superAdminSection);
        superAdminPassword = findViewById(R.id.superAdminPassword);
        registerSuperAdminBtn = findViewById(R.id.registerSuperAdminBtn);
        progressBar = findViewById(R.id.progressBarSuperAdminRegister);

        auth = FirebaseAuth.getInstance();
        realtimeDb = FirebaseDatabase.getInstance("https://finalproject-b08f4-default-rtdb.firebaseio.com/");
        usersRef = realtimeDb.getReference("users");

        registerSuperAdminBtn.setOnClickListener(v -> registerSuperAdmin());
    }

    private void registerSuperAdmin() {
        String firstName = superAdminFirstName.getText().toString().trim();
        String lastName = superAdminLastName.getText().toString().trim();
        String email = superAdminEmail.getText().toString().trim();
        String section = superAdminSection.getText().toString().trim();
        String password = superAdminPassword.getText().toString();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || section.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters.", Toast.LENGTH_SHORT).show();
            return;
        }

        registerSuperAdminBtn.setEnabled(false);
        progressBar.setVisibility(android.view.View.VISIBLE);

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    String uid = result.getUser().getUid();
                    Map<String, Object> data = new HashMap<>();
                    data.put("firstName", firstName);
                    data.put("lastName", lastName);
                    data.put("email", email);
                    data.put("role", "superadmin"); // Set role to superadmin
                    data.put("section", section);

                    usersRef.child(uid).setValue(data)
                            .addOnSuccessListener(a -> {
                                progressBar.setVisibility(android.view.View.GONE);
                                registerSuperAdminBtn.setEnabled(true);
                                Toast.makeText(this, "Super Admin registered successfully!", Toast.LENGTH_LONG).show();
                                finish(); // Go back to the dashboard
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(android.view.View.GONE);
                                registerSuperAdminBtn.setEnabled(true);
                                Toast.makeText(this, "Database Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(android.view.View.GONE);
                    registerSuperAdminBtn.setEnabled(true);
                    Toast.makeText(this, "Registration Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
