package com.example.finalproject;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class EbookReaderActivity extends AppCompatActivity {
    private static final String TAG = "EbookReader";

    private ImageView ivPage;
    private ImageButton btnPrev, btnNext;
    private ProgressBar progressBar;

    private PdfRenderer pdfRenderer;
    private PdfRenderer.Page currentPage;
    private ParcelFileDescriptor parcelFileDescriptor;
    private int pageIndex = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ebook_reader);

        ivPage = findViewById(R.id.ivPdfPage);
        btnPrev = findViewById(R.id.btnPrevPage);
        btnNext = findViewById(R.id.btnNextPage);
        progressBar = findViewById(R.id.progressPdf);

        String path = getIntent().getStringExtra("filePath");
        if (path == null) {
            Toast.makeText(this, "No file specified.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        openPdf(path);

        btnPrev.setOnClickListener(v -> showPage(pageIndex - 1));
        btnNext.setOnClickListener(v -> showPage(pageIndex + 1));
    }

    private void openPdf(String path) {
        try {
            File file = new File(path);
            parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
            pdfRenderer = new PdfRenderer(parcelFileDescriptor);
            showPage(0);
        } catch (IOException e) {
            Log.w(TAG, "openPdf:fail", e);
            Toast.makeText(this, "Failed to open PDF.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void showPage(int index) {
        if (pdfRenderer == null) return;
        if (index < 0 || index >= pdfRenderer.getPageCount()) return;
        if (currentPage != null) currentPage.close();
        currentPage = pdfRenderer.openPage(index);
        pageIndex = index;

        Bitmap bmp = Bitmap.createBitmap(currentPage.getWidth(), currentPage.getHeight(), Bitmap.Config.ARGB_8888);
        currentPage.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        ivPage.setImageBitmap(bmp);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (currentPage != null) currentPage.close();
            if (pdfRenderer != null) pdfRenderer.close();
            if (parcelFileDescriptor != null) parcelFileDescriptor.close();
        } catch (IOException e) {
            // ignore
        }
    }
}
