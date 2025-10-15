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

public class SuperAdminLoginActivity extends AppCompatActivity {

    private static final String TAG = "SuperAdminLogin";
    private EditText edtEmail, edtPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_super_admin_login);

        mAuth = FirebaseAuth.getInstance();

        edtEmail = findViewById(R.id.txtSuperAdminEmail);
        edtPassword = findViewById(R.id.txtSuperAdminPassword);
        btnLogin = findViewById(R.id.btnLoginSuperAdmin);
        progressBar = findViewById(R.id.progressBar); // This might be null, handled in setInProgress

        if (edtEmail == null || edtPassword == null || btnLogin == null) {
            Toast.makeText(this, "A critical layout view is missing.", Toast.LENGTH_LONG).show();
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
                    if ("superadmin".equalsIgnoreCase(role)) {
                        Toast.makeText(SuperAdminLoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SuperAdminLoginActivity.this, SuperAdminDashboard.class));
                        finish();
                    } else {
                        showError("Access Denied: Not a Super Admin account.");
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
        if (progressBar != null) {
             progressBar.setVisibility(inProgress ? View.VISIBLE : View.GONE);
        }
        btnLogin.setEnabled(!inProgress);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        setInProgress(false);
    }
}
