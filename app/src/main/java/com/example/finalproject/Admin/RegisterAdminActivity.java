package com.example.finalproject.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.finalproject.R;
import com.example.finalproject.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterAdminActivity extends AppCompatActivity {

    private EditText adminFirstName, adminLastName, adminEmail, adminPassword;
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
        registerBtn = findViewById(R.id.registerBtn);
        progressBar = findViewById(R.id.progressBarAdminRegister);

        registerBtn.setOnClickListener(v -> registerAdmin());
    }

    private void registerAdmin() {
        String firstName = adminFirstName.getText().toString().trim();
        String lastName = adminLastName.getText().toString().trim();
        String email = adminEmail.getText().toString().trim();
        String password = adminPassword.getText().toString().trim();

        if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        registerBtn.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    if (firebaseUser != null) {
                        String uid = firebaseUser.getUid();
                        // Create a User object with the role "admin"
                        User adminUser = new User(firstName, lastName, null, null, email, null, null, null, "admin", uid);

                        usersRef.child(uid).setValue(adminUser).addOnCompleteListener(dbTask -> {
                            progressBar.setVisibility(View.GONE);
                            registerBtn.setEnabled(true);

                            if (dbTask.isSuccessful()) {
                                Toast.makeText(this, "Admin registered successfully.", Toast.LENGTH_LONG).show();
                                mAuth.signOut(); // Sign out to prevent auto-login of the new account
                                finish(); // Go back to the super admin dashboard
                            } else {
                                Toast.makeText(this, "Database error: " + dbTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    progressBar.setVisibility(View.GONE);
                    registerBtn.setEnabled(true);
                    Toast.makeText(this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
    }
}
