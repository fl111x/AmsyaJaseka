package com.example.amsyajaseka;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import com.example.amsyajaseka.data.Order;

import java.io.OutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReceiptGenerator {

    public static void generateAndSaveReceipt(Context context, Order order) {
        // Receipt dimensions
        int width = 600;
        int height = 800;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE); // White background

        // Watermark
        Paint watermarkPaint = new Paint();
        watermarkPaint.setAntiAlias(true);
        watermarkPaint.setColor(Color.argb(40, 150, 150, 150)); // Light transparent gray
        watermarkPaint.setTextSize(80);
        watermarkPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        watermarkPaint.setTextAlign(Paint.Align.CENTER);
        
        canvas.save();
        canvas.translate(width / 2f, height / 2f);
        canvas.rotate(-45);
        canvas.drawText("AMSYA JASEKA", 0, 0, watermarkPaint);
        canvas.restore();

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);

        int yPos = 80;

        // Header
        paint.setTextSize(40);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("AMSYA JASEKA", width / 2, yPos, paint);

        yPos += 40;
        paint.setTextSize(20);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        canvas.drawText("Nota Pesanan", width / 2, yPos, paint);

        yPos += 40;
        // Draw dashed line
        drawDashedLine(canvas, paint, yPos, width);
        
        yPos += 60;
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(24);
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm", new Locale("id", "ID"));
        String dateStr = sdf.format(new Date());
        canvas.drawText("Tanggal : " + dateStr, 40, yPos, paint);
        
        yPos += 40;
        canvas.drawText("Nama    : " + order.name, 40, yPos, paint);
        
        yPos += 40;
        canvas.drawText("Alamat  : " + order.address, 40, yPos, paint);

        yPos += 60;
        drawDashedLine(canvas, paint, yPos, width);

        yPos += 60;
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("Rincian", 40, yPos, paint);
        
        yPos += 40;
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        
        String weightPrice = order.weight + " Kg x " + currencyFormat.format(order.pricePerKg);
        canvas.drawText("Berat x Harga", 40, yPos, paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(weightPrice, width - 40, yPos, paint);

        yPos += 60;
        paint.setTextAlign(Paint.Align.LEFT);
        drawDashedLine(canvas, paint, yPos, width);

        yPos += 60;
        paint.setTextSize(32);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("TOTAL", 40, yPos, paint);
        
        paint.setTextAlign(Paint.Align.RIGHT);
        double total = order.weight * order.pricePerKg;
        canvas.drawText(currencyFormat.format(total), width - 40, yPos, paint);

        yPos += 120;
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(20);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));
        canvas.drawText("Terima kasih telah menggunakan jasa kami!", width / 2, yPos, paint);

        saveImageToGallery(context, bitmap, order.name);
    }

    private static void drawDashedLine(Canvas canvas, Paint paint, int y, int width) {
        Paint dashPaint = new Paint(paint);
        dashPaint.setTextAlign(Paint.Align.LEFT);
        String dashes = "------------------------------------------------------------------";
        canvas.drawText(dashes, 40, y, dashPaint);
    }

    private static void saveImageToGallery(Context context, Bitmap bitmap, String customerName) {
        String fileName = "Nota_Setrika_" + customerName.replaceAll(" ", "_") + "_" + System.currentTimeMillis() + ".jpg";

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/SetrikaKiloan");
            values.put(MediaStore.Images.Media.IS_PENDING, 1);
        }

        ContentResolver resolver = context.getContentResolver();
        Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        try {
            if (uri != null) {
                OutputStream out = resolver.openOutputStream(uri);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                if (out != null) {
                    out.close();
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear();
                    values.put(MediaStore.Images.Media.IS_PENDING, 0);
                    resolver.update(uri, values, null, null);
                }

                Toast.makeText(context, "Nota berhasil disimpan ke Galeri", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(context, "Gagal menyimpan nota", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
