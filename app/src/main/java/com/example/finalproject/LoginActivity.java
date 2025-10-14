package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    EditText emailField, passwordField;
    Button loginBtn;
    FirebaseAuth auth;
    FirebaseDatabase realtimeDb;
    DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
        loginBtn = findViewById(R.id.loginBtn);

    auth = FirebaseAuth.getInstance();
    realtimeDb = FirebaseDatabase.getInstance("https://finalproject-b08f4-default-rtdb.firebaseio.com/");
    usersRef = realtimeDb.getReference("users");

        loginBtn.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user == null) {
                        Toast.makeText(this, "Login failed.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // enforce email verification
                    if (!user.isEmailVerified()) {
                        Toast.makeText(this, "Please verify your email before logging in.", Toast.LENGTH_LONG).show();
                        auth.signOut();
                        return;
                    }

                    String uid = user.getUid();
                    usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (snapshot == null || !snapshot.exists()) {
                                Toast.makeText(LoginActivity.this, "No role found.", Toast.LENGTH_SHORT).show();
                                auth.signOut();
                                return;
                            }
                            String role = snapshot.child("role").getValue(String.class);
                            routeUser(role);
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            Toast.makeText(LoginActivity.this, "Database error: " + (error.getMessage() != null ? error.getMessage() : ""), Toast.LENGTH_LONG).show();
                            auth.signOut();
                        }
                    });
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void routeUser(String role) {
        if ("superadmin".equals(role)) {
            startActivity(new Intent(this, SuperAdminDashboard.class));
        } else if ("admin".equals(role)) {
            startActivity(new Intent(this, AdminDashboard.class));
        } else if ("student".equals(role)) {
            startActivity(new Intent(this, StudentDashboard.class));
        } else {
            Toast.makeText(this, "Unknown role", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}
