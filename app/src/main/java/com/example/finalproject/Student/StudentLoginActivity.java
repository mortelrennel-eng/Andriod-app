package com.example.finalproject.student;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finalproject.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StudentLoginActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private Button btnLogin;
    private TextView txtRegister, txtResendVerification;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_login);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        edtEmail = findViewById(R.id.txtStudentEmail);
        edtPassword = findViewById(R.id.txtStudentPassword);
        btnLogin = findViewById(R.id.btnLoginStudent);
        txtRegister = findViewById(R.id.txtGoToRegister);
        txtResendVerification = findViewById(R.id.txtResendVerification);
        progressBar = findViewById(R.id.progressBarStudent);

        btnLogin.setOnClickListener(v -> loginUser());

        txtRegister.setOnClickListener(v -> {
            startActivity(new Intent(StudentLoginActivity.this, RegisterStudentActivity.class));
        });

        txtResendVerification.setOnClickListener(v -> {
            // Logic to resend verification email
        });
    }

    private void loginUser() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null && user.isEmailVerified()) {
                        checkRoleAndProceed(user.getUid());
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(StudentLoginActivity.this, "Please verify your email address.", Toast.LENGTH_LONG).show();
                        txtResendVerification.setVisibility(View.VISIBLE);
                        mAuth.signOut();
                    }
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(StudentLoginActivity.this, "Authentication Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
    }

    private void checkRoleAndProceed(String uid) {
        usersRef.child(uid).child("role").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressBar.setVisibility(View.GONE);
                String role = snapshot.getValue(String.class);
                
                // --- THIS IS THE FIX: Check if role is 'student' ---
                if ("student".equals(role)) {
                    Toast.makeText(StudentLoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(StudentLoginActivity.this, StudentDashboard.class));
                    finish();
                } else {
                    Toast.makeText(StudentLoginActivity.this, "Access Denied. Not a Student account.", Toast.LENGTH_LONG).show();
                    mAuth.signOut();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(StudentLoginActivity.this, "Database error. Please try again.", Toast.LENGTH_SHORT).show();
                mAuth.signOut();
            }
        });
    }
}
