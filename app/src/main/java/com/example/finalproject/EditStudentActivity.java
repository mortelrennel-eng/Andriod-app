package com.example.finalproject;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

public class EditStudentActivity extends AppCompatActivity {

    private EditText edtFirstName, edtLastName, edtStudentId, edtContactNumber, edtParentName, edtParentContactNumber;
    private Button btnSaveChanges;
    private String studentUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_student);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Edit Student");

        edtFirstName = findViewById(R.id.edtFirstName);
        edtLastName = findViewById(R.id.edtLastName);
        edtStudentId = findViewById(R.id.edtStudentId);
        edtContactNumber = findViewById(R.id.edtContactNumber);
        edtParentName = findViewById(R.id.edtParentName);
        edtParentContactNumber = findViewById(R.id.edtParentContactNumber);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);

        // Get data passed from the previous activity
        User student = (User) getIntent().getSerializableExtra("STUDENT_DATA");
        studentUid = student.getUid();

        // Populate fields
        edtFirstName.setText(student.getFirstName());
        edtLastName.setText(student.getLastName());
        edtStudentId.setText(student.getStudentId());
        edtContactNumber.setText(student.getContactNumber());
        edtParentName.setText(student.getParentName());
        edtParentContactNumber.setText(student.getParentContactNumber());

        btnSaveChanges.setOnClickListener(v -> saveChanges());
    }

    private void saveChanges() {
        String firstName = edtFirstName.getText().toString().trim();
        String lastName = edtLastName.getText().toString().trim();
        String studentId = edtStudentId.getText().toString().trim();
        String contactNumber = edtContactNumber.getText().toString().trim();
        String parentName = edtParentName.getText().toString().trim();
        String parentContact = edtParentContactNumber.getText().toString().trim();

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(studentUid);

        Map<String, Object> updates = new HashMap<>();
        updates.put("firstName", firstName);
        updates.put("lastName", lastName);
        updates.put("studentId", studentId);
        updates.put("contactNumber", contactNumber);
        updates.put("parentName", parentName);
        updates.put("parentContactNumber", parentContact);

        userRef.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Student details updated successfully.", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to update details.", Toast.LENGTH_SHORT).show();
            }
        });
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
