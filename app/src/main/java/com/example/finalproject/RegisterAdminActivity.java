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

public class RegisterAdminActivity extends AppCompatActivity {

    EditText adminFirstName, adminLastName, adminEmail, adminSection, adminPassword;
    Button registerBtn;
    FirebaseAuth auth;
    FirebaseDatabase realtimeDb;
    DatabaseReference usersRef;
    android.widget.ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_admin);

        adminFirstName = findViewById(R.id.adminFirstName);
        adminLastName = findViewById(R.id.adminLastName);
        adminEmail = findViewById(R.id.adminEmail);
        adminSection = findViewById(R.id.adminSection);
        adminPassword = findViewById(R.id.adminPassword);
        registerBtn = findViewById(R.id.registerBtn);
        progressBar = findViewById(R.id.progressBarAdminRegister);

        auth = FirebaseAuth.getInstance();
        realtimeDb = FirebaseDatabase.getInstance("https://finalproject-b08f4-default-rtdb.firebaseio.com/");
        usersRef = realtimeDb.getReference("users");

        registerBtn.setOnClickListener(v -> registerAdmin());
    }

    private void registerAdmin() {
        String firstName = adminFirstName.getText().toString().trim();
        String lastName = adminLastName.getText().toString().trim();
        String email = adminEmail.getText().toString().trim();
        String section = adminSection.getText().toString().trim();
        String password = adminPassword.getText().toString();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || section.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters.", Toast.LENGTH_SHORT).show();
            return;
        }

        registerBtn.setEnabled(false);
        progressBar.setVisibility(android.view.View.VISIBLE);

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    String uid = result.getUser().getUid();
                    Map<String, Object> data = new HashMap<>();
                    data.put("firstName", firstName);
                    data.put("lastName", lastName);
                    data.put("email", email);
                    data.put("role", "admin");
                    data.put("section", section);

                    usersRef.child(uid).setValue(data)
                            .addOnSuccessListener(a -> {
                                if (auth.getCurrentUser() != null) {
                                    auth.getCurrentUser().sendEmailVerification()
                                            .addOnCompleteListener(v -> {
                                                progressBar.setVisibility(android.view.View.GONE);
                                                registerBtn.setEnabled(true);
                                                auth.signOut();
                                                startActivity(new Intent(RegisterAdminActivity.this, AdminRegistrationCompleteActivity.class));
                                                finish();
                                            });
                                }
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(android.view.View.GONE);
                                registerBtn.setEnabled(true);
                                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(android.view.View.GONE);
                    registerBtn.setEnabled(true);
                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
