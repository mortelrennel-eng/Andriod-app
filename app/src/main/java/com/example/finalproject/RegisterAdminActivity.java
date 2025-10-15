package com.example.finalproject;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

public class RegisterAdminActivity extends AppCompatActivity {

    private EditText edtFirstName, edtLastName, edtEmail, edtPassword, edtSection;
    private Button btnRegister;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_admin);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Register New Admin");

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance("https://finalproject-b08f4-default-rtdb.firebaseio.com/").getReference("users");

        edtFirstName = findViewById(R.id.adminFirstName);
        edtLastName = findViewById(R.id.adminLastName);
        edtEmail = findViewById(R.id.adminEmail);
        edtSection = findViewById(R.id.adminSection);
        edtPassword = findViewById(R.id.adminPassword);
        btnRegister = findViewById(R.id.registerBtn);
        progressBar = findViewById(R.id.progressBarAdminRegister);

        btnRegister.setOnClickListener(v -> registerAdmin());
    }

    private void registerAdmin() {
        String firstName = edtFirstName.getText().toString().trim();
        String lastName = edtLastName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String section = edtSection.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        setInProgress(true);

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    if (firebaseUser != null) {
                        firebaseUser.sendEmailVerification(); // Send verification email
                        saveAdminDataToDatabase(firebaseUser, firstName, lastName, section);
                    } else {
                        showError("Failed to get user session after creation.");
                    }
                } else {
                    showError("Registration Failed: " + task.getException().getMessage());
                }
            });
    }

    private void saveAdminDataToDatabase(FirebaseUser firebaseUser, String firstName, String lastName, String section) {
        String uid = firebaseUser.getUid();

        Map<String, Object> adminData = new HashMap<>();
        adminData.put("firstName", firstName);
        adminData.put("lastName", lastName);
        adminData.put("email", firebaseUser.getEmail());
        adminData.put("role", "admin");
        adminData.put("uid", uid);
        if (!section.isEmpty()) {
            adminData.put("section", section);
        }

        usersRef.child(uid).setValue(adminData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Admin registered! Verification email sent.", Toast.LENGTH_LONG).show();
                mAuth.signOut();
                finish();
            } else {
                showError("Failed to save admin data: " + task.getException().getMessage());
            }
        });
    }

    private void setInProgress(boolean inProgress) {
        progressBar.setVisibility(inProgress ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!inProgress);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        setInProgress(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
