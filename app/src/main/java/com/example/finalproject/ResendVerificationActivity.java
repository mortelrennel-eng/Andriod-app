package com.example.finalproject;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ResendVerificationActivity extends AppCompatActivity {

    private static final String TAG = "ResendVerification";
    private EditText edtEmail, edtPassword;
    private Button btnSignInResend;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resend_verification);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnSignInResend = findViewById(R.id.btnSignInResend);
        progressBar = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();

        btnSignInResend.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }
            setInProgress(true);
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null && !user.isEmailVerified()) {
                        user.sendEmailVerification().addOnCompleteListener(this, sendTask -> {
                            if (sendTask.isSuccessful()) {
                                Toast.makeText(this, "Verification email sent.", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(this, "Failed to send: " + (sendTask.getException() != null ? sendTask.getException().getMessage() : "unknown"), Toast.LENGTH_LONG).show();
                                Log.e(TAG, "sendEmailVerification failed", sendTask.getException());
                            }
                            mAuth.signOut();
                            setInProgress(false);
                        });
                    } else if (user != null && user.isEmailVerified()) {
                        Toast.makeText(this, "Email already verified.", Toast.LENGTH_LONG).show();
                        mAuth.signOut();
                        setInProgress(false);
                    } else {
                        Toast.makeText(this, "Sign-in succeeded but user is null.", Toast.LENGTH_LONG).show();
                        mAuth.signOut();
                        setInProgress(false);
                    }
                } else {
                    Toast.makeText(this, "Sign-in failed: " + (task.getException() != null ? task.getException().getMessage() : "unknown"), Toast.LENGTH_LONG).show();
                    setInProgress(false);
                }
            });
        });
    }

    private void setInProgress(boolean inProgress) {
        if (progressBar != null) progressBar.setVisibility(inProgress ? View.VISIBLE : View.GONE);
        if (btnSignInResend != null) btnSignInResend.setEnabled(!inProgress);
    }
}
