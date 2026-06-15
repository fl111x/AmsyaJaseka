package com.example.amsyajaseka;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amsyajaseka.data.AppDatabase;
import com.example.amsyajaseka.data.Order;
import com.example.amsyajaseka.data.OrderDao;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private OrderAdapter adapter;
    private OrderDao orderDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderAdapter();
        recyclerView.setAdapter(adapter);

        orderDao = AppDatabase.getDatabase(this).orderDao();

        adapter.setOnItemClickListener(order -> {
            Intent intent = new Intent(MainActivity.this, OrderDetailActivity.class);
            intent.putExtra("ORDER_ID", order.id);
            startActivity(intent);
        });

        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddOrderActivity.class);
            startActivity(intent);
        });

        ImageButton btnHistory = findViewById(R.id.btnHistory);
        btnHistory.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
        });

        ImageButton btnReport = findViewById(R.id.btnReport);
        btnReport.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ReportActivity.class);
            startActivity(intent);
        });

        EditText searchEditText = findViewById(R.id.searchEditText);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchOrders(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        ImageButton btnExportAllCsv = findViewById(R.id.btnExportAllCsv);
        btnExportAllCsv.setOnClickListener(v -> {
            AppDatabase.databaseWriteExecutor.execute(() -> {
                List<Order> allOrders = orderDao.getAllOrdersSync();
                boolean success = CsvExporter.exportAllOrdersBackupToCsv(MainActivity.this, allOrders);
                runOnUiThread(() -> {
                    if (success) {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Backup Berhasil")
                                .setMessage("Semua pesanan (termasuk yang selesai) telah diekspor ke Documents/LaporanLaundry sebagai file backup CSV.")
                                .setPositiveButton("OK", null)
                                .show();
                    } else {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Backup Gagal")
                                .setMessage("Gagal mengekspor data atau tidak ada pesanan.")
                                .setPositiveButton("OK", null)
                                .show();
                    }
                });
            });
        });

        checkAndExportOldOrders();
        loadOrders();
    }

    private void checkAndExportOldOrders() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            long startOfMonthTimestamp = calendar.getTimeInMillis();

            List<Order> oldOrders = orderDao.getCompletedOrdersBefore(startOfMonthTimestamp);

            if (oldOrders != null && !oldOrders.isEmpty()) {
                long targetMonthTimestamp = oldOrders.get(0).completionDate;
                boolean success = CsvExporter.exportOrdersToCsv(MainActivity.this, oldOrders, targetMonthTimestamp);
                
                if (success) {
                    orderDao.deleteCompletedOrdersBefore(startOfMonthTimestamp);
                    
                    runOnUiThread(() -> {
                        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", new Locale("id", "ID"));
                        String monthStr = sdf.format(new Date(targetMonthTimestamp));
                        
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Pembersihan Riwayat")
                                .setMessage("Riwayat pesanan bulan " + monthStr + " telah diekspor ke folder Documents/LaporanLaundry dan dihapus dari aplikasi untuk menghemat memori.")
                                .setPositiveButton("OK", null)
                                .show();
                    });
                }
            }
        });
    }

    private void loadOrders() {
        orderDao.getAllOrdersByDateDesc().observe(this, orders -> {
            adapter.setOrders(orders);
        });
    }

    private void searchOrders(String query) {
        if (query.isEmpty()) {
            loadOrders();
        } else {
            orderDao.searchOrdersByName(query).observe(this, orders -> {
                adapter.setOrders(orders);
            });
        }
    }
}