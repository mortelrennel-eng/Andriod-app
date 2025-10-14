package com.example.finalproject;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class StudentRegisterActivity extends AppCompatActivity {

    EditText edtFirstName, edtLastName, edtStudentId, edtEmail, edtContactNumber, edtPassword, edtParentName, edtParentContactNumber;
    Button btnRegister;
    FirebaseAuth mAuth;
    FirebaseDatabase realtimeDb;
    DatabaseReference usersRef;
    FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_register);

        edtFirstName = findViewById(R.id.edtFirstName);
        edtLastName = findViewById(R.id.edtLastName);
        edtStudentId = findViewById(R.id.edtStudentId);
        edtEmail = findViewById(R.id.edtEmail);
        edtContactNumber = findViewById(R.id.edtContactNumber);
        edtPassword = findViewById(R.id.edtPassword);
        edtParentName = findViewById(R.id.edtParentName);
        edtParentContactNumber = findViewById(R.id.edtParentContactNumber);
        btnRegister = findViewById(R.id.btnRegister);

    mAuth = FirebaseAuth.getInstance();
    realtimeDb = FirebaseDatabase.getInstance("https://finalproject-b08f4-default-rtdb.firebaseio.com/");
    usersRef = realtimeDb.getReference("users");
        storage = FirebaseStorage.getInstance();

        btnRegister.setOnClickListener(v -> registerStudent());
    }

    private void registerStudent() {
        String firstName = edtFirstName.getText().toString().trim();
        String lastName = edtLastName.getText().toString().trim();
        String studentId = edtStudentId.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String contactNumber = edtContactNumber.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String parentName = edtParentName.getText().toString().trim();
        String parentContactNumber = edtParentContactNumber.getText().toString().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || studentId.isEmpty() || email.isEmpty() || contactNumber.isEmpty() || password.isEmpty() || parentName.isEmpty() || parentContactNumber.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    String uid = user.getUid();
                    // generate QR then save to RTDB
                    generateAndUploadQrCode(uid, firstName, lastName, studentId, email, contactNumber, parentName, parentContactNumber);
                }
            } else {
                Toast.makeText(this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void generateAndUploadQrCode(String uid, String firstName, String lastName, String studentId, String email, String contactNumber, String parentName, String parentContactNumber) {
        MultiFormatWriter writer = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = writer.encode(uid, BarcodeFormat.QR_CODE, 400, 400);
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.createBitmap(bitMatrix);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            StorageReference qrCodeRef = storage.getReference().child("qr_codes/" + uid + ".jpg");

            qrCodeRef.putBytes(data)
                    .addOnSuccessListener(taskSnapshot -> qrCodeRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String qrCodeUrl = uri.toString();
                        saveUserToFirestore(uid, firstName, lastName, studentId, email, contactNumber, parentName, parentContactNumber, qrCodeUrl);
                    }))
                    .addOnFailureListener(e -> Toast.makeText(StudentRegisterActivity.this, "QR Code Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        } catch (WriterException e) {
            Toast.makeText(this, "QR Code Generation Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveUserToFirestore(String uid, String firstName, String lastName, String studentId, String email, String contactNumber, String parentName, String parentContactNumber, String qrCodeUrl) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("firstName", firstName);
        userMap.put("lastName", lastName);
        userMap.put("studentId", studentId);
        userMap.put("email", email);
        userMap.put("contactNumber", contactNumber);
        userMap.put("parentName", parentName);
        userMap.put("parentContactNumber", parentContactNumber);
        userMap.put("role", "student");
        userMap.put("uid", uid);
        userMap.put("qrCodeUrl", qrCodeUrl);
        usersRef.child(uid).setValue(userMap)
                .addOnSuccessListener(aVoid -> {
                    // send verification email
                    FirebaseUser u = mAuth.getCurrentUser();
                    if (u != null) {
                        u.sendEmailVerification().addOnCompleteListener(v -> {
                            Toast.makeText(this, "Registration successful! Verify your email before logging in.", Toast.LENGTH_LONG).show();
                            mAuth.signOut();
                            startActivity(new Intent(StudentRegisterActivity.this, StudentLoginActivity.class));
                            finish();
                        }).addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to send verification: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            mAuth.signOut();
                            startActivity(new Intent(StudentRegisterActivity.this, StudentLoginActivity.class));
                            finish();
                        });
                    } else {
                        Toast.makeText(this, "Registration finished; please login.", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(StudentRegisterActivity.this, StudentLoginActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Database Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
