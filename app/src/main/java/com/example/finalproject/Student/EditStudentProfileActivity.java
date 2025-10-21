package com.example.finalproject.student;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.finalproject.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Map;

public class EditStudentProfileActivity extends AppCompatActivity {

    private EditText edtFirstName, edtLastName, edtStudentId, edtContactNumber, edtParentName, edtParentContactNumber;
    private RadioGroup rgGender;
    private RadioButton rbMale, rbFemale;
    private Button btnSaveChanges;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_student_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Edit Profile");

        edtFirstName = findViewById(R.id.edtFirstName);
        edtLastName = findViewById(R.id.edtLastName);
        edtStudentId = findViewById(R.id.edtStudentId);
        rgGender = findViewById(R.id.rgGender);
        rbMale = findViewById(R.id.rbMale);
        rbFemale = findViewById(R.id.rbFemale);
        edtContactNumber = findViewById(R.id.edtContactNumber);
        edtParentName = findViewById(R.id.edtParentName);
        edtParentContactNumber = findViewById(R.id.edtParentContactNumber);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
            loadUserProfile();
        }

        btnSaveChanges.setOnClickListener(v -> saveChanges());
    }

    private void loadUserProfile() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    edtFirstName.setText(snapshot.child("firstName").getValue(String.class));
                    edtLastName.setText(snapshot.child("lastName").getValue(String.class));
                    edtStudentId.setText(snapshot.child("studentId").getValue(String.class));
                    edtContactNumber.setText(snapshot.child("contactNumber").getValue(String.class));
                    edtParentName.setText(snapshot.child("parentName").getValue(String.class));
                    edtParentContactNumber.setText(snapshot.child("parentContactNumber").getValue(String.class));

                    String gender = snapshot.child("gender").getValue(String.class);
                    if ("Male".equalsIgnoreCase(gender)) {
                        rbMale.setChecked(true);
                    } else if ("Female".equalsIgnoreCase(gender)) {
                        rbFemale.setChecked(true);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void saveChanges() {
        int selectedGenderId = rgGender.getCheckedRadioButtonId();
        if (selectedGenderId == -1) {
            Toast.makeText(this, "Please select a gender", Toast.LENGTH_SHORT).show();
            return;
        }
        RadioButton selectedGender = findViewById(selectedGenderId);
        String gender = selectedGender.getText().toString();

        Map<String, Object> updates = new HashMap<>();
        updates.put("firstName", edtFirstName.getText().toString().trim());
        updates.put("lastName", edtLastName.getText().toString().trim());
        updates.put("studentId", edtStudentId.getText().toString().trim());
        updates.put("gender", gender);
        updates.put("contactNumber", edtContactNumber.getText().toString().trim());
        updates.put("parentName", edtParentName.getText().toString().trim());
        updates.put("parentContactNumber", edtParentContactNumber.getText().toString().trim());

        userRef.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to update profile.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { onBackPressed(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
