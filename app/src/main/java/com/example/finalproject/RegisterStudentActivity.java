package com.example.finalproject;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

public class RegisterStudentActivity extends AppCompatActivity {

    private EditText edtFirstName, edtLastName, edtStudentId, edtEmail, edtContactNumber, edtParentName, edtParentContactNumber, edtPassword;
    private RadioGroup rgGender;
    private Button btnRegister;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_student);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance("https://finalproject-b08f4-default-rtdb.firebaseio.com/").getReference("users");

        // Using the correct IDs from the updated layout
        edtFirstName = findViewById(R.id.edtFirstName);
        edtLastName = findViewById(R.id.edtLastName);
        edtStudentId = findViewById(R.id.edtStudentId);
        rgGender = findViewById(R.id.rgGender);
        edtEmail = findViewById(R.id.edtEmail);
        edtContactNumber = findViewById(R.id.edtContactNumber);
        edtParentName = findViewById(R.id.edtParentName);
        edtParentContactNumber = findViewById(R.id.edtParentContactNumber);
        edtPassword = findViewById(R.id.edtPassword);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);

        btnRegister.setOnClickListener(v -> registerStudent());
    }

    private void registerStudent() {
        String firstName = edtFirstName.getText().toString().trim();
        String lastName = edtLastName.getText().toString().trim();
        String studentId = edtStudentId.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String contact = edtContactNumber.getText().toString().trim();
        String parentName = edtParentName.getText().toString().trim();
        String parentContact = edtParentContactNumber.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        int selectedGenderId = rgGender.getCheckedRadioButtonId();
        if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) || TextUtils.isEmpty(studentId) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || selectedGenderId == -1) {
            Toast.makeText(this, "All fields including gender are required", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedGender = findViewById(selectedGenderId);
        String gender = selectedGender.getText().toString();

        progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                if (firebaseUser != null) {
                    String uid = firebaseUser.getUid();
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("firstName", firstName);
                    userData.put("lastName", lastName);
                    userData.put("studentId", studentId);
                    userData.put("gender", gender); // Save gender
                    userData.put("email", email);
                    userData.put("contactNumber", contact);
                    userData.put("parentName", parentName);
                    userData.put("parentContactNumber", parentContact);
                    userData.put("role", "student");
                    userData.put("uid", uid);

                    usersRef.child(uid).setValue(userData).addOnCompleteListener(dbTask -> {
                        progressBar.setVisibility(View.GONE);
                        if (dbTask.isSuccessful()) {
                            firebaseUser.sendEmailVerification();
                            Toast.makeText(this, "Registration successful. Please verify your email.", Toast.LENGTH_LONG).show();
                            mAuth.signOut();
                            finish();
                        } else {
                            Toast.makeText(this, "Database error: " + dbTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
