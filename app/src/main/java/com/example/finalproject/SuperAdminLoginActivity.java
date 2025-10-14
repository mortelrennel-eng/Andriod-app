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

public class SuperAdminLoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton, registerButton;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_super_admin_login);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance("https://finalproject-b08f4-default-rtdb.firebaseio.com/").getReference("users");

        emailEditText = findViewById(R.id.txtSuperAdminEmail);
        passwordEditText = findViewById(R.id.txtSuperAdminPassword);
        loginButton = findViewById(R.id.btnLoginSuperAdmin);
        registerButton = findViewById(R.id.btnRegisterSuperAdmin);

        loginButton.setOnClickListener(v -> loginSuperAdmin());

        // Restore the functionality to go to the registration page
        registerButton.setOnClickListener(v -> {
            startActivity(new Intent(SuperAdminLoginActivity.this, RegisterSuperAdminActivity.class));
        });
    }

    private void loginSuperAdmin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();
                    usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String role = snapshot.child("role").getValue(String.class);
                                if ("superadmin".equals(role)) {
                                    // Role is correct, proceed to dashboard
                                    Toast.makeText(SuperAdminLoginActivity.this, "Super Admin login successful!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(SuperAdminLoginActivity.this, SuperAdminDashboard.class));
                                    finish();
                                } else {
                                    // Role is not superadmin, deny access
                                    mAuth.signOut(); // Sign out the user
                                    Toast.makeText(SuperAdminLoginActivity.this, "Access Denied: Not a Super Admin.", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                // User exists in Auth but not in the database for roles
                                mAuth.signOut();
                                Toast.makeText(SuperAdminLoginActivity.this, "Access Denied: User record not found.", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            // Handle database error
                            mAuth.signOut();
                            Toast.makeText(SuperAdminLoginActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    // Handle authentication failure
                    Toast.makeText(SuperAdminLoginActivity.this, "Login Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
