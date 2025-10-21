package com.example.finalproject.superadmin;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finalproject.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Map;

public class EditStudentActivity extends AppCompatActivity {

    private EditText edtFirstName, edtLastName, edtStudentId, edtSection;
    private Button btnSaveChanges, btnRemoveFromSection;
    private String studentUid;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_student);

        studentUid = getIntent().getStringExtra("STUDENT_UID");
        userRef = FirebaseDatabase.getInstance().getReference("users").child(studentUid);

        edtFirstName = findViewById(R.id.edtFirstName);
        edtLastName = findViewById(R.id.edtLastName);
        edtStudentId = findViewById(R.id.edtStudentId);
        edtSection = findViewById(R.id.edtSection);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        btnRemoveFromSection = findViewById(R.id.btnRemoveFromSection);

        loadStudentInfo();

        btnSaveChanges.setOnClickListener(v -> saveChanges());
        btnRemoveFromSection.setOnClickListener(v -> removeFromSection());
    }

    private void loadStudentInfo() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    edtFirstName.setText(snapshot.child("firstName").getValue(String.class));
                    edtLastName.setText(snapshot.child("lastName").getValue(String.class));
                    edtStudentId.setText(snapshot.child("studentId").getValue(String.class));
                    edtSection.setText(snapshot.child("section").getValue(String.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditStudentActivity.this, "Failed to load student info.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveChanges() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("firstName", edtFirstName.getText().toString().trim());
        updates.put("lastName", edtLastName.getText().toString().trim());
        updates.put("studentId", edtStudentId.getText().toString().trim());
        updates.put("section", edtSection.getText().toString().trim());

        userRef.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(EditStudentActivity.this, "Student info updated.", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(EditStudentActivity.this, "Failed to update info.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeFromSection() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("section", null);
        userRef.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(EditStudentActivity.this, "Student removed from section.", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(EditStudentActivity.this, "Failed to remove from section.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
