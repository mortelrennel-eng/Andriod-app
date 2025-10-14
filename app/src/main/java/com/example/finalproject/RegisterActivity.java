package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseUser;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.content.Context;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";

    private TextInputEditText firstNameEt, lastNameEt, studentIdEt, contactEt, parentNameEt, parentContactEt;
    private TextInputEditText emailEt, passEt, confirmPassEt;
    private Button registerBtn;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    // Realtime Database reference
    private FirebaseDatabase realtimeDb;
    private DatabaseReference usersRef;
    // timeout handler to recover from hanging network calls
    private final Handler timeoutHandler = new Handler(Looper.getMainLooper());
    private Runnable timeoutRunnable;
    private static final long NETWORK_TIMEOUT_MS = 30_000; // 30 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firstNameEt = findViewById(R.id.txtFirstName);
        lastNameEt = findViewById(R.id.txtLastName);
        studentIdEt = findViewById(R.id.txtStudentId);
        contactEt = findViewById(R.id.txtContactNumber);
        parentNameEt = findViewById(R.id.txtParentName);
        parentContactEt = findViewById(R.id.txtParentContact);

        emailEt = findViewById(R.id.txtEmail);
        passEt = findViewById(R.id.txtPassword);
        confirmPassEt = findViewById(R.id.txtConfirmPassword);

        registerBtn = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBarRegister);

    mAuth = FirebaseAuth.getInstance();
    // initialize Realtime Database with explicit URL
    realtimeDb = FirebaseDatabase.getInstance("https://finalproject-b08f4-default-rtdb.firebaseio.com/");
    usersRef = realtimeDb.getReference("users");

        registerBtn.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String firstName = text(firstNameEt);
        String lastName = text(lastNameEt);
        String studentId = text(studentIdEt);
        String contact = text(contactEt);
        String parentName = text(parentNameEt);
        String parentContact = text(parentContactEt);
        String email = text(emailEt);
        String password = text(passEt);
        String confirm = text(confirmPassEt);

        // basic required checks
        if (firstName.isEmpty() || lastName.isEmpty() || studentId.isEmpty() ||
                contact.isEmpty() || parentName.isEmpty() || parentContact.isEmpty() ||
                email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Enter a valid email.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirm)) {
            Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Student ID format: 000-000
        if (!studentId.matches("\\d{3}-\\d{3}")) {
            Toast.makeText(this, "Student ID must be in format 000-000.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Contact number validation: digits only, reasonable length (7-13)
        if (!contact.matches("\\d{7,13}")) {
            Toast.makeText(this, "Contact number must be digits (7–13 chars).", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!parentContact.matches("\\d{7,13}")) {
            Toast.makeText(this, "Parent contact must be digits (7–13 chars).", Toast.LENGTH_SHORT).show();
            return;
        }

        // check connectivity first
        if (!isConnected()) {
            Toast.makeText(this, "No internet connection. Please connect and try again.", Toast.LENGTH_LONG).show();
            return;
        }

        // UI lock
        registerBtn.setEnabled(false);
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
    Log.d(TAG, "Starting registration for: " + email);

    // start timeout watchdog
    startNetworkTimeout();

        try {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        cancelNetworkTimeout();
                    if (!task.isSuccessful() || task.getResult() == null || task.getResult().getUser() == null) {
                        String msg = "Registration failed.";
                        if (task.getException() != null) {
                            msg = task.getException().getMessage();
                            Log.w(TAG, "createUser:fail", task.getException());
                        }
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                        resetUi();
                        return;
                    }

                    String uid = task.getResult().getUser().getUid();
                        Log.d(TAG, "Auth succeeded, uid=" + uid + " - writing user data to Firestore");

                        // start timeout again for Firestore write
                        startNetworkTimeout();

                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("firstName", firstName);
                    userMap.put("lastName", lastName);
                    userMap.put("studentId", studentId);
                    userMap.put("contactNumber", contact);
                    userMap.put("parentName", parentName);
                    userMap.put("parentContact", parentContact);
                    userMap.put("email", email);
                    userMap.put("role", "student");
                    userMap.put("createdAt", System.currentTimeMillis());
                    // write to Realtime Database under /users/{uid}
                    usersRef.child(uid).setValue(userMap)
                            .addOnCompleteListener(dbTask -> {
                                cancelNetworkTimeout();
                                if (!dbTask.isSuccessful()) {
                                    String m = "Failed saving user data.";
                                    if (dbTask.getException() != null) {
                                        m = dbTask.getException().getMessage();
                                        Log.w(TAG, "saveUser:fail", dbTask.getException());
                                    }
                                    // cleanup orphan auth
                                    if (mAuth.getCurrentUser() != null) mAuth.getCurrentUser().delete();
                                    Toast.makeText(this, m, Toast.LENGTH_LONG).show();
                                    resetUi();
                                    return;
                                }

                                // send verification email
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    startNetworkTimeout();
                                    user.sendEmailVerification()
                                            .addOnCompleteListener(verifyTask -> {
                                                cancelNetworkTimeout();
                                                if (verifyTask.isSuccessful()) {
                                                    Toast.makeText(this, "Registration successful. A verification email has been sent. Please verify your email before logging in.", Toast.LENGTH_LONG).show();
                                                } else {
                                                    String msg = "Failed to send verification email.";
                                                    if (verifyTask.getException() != null) msg = verifyTask.getException().getMessage();
                                                    Log.w(TAG, "sendEmailVerification:fail", verifyTask.getException());
                                                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                                                }
                                                // sign out so user must verify before logging in
                                                mAuth.signOut();
                                                resetUi();
                                                // go back to login screen
                                                startActivity(new Intent(RegisterActivity.this, AdminLoginActivity.class));
                                                finish();
                                            })
                                            .addOnFailureListener(e -> {
                                                cancelNetworkTimeout();
                                                Log.w(TAG, "sendEmailVerification:onFailure", e);
                                                Toast.makeText(this, e.getMessage() != null ? e.getMessage() : "Failed to send verification email.", Toast.LENGTH_LONG).show();
                                                // sign out and reset UI
                                                mAuth.signOut();
                                                resetUi();
                                                startActivity(new Intent(RegisterActivity.this, AdminLoginActivity.class));
                                                finish();
                                            });
                                } else {
                                    // Unexpected: user null after creation
                                    Toast.makeText(this, "User created but not available. Please login.", Toast.LENGTH_LONG).show();
                                    resetUi();
                                    startActivity(new Intent(RegisterActivity.this, AdminLoginActivity.class));
                                    finish();
                                }
                            })
                            .addOnFailureListener(e -> {
                                cancelNetworkTimeout();
                                Log.w(TAG, "saveUser:onFailure", e);
                                if (mAuth.getCurrentUser() != null) mAuth.getCurrentUser().delete();
                                Toast.makeText(this, e.getMessage() != null ? e.getMessage() : "Saving user failed", Toast.LENGTH_LONG).show();
                                resetUi();
                            });
                    })
                    .addOnFailureListener(e -> {
                        cancelNetworkTimeout();
                        Log.w(TAG, "createUser:onFailure", e);
                        Toast.makeText(this, e.getMessage() != null ? e.getMessage() : "Registration failed.", Toast.LENGTH_LONG).show();
                        resetUi();
                    });
        } catch (Exception ex) {
            cancelNetworkTimeout();
            Log.e(TAG, "Exception while starting registration", ex);
            Toast.makeText(this, "Unexpected error: " + ex.getMessage(), Toast.LENGTH_LONG).show();
            resetUi();
        }
    }

    private String text(TextInputEditText et) {
        return et != null && et.getText() != null ? et.getText().toString().trim() : "";
    }

    private void resetUi() {
        registerBtn.setEnabled(true);
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        cancelNetworkTimeout();
    }

    private boolean isConnected() {
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return false;
            NetworkInfo ni = cm.getActiveNetworkInfo();
            return ni != null && ni.isConnected();
        } catch (Exception e) {
            Log.w(TAG, "isConnected:failed", e);
            return false;
        }
    }

    private void startNetworkTimeout() {
        cancelNetworkTimeout();
        timeoutRunnable = () -> {
            Log.w(TAG, "Network operation timed out after " + NETWORK_TIMEOUT_MS + "ms");
            Toast.makeText(RegisterActivity.this, "Network timeout. Please check your connection and try again.", Toast.LENGTH_LONG).show();
            resetUi();
        };
        timeoutHandler.postDelayed(timeoutRunnable, NETWORK_TIMEOUT_MS);
    }

    private void cancelNetworkTimeout() {
        if (timeoutRunnable != null) {
            timeoutHandler.removeCallbacks(timeoutRunnable);
            timeoutRunnable = null;
        }
    }
}