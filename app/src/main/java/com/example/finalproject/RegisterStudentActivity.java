package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

public class RegisterStudentActivity extends AppCompatActivity {

    EditText nameField, emailField, phoneField, passwordField, parentEmailField;
    Button registerBtn;
    FirebaseAuth auth;
    FirebaseDatabase realtimeDb;
    DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_student);

        nameField = findViewById(R.id.studentName);
        emailField = findViewById(R.id.studentEmail);
        phoneField = findViewById(R.id.studentPhone);
        passwordField = findViewById(R.id.studentPassword);
        parentEmailField = findViewById(R.id.parentEmail);
        registerBtn = findViewById(R.id.registerBtn);

    auth = FirebaseAuth.getInstance();
    realtimeDb = FirebaseDatabase.getInstance("https://finalproject-b08f4-default-rtdb.firebaseio.com/");
    usersRef = realtimeDb.getReference("users");

        registerBtn.setOnClickListener(v -> registerStudent());
    }

    private void registerStudent() {
        String name = nameField.getText().toString().trim();
        String email = emailField.getText().toString().trim();
        String phone = phoneField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();
        String parentEmail = parentEmailField.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || parentEmail.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    String uid = result.getUser().getUid();

                    Map<String, Object> data = new HashMap<>();
                    data.put("name", name);
                    data.put("email", email);
                    data.put("phone", phone);
                    data.put("role", "student");
                    data.put("parentEmail", parentEmail);
                    data.put("rfid", "");  // Assigned after RFID tap

                    usersRef.child(uid).setValue(data)
                            .addOnSuccessListener(a -> {
                                if (result.getUser() != null) {
                                    result.getUser().sendEmailVerification();
                                }
                                Toast.makeText(this, "Registered! Please verify your email.", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(this, StudentLoginActivity.class));
                                finish();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
