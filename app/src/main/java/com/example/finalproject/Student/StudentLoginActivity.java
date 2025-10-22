package com.example.finalproject.student;

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
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StudentLoginActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private TextView txtForgotPassword, txtGoToRegister, txtResendVerification;
    private Button btnLogin;
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
        txtForgotPassword = findViewById(R.id.txtForgotPassword);
        btnLogin = findViewById(R.id.btnLoginStudent);
        txtGoToRegister = findViewById(R.id.txtGoToRegister);
        txtResendVerification = findViewById(R.id.txtResendVerification);
        progressBar = findViewById(R.id.progressBarStudent);

        btnLogin.setOnClickListener(v -> loginUser());
        txtGoToRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterStudentActivity.class)));
        txtForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
        txtResendVerification.setOnClickListener(v -> resendVerificationEmail());
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
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null && user.isEmailVerified()) {
                        checkRoleAndProceed(user.getUid());
                    } else {
                        Toast.makeText(StudentLoginActivity.this, "Login Failed: Please verify your email first.", Toast.LENGTH_LONG).show();
                        txtResendVerification.setVisibility(View.VISIBLE);
                        mAuth.signOut();
                    }
                } else {
                    String errorMessage = "Authentication Failed.";
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthInvalidUserException e) {
                        errorMessage = "No account found with this email.";
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        errorMessage = "Incorrect password. Please try again.";
                    } catch (Exception e) {
                        errorMessage = e.getMessage();
                    }
                    Toast.makeText(StudentLoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            });
    }

    private void resendVerificationEmail() {
       FirebaseUser user = mAuth.getCurrentUser(); 
        if(user != null){
             user.sendEmailVerification().addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    Toast.makeText(this, "Verification email sent.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
             Toast.makeText(this, "Cannot send email. Please try logging in again.", Toast.LENGTH_LONG).show();
        }
    }

    private void checkRoleAndProceed(String uid) {
        usersRef.child(uid).child("role").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressBar.setVisibility(View.GONE);
                String role = snapshot.getValue(String.class);
                if ("student".equals(role)) {
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
