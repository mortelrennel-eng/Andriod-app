package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class StudentSignupActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput, nameInput;
    private Button signupBtn;
    private TextView loginText;
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_signup);

        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        signupBtn = findViewById(R.id.signupBtn);
        loginText = findViewById(R.id.loginText);

    mAuth = FirebaseAuth.getInstance();
    dbRef = FirebaseDatabase.getInstance("https://finalproject-b08f4-default-rtdb.firebaseio.com/").getReference("users");

        signupBtn.setOnClickListener(v -> registerStudent());

        loginText.setOnClickListener(v -> {
            Toast.makeText(this, "Redirecting to Login", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, StudentLoginActivity.class));
            finish();
        });
    }

    private void registerStudent() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String name = nameInput.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(name)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    // Save student data in Firebase Database
                    String uid = mAuth.getCurrentUser().getUid();
                    HashMap<String, Object> studentData = new HashMap<>();
                    studentData.put("uid", uid);
                    studentData.put("name", name);
                    studentData.put("email", email);
                    studentData.put("role", "student");

                    // Save to RTDB and then send verification
                    dbRef.child(uid).setValue(studentData)
                            .addOnSuccessListener(aVoid -> {
                                if (mAuth.getCurrentUser() != null) {
                                    mAuth.getCurrentUser().sendEmailVerification()
                                            .addOnSuccessListener(v -> {
                                                Toast.makeText(this, "Verification email sent. Please verify your email.", Toast.LENGTH_LONG).show();
                                                mAuth.signOut();
                                                startActivity(new Intent(this, StudentLoginActivity.class));
                                                finish();
                                            })
                                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to send verification: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                } else {
                                    Toast.makeText(this, "Registration completed. Please login and verify your email.", Toast.LENGTH_LONG).show();
                                    startActivity(new Intent(this, StudentLoginActivity.class));
                                    finish();
                                }
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to save user: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
