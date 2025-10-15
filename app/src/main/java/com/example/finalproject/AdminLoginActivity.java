package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
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
    private EditText edtEmail, edtPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        mAuth = FirebaseAuth.getInstance();
        
        edtEmail = findViewById(R.id.txtAdminEmail);
        edtPassword = findViewById(R.id.txtAdminPassword);
        btnLogin = findViewById(R.id.btnLoginAdmin);
        progressBar = findViewById(R.id.progressBarAdmin);

        if (edtEmail == null || edtPassword == null || btnLogin == null || progressBar == null) {
            Toast.makeText(this, "A critical layout view is missing. Cannot proceed.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        btnLogin.setOnClickListener(v -> loginUser());
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
                    checkUserRole(task.getResult().getUser());
                } else {
                    showError("Authentication Failed: " + task.getException().getMessage());
                }
            });
    }

    private void checkUserRole(FirebaseUser firebaseUser) {
        String userUid = firebaseUser.getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userUid);
        
        Log.d(TAG, "Checking role for UID: " + userUid);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String role = snapshot.child("role").getValue(String.class);
                    if ("admin".equalsIgnoreCase(role)) {
                        Toast.makeText(AdminLoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(AdminLoginActivity.this, AdminDashboard.class));
                        finish();
                    } else {
                        showError("Access Denied: Not an Admin account.");
                        mAuth.signOut();
                    }
                } else {
                    // *** THIS IS THE CRITICAL DEBUGGING MESSAGE ***
                    String errorMsg = "User data not found in Database for UID: " + userUid;
                    Log.e(TAG, errorMsg);
                    showError(errorMsg);
                    mAuth.signOut();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showError("Database error: " + error.getMessage());
                mAuth.signOut();
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
