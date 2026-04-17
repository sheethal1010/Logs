package com.hearthborn.studios.logs;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Environment;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PdfGenerator {

    private static final int PAGE_WIDTH = 595; // A4 width in points
    private static final int PAGE_HEIGHT = 842; // A4 height in points
    private static final int MARGIN = 50;
    private static final int CONTENT_WIDTH = PAGE_WIDTH - (2 * MARGIN);

    public static void generateAndSharePdf(Context context, String title, String content) {
        if (title.trim().isEmpty() && content.trim().isEmpty()) {
            Toast.makeText(context, "com.hearthborn.studios.logs.Note is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File pdfFile = createPdf(context, title, content);
            if (pdfFile != null) {
                sharePdf(context, pdfFile);
            } else {
                Toast.makeText(context, "Failed to create PDF", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error creating PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private static File createPdf(Context context, String title, String content) {
        PdfDocument pdfDocument = new PdfDocument();

        // Create page
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        int yPosition = MARGIN;

        // Draw title
        if (!title.trim().isEmpty()) {
            TextPaint titlePaint = new TextPaint();
            titlePaint.setTextSize(24);
            titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            titlePaint.setColor(0xFF000000);
            titlePaint.setAntiAlias(true);

            StaticLayout titleLayout = new StaticLayout(
                    title,
                    titlePaint,
                    CONTENT_WIDTH,
                    Layout.Alignment.ALIGN_NORMAL,
                    1.0f,
                    0.0f,
                    false
            );

            canvas.save();
            canvas.translate(MARGIN, yPosition);
            titleLayout.draw(canvas);
            canvas.restore();

            yPosition += titleLayout.getHeight() + 30;
        }

        // Draw content
        if (!content.trim().isEmpty()) {
            TextPaint contentPaint = new TextPaint();
            contentPaint.setTextSize(12);
            contentPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
            contentPaint.setColor(0xFF000000);
            contentPaint.setAntiAlias(true);

            StaticLayout contentLayout = new StaticLayout(
                    content,
                    contentPaint,
                    CONTENT_WIDTH,
                    Layout.Alignment.ALIGN_NORMAL,
                    1.2f,
                    4.0f,
                    false
            );

            canvas.save();
            canvas.translate(MARGIN, yPosition);
            contentLayout.draw(canvas);
            canvas.restore();
        }

        pdfDocument.finishPage(page);

        // Save PDF to file
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "Note_" + timestamp + ".pdf";

        File pdfFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName);

        try {
            pdfDocument.writeTo(new FileOutputStream(pdfFile));
            pdfDocument.close();
            return pdfFile;
        } catch (IOException e) {
            e.printStackTrace();
            pdfDocument.close();
            return null;
        }
    }

    private static void sharePdf(Context context, File pdfFile) {
        Uri pdfUri = FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".provider",
                pdfFile
        );

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/pdf");
        shareIntent.putExtra(Intent.EXTRA_STREAM, pdfUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        context.startActivity(Intent.createChooser(shareIntent, "Share com.hearthborn.studios.logs.Note as PDF"));
    }
}