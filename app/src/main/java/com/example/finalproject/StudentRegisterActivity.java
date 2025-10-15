package com.example.finalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class StudentRegisterActivity extends AppCompatActivity {

    private EditText edtFirstName, edtLastName, edtStudentId, edtEmail, edtContactNumber, edtPassword, edtParentName, edtParentContactNumber;
    private Button btnRegister;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_register);

        // Initialize Views
        edtFirstName = findViewById(R.id.edtFirstName);
        edtLastName = findViewById(R.id.edtLastName);
        edtStudentId = findViewById(R.id.edtStudentId);
        edtEmail = findViewById(R.id.edtEmail);
        edtContactNumber = findViewById(R.id.edtContactNumber);
        edtPassword = findViewById(R.id.edtPassword);
        edtParentName = findViewById(R.id.edtParentName);
        edtParentContactNumber = findViewById(R.id.edtParentContactNumber);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance("https://finalproject-b08f4-default-rtdb.firebaseio.com/").getReference("users");

        btnRegister.setOnClickListener(v -> registerStudent());
    }

    private void registerStudent() {
        // --- 1. Get and Validate Input ---
        String firstName = edtFirstName.getText().toString().trim();
        String lastName = edtLastName.getText().toString().trim();
        String studentId = edtStudentId.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String contactNumber = edtContactNumber.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String parentName = edtParentName.getText().toString().trim();
        String parentContact = edtParentContactNumber.getText().toString().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || studentId.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showError("Please fill all required fields");
            return;
        }

        setInProgress(true);

        // --- 2. Create User in Firebase Auth ---
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                if (firebaseUser == null) {
                    showError("Registration succeeded but failed to get user session.");
                    return;
                }
                // --- 3. Save user data to Realtime Database ---
                saveUserToDatabase(firebaseUser, firstName, lastName, studentId, email, contactNumber, parentName, parentContact);
            } else {
                showError("Registration Failed: " + (task.getException() != null ? task.getException().getMessage() : "An unknown error occurred"));
            }
        });
    }

    private void saveUserToDatabase(FirebaseUser firebaseUser, String firstName, String lastName, String studentId, String email, String contactNumber, String parentName, String parentContact) {
        String uid = firebaseUser.getUid();

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("firstName", firstName);
        userMap.put("lastName", lastName);
        userMap.put("studentId", studentId);
        userMap.put("email", email);
        userMap.put("contactNumber", contactNumber);
        userMap.put("parentName", parentName);
        userMap.put("parentContactNumber", parentContact);
        userMap.put("role", "student");
        userMap.put("uid", uid);

        usersRef.child(uid).setValue(userMap).addOnCompleteListener(dbTask -> {
            if (dbTask.isSuccessful()) {
                // --- 4. Send Verification Email ---
                firebaseUser.sendEmailVerification().addOnCompleteListener(emailTask -> {
                    Toast.makeText(this, "Registration successful! Please check your email to verify.", Toast.LENGTH_LONG).show();
                    // --- 5. Sign Out and Redirect to Login ---
                    mAuth.signOut();
                    Intent intent = new Intent(StudentRegisterActivity.this, StudentLoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });
            } else {
                showError("Failed to save user data: " + (dbTask.getException() != null ? dbTask.getException().getMessage() : "Unknown database error"));
            }
        });
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        setInProgress(false);
    }

    private void setInProgress(boolean inProgress) {
        if (progressBar != null) {
            progressBar.setVisibility(inProgress ? View.VISIBLE : View.GONE);
        }
        if (btnRegister != null) {
            btnRegister.setEnabled(!inProgress);
        }
    }
}
