package com.example.finalproject;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;

public class QRGeneratorActivity extends AppCompatActivity {

    private EditText studentNameInput, studentEmailInput;
    private Button generateQrBtn;
    private ImageView qrImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_generator);

        studentNameInput = findViewById(R.id.studentNameInput);
        studentEmailInput = findViewById(R.id.studentEmailInput);
        generateQrBtn = findViewById(R.id.generateQrBtn);
        qrImage = findViewById(R.id.qrImage);

        generateQrBtn.setOnClickListener(v -> {
            String name = studentNameInput.getText().toString().trim();
            String email = studentEmailInput.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Please enter both name and email", Toast.LENGTH_SHORT).show();
                return;
            }

            // Combine the data into a single string, separated by a comma
            String qrData = name + "," + email;
            generateQRCode(qrData);
            qrImage.setVisibility(View.VISIBLE);
        });
    }

    private void generateQRCode(String data) {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            int size = 512;
            com.google.zxing.common.BitMatrix bitMatrix =
                    writer.encode(data, BarcodeFormat.QR_CODE, size, size);

            Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);

            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }

            qrImage.setImageBitmap(bmp);
        } catch (WriterException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to generate QR Code", Toast.LENGTH_SHORT).show();
        }
    }
}
