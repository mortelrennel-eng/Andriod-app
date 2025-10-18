package com.example.finalproject;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StudentLoginActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private Button btnLogin;
    private TextView txtRegister, txtResendVerification;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_login);

        mAuth = FirebaseAuth.getInstance();

        // *** THIS IS THE FIX: Using the correct IDs from the redesigned layout ***
        edtEmail = findViewById(R.id.txtStudentEmail);
        edtPassword = findViewById(R.id.txtStudentPassword);
        btnLogin = findViewById(R.id.btnLoginStudent);
        txtRegister = findViewById(R.id.txtGoToRegister);
        txtResendVerification = findViewById(R.id.txtResendVerification);
        progressBar = findViewById(R.id.progressBarStudent);

        // Defensive check to prevent crash
        if (edtEmail == null || edtPassword == null || btnLogin == null || txtRegister == null) {
            Toast.makeText(this, "A critical layout view is missing. Cannot proceed.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        btnLogin.setOnClickListener(v -> loginUser());

        txtRegister.setOnClickListener(v -> {
            startActivity(new Intent(StudentLoginActivity.this, StudentRegisterActivity.class));
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

        setInProgress(true);

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null && user.isEmailVerified()) {
                        Toast.makeText(StudentLoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(StudentLoginActivity.this, StudentDashboard.class));
                        finish();
                    } else {
                        showError("Please verify your email address.");
                        txtResendVerification.setVisibility(View.VISIBLE);
                        mAuth.signOut();
                    }
                } else {
                    showError("Authentication Failed: " + task.getException().getMessage());
                }
            });
    }

    private void setInProgress(boolean inProgress) {
        progressBar.setVisibility(inProgress ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!inProgress);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        setInProgress(false);
    }
}
