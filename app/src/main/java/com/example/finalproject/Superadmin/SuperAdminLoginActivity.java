package com.example.finalproject.superadmin;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finalproject.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SuperAdminLoginActivity extends AppCompatActivity {

    private EditText txtEmail, txtPassword;
    private TextView txtForgotPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_super_admin_login);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        txtEmail = findViewById(R.id.txtSuperAdminEmail);
        txtPassword = findViewById(R.id.txtSuperAdminPassword);
        txtForgotPassword = findViewById(R.id.txtForgotPassword);
        btnLogin = findViewById(R.id.btnLoginSuperAdmin);
        progressBar = findViewById(R.id.progressBar);

        btnLogin.setOnClickListener(v -> loginSuperAdmin());
        txtForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
    }

    private void showForgotPasswordDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_forgot_password, null);
        final EditText resetMail = dialogView.findViewById(R.id.edtResetEmail);
        final ProgressBar progressBarDialog = dialogView.findViewById(R.id.progressBarDialog);

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("Reset Password")
            .setView(dialogView)
            .setPositiveButton("Send", null)
            .setNegativeButton("Cancel", (d, w) -> d.dismiss())
            .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(view -> {
                String mail = resetMail.getText().toString().trim();
                if (TextUtils.isEmpty(mail)) {
                    resetMail.setError("Email is required.");
                    return;
                }

                progressBarDialog.setVisibility(View.VISIBLE);
                positiveButton.setEnabled(false);

                mAuth.sendPasswordResetEmail(mail).addOnCompleteListener(task -> {
                    progressBarDialog.setVisibility(View.GONE);
                    positiveButton.setEnabled(true);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Reset link sent to your email.", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            });
        });
        dialog.show();
    }

    private void loginSuperAdmin() {
        String email = txtEmail.getText().toString().trim();
        String password = txtPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                if (firebaseUser != null) {
                    checkRoleAndProceed(firebaseUser.getUid());
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            } else {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkRoleAndProceed(String uid) {
        usersRef.child(uid).child("role").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressBar.setVisibility(View.GONE);
                String role = snapshot.getValue(String.class);
                
                if ("superadmin".equals(role)) {
                    Toast.makeText(SuperAdminLoginActivity.this, "Super Admin Login Successful!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SuperAdminLoginActivity.this, SuperAdminDashboard.class));
                    finish();
                } else {
                    Toast.makeText(SuperAdminLoginActivity.this, "Access Denied. Not a Super Admin account.", Toast.LENGTH_LONG).show();
                    mAuth.signOut();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(SuperAdminLoginActivity.this, "Database error. Please try again.", Toast.LENGTH_SHORT).show();
                mAuth.signOut();
            }
        });
    }
}
