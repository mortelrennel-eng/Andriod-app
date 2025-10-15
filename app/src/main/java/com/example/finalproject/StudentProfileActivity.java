package com.example.finalproject;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;
import android.widget.Button;

import java.util.Map;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StudentProfileActivity extends AppCompatActivity {
    private EditText edtFirstName;
    private EditText edtLastName;
    private EditText edtEmail;
    private EditText edtStudentId;
    private EditText edtContact;
    private EditText edtSection;
    private Button btnSave;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_profile);

        edtFirstName = findViewById(R.id.edtFirstName);
        edtLastName = findViewById(R.id.edtLastName);
        edtEmail = findViewById(R.id.edtEmail);
        edtStudentId = findViewById(R.id.edtStudentId);
        edtContact = findViewById(R.id.edtContact);
        edtSection = findViewById(R.id.edtSection);
        btnSave = findViewById(R.id.btnSaveStudent);

        uid = getIntent().getStringExtra("uid");
        if (uid == null) {
            android.widget.Toast.makeText(this, "No student selected", android.widget.Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadProfile();

        btnSave.setOnClickListener(v -> {
            // validate
            String fn = edtFirstName.getText().toString().trim();
            String ln = edtLastName.getText().toString().trim();
            if (fn.isEmpty() || ln.isEmpty()) {
                android.widget.Toast.makeText(StudentProfileActivity.this, "First and last name are required.", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            new androidx.appcompat.app.AlertDialog.Builder(StudentProfileActivity.this)
                .setTitle("Save changes?")
                .setMessage("Do you want to save the changes to this student profile?")
                .setPositiveButton("Save", (dialog, which) -> saveProfile())
                .setNegativeButton("Cancel", null)
                .show();
        });
    }

    private void loadProfile() {
        DatabaseReference ref = FirebaseDatabase.getInstance("https://finalproject-b08f4-default-rtdb.firebaseio.com/").getReference("users").child(uid);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                edtFirstName.setText(snapshot.child("firstName").getValue(String.class));
                edtLastName.setText(snapshot.child("lastName").getValue(String.class));
                edtEmail.setText(snapshot.child("email").getValue(String.class));
                edtStudentId.setText(snapshot.child("studentId").getValue(String.class));
                edtContact.setText(snapshot.child("contact").getValue(String.class));
                edtSection.setText(snapshot.child("section").getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                android.widget.Toast.makeText(StudentProfileActivity.this, "Failed to load profile.", android.widget.Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void saveProfile() {
        String fn = edtFirstName.getText().toString().trim();
        String ln = edtLastName.getText().toString().trim();
        String sid = edtStudentId.getText().toString().trim();
        String contact = edtContact.getText().toString().trim();
        String section = edtSection.getText().toString().trim();

        Map<String, Object> updates = new HashMap<>();
        updates.put("firstName", fn);
        updates.put("lastName", ln);
        updates.put("studentId", sid);
        updates.put("contact", contact);
        updates.put("section", section);

        DatabaseReference ref = FirebaseDatabase.getInstance("https://finalproject-b08f4-default-rtdb.firebaseio.com/").getReference("users").child(uid);
        ref.updateChildren(updates).addOnSuccessListener(aVoid -> {
            android.widget.Toast.makeText(StudentProfileActivity.this, "Profile saved.", android.widget.Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        }).addOnFailureListener(e -> android.widget.Toast.makeText(StudentProfileActivity.this, "Failed to save: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show());
    }
}
