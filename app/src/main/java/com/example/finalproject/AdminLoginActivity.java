package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminLoginActivity extends AppCompatActivity {

    private static final String TAG = "AdminLoginActivity";

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView registerLink;
    private TextView resendVerificationLink;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    // Remove fields for DB references to keep them local and reduce lint warnings

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

    mAuth = FirebaseAuth.getInstance();

    emailEditText = findViewById(R.id.txtAdminEmail);
        passwordEditText = findViewById(R.id.txtAdminPassword);
        loginButton = findViewById(R.id.btnLoginAdmin);
        registerLink = findViewById(R.id.txtGoToRegister);
    resendVerificationLink = findViewById(R.id.txtResendVerification);
        progressBar = findViewById(R.id.progressBarAdmin);

        loginButton.setOnClickListener(v -> loginAdmin());
        if (registerLink != null) {
            registerLink.setOnClickListener(v -> startActivity(new Intent(this, RegisterAdminActivity.class)));
        }
        if (resendVerificationLink != null) {
            resendVerificationLink.setOnClickListener(v -> resendVerification());
        }
    }

    private void loginAdmin() {
        String email = emailEditText.getText() != null ? emailEditText.getText().toString().trim() : "";
        String password = passwordEditText.getText() != null ? passwordEditText.getText().toString() : "";

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show();
            return;
        }

    loginButton.setEnabled(false);
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
    if (resendVerificationLink != null) resendVerificationLink.setVisibility(View.GONE);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> handleSignInResult(task))
                .addOnFailureListener(e -> {
                    Log.w(TAG, "signIn:onFailure", e);
                    Toast.makeText(this, e.getMessage() != null ? e.getMessage() : "Login failed.", Toast.LENGTH_LONG).show();
                    resetUi();
                });
    }

    private void handleSignInResult(com.google.android.gms.tasks.Task<com.google.firebase.auth.AuthResult> task) {
        if (!task.isSuccessful() || task.getResult() == null || task.getResult().getUser() == null) {
            String msg = "Invalid email or password.";
            if (task.getException() != null) {
                msg = task.getException().getMessage();
                Log.w(TAG, "signIn:fail", task.getException());
            }
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            resetUi();
            return;
        }

        String uid = task.getResult().getUser().getUid();

        // check email verification first
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && !currentUser.isEmailVerified()) {
            Toast.makeText(this, "Please verify your email before logging in. Check your inbox.", Toast.LENGTH_LONG).show();
            // show resend option
            if (resendVerificationLink != null) resendVerificationLink.setVisibility(View.VISIBLE);
            mAuth.signOut();
            resetUi();
            return;
        }

        // read role from Realtime Database (use local reference)
        FirebaseDatabase db = FirebaseDatabase.getInstance("https://finalproject-b08f4-default-rtdb.firebaseio.com/");
        DatabaseReference usersRefLocal = db.getReference("users");

        usersRefLocal.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot == null || !snapshot.exists()) {
                    Toast.makeText(AdminLoginActivity.this, "User data not found.", Toast.LENGTH_LONG).show();
                    mAuth.signOut();
                    resetUi();
                    return;
                }

                String role = "user";
                if (snapshot.child("role").getValue() != null) role = snapshot.child("role").getValue().toString();

                if ("admin".equals(role) || "superadmin".equals(role)) {
                    Toast.makeText(AdminLoginActivity.this, "Admin login successful!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(AdminLoginActivity.this, AdminDashboard.class));
                    finish();
                } else {
                    Toast.makeText(AdminLoginActivity.this, "Login successful.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(AdminLoginActivity.this, StudentDashboard.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "fetchUser:onCancelled", error.toException());
                Toast.makeText(AdminLoginActivity.this, error.getMessage() != null ? error.getMessage() : "Database error", Toast.LENGTH_LONG).show();
                mAuth.signOut();
                resetUi();
            }
        });
    }

    private void resendVerification() {
        // attempt to resend verification to the email in the emailEditText
        String email = emailEditText.getText() != null ? emailEditText.getText().toString().trim() : "";
        String password = passwordEditText.getText() != null ? passwordEditText.getText().toString() : "";
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter your email and password to resend verification.", Toast.LENGTH_SHORT).show();
            return;
        }

        // sign in silently to send verification if possible
        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    if (!task.isSuccessful() || task.getResult() == null || task.getResult().getUser() == null) {
                        Toast.makeText(this, "Unable to sign in to resend verification.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    FirebaseUser u = mAuth.getCurrentUser();
                    if (u != null && !u.isEmailVerified()) {
                        u.sendEmailVerification()
                                .addOnCompleteListener(v -> {
                                    if (v.isSuccessful()) {
                                        Toast.makeText(AdminLoginActivity.this, "Verification email resent. Check your inbox.", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(AdminLoginActivity.this, "Failed to resend verification.", Toast.LENGTH_LONG).show();
                                    }
                                    mAuth.signOut();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(AdminLoginActivity.this, e.getMessage() != null ? e.getMessage() : "Failed to resend verification.", Toast.LENGTH_LONG).show();
                                    mAuth.signOut();
                                });
                    } else {
                        Toast.makeText(this, "Account already verified or not accessible.", Toast.LENGTH_SHORT).show();
                        if (u != null) mAuth.signOut();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, e.getMessage() != null ? e.getMessage() : "Error signing in.", Toast.LENGTH_LONG).show();
                });
    }

    private void resetUi() {
        loginButton.setEnabled(true);
        if (progressBar != null) progressBar.setVisibility(View.GONE);
    }
}
