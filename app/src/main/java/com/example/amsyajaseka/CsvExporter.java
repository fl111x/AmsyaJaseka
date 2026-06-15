package com.example.amsyajaseka;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.example.amsyajaseka.data.Order;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CsvExporter {

    public static boolean exportOrdersToCsv(Context context, List<Order> orders, long targetMonthTimestamp) {
        if (orders == null || orders.isEmpty()) {
            return false;
        }

        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM_yyyy", new Locale("id", "ID"));
        String monthString = monthFormat.format(new Date(targetMonthTimestamp));
        String fileName = "Laporan_Laundry_" + monthString + ".csv";

        StringBuilder csvContent = new StringBuilder();
        // Header
        csvContent.append("ID,Nama,Alamat,Berat (Kg),Harga Per Kg,Total Harga,Tanggal Masuk,Tanggal Selesai\n");

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", new Locale("id", "ID"));

        for (Order order : orders) {
            double total = order.weight * order.pricePerKg;
            csvContent.append(order.id).append(",");
            csvContent.append("\"").append(order.name.replace("\"", "\"\"")).append("\",");
            csvContent.append("\"").append(order.address.replace("\"", "\"\"")).append("\",");
            csvContent.append(order.weight).append(",");
            csvContent.append(order.pricePerKg).append(",");
            csvContent.append(total).append(",");
            csvContent.append(dateFormat.format(new Date(order.date))).append(",");
            csvContent.append(dateFormat.format(new Date(order.completionDate))).append("\n");
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "text/csv");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/LaporanLaundry");

                ContentResolver resolver = context.getContentResolver();
                Uri uri = resolver.insert(MediaStore.Files.getContentUri("external"), values);

                if (uri != null) {
                    try (OutputStream out = resolver.openOutputStream(uri)) {
                        out.write(csvContent.toString().getBytes());
                    }
                    return true;
                }
            } else {
                // For Android 9 and below
                File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "LaporanLaundry");
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File file = new File(dir, fileName);
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(csvContent.toString().getBytes());
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("CsvExporter", "Failed to export CSV", e);
        }

        return false;
    }
}
