package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class AdminRegistrationCompleteActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_registration_complete);

        Button btn = findViewById(R.id.btnGoToLogin);
        btn.setOnClickListener(v -> {
            startActivity(new Intent(AdminRegistrationCompleteActivity.this, AdminLoginActivity.class));
            finish();
        });
    }
}
