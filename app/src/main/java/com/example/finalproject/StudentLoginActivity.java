package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StudentLoginActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private Button loginBtn, registerBtn;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_login);

    mAuth = FirebaseAuth.getInstance();
    FirebaseDatabase db = FirebaseDatabase.getInstance("https://finalproject-b08f4-default-rtdb.firebaseio.com/");
    usersRef = db.getReference("users");

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginBtn = findViewById(R.id.loginBtn);
        registerBtn = findViewById(R.id.registerBtn);

        loginBtn.setOnClickListener(v -> loginStudent());

        registerBtn.setOnClickListener(v -> {
            startActivity(new Intent(StudentLoginActivity.this, StudentRegisterActivity.class));
        });
    }

    private void loginStudent() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (!task.isSuccessful() || task.getResult() == null || task.getResult().getUser() == null) {
                Toast.makeText(this, "Login Failed: " + (task.getException() != null ? task.getException().getMessage() : ""), Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null && !user.isEmailVerified()) {
                Toast.makeText(this, "Please verify your email before logging in.", Toast.LENGTH_LONG).show();
                mAuth.signOut();
                return;
            }

            String uid = task.getResult().getUser().getUid();
            usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot == null || !snapshot.exists()) {
                        Toast.makeText(StudentLoginActivity.this, "User data not found.", Toast.LENGTH_LONG).show();
                        mAuth.signOut();
                        return;
                    }
                    String role = snapshot.child("role").getValue(String.class);
                    if (!"student".equals(role)) {
                        Toast.makeText(StudentLoginActivity.this, "This account is not a student account.", Toast.LENGTH_LONG).show();
                        mAuth.signOut();
                        return;
                    }
                    startActivity(new Intent(StudentLoginActivity.this, StudentDashboard.class));
                    finish();
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(StudentLoginActivity.this, "Database error: " + (error.getMessage() != null ? error.getMessage() : ""), Toast.LENGTH_LONG).show();
                    mAuth.signOut();
                }
            });
        });
    }
}
