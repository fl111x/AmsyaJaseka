package com.example.amsyajaseka;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.example.amsyajaseka.data.AppDatabase;
import com.example.amsyajaseka.data.Order;
import com.example.amsyajaseka.data.OrderDao;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class OrderDetailActivity extends AppCompatActivity {

    private OrderDao orderDao;
    private Order currentOrder;
    private int orderId;
    private boolean isHistoryMode = false;

    private TextView tvName, tvAddress, tvDate, tvWeightPrice, tvTotal;
    private Button btnComplete, btnEdit, btnDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        Toolbar toolbar = findViewById(R.id.toolbarDetail);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        orderDao = AppDatabase.getDatabase(this).orderDao();
        orderId = getIntent().getIntExtra("ORDER_ID", -1);
        isHistoryMode = getIntent().getBooleanExtra("IS_HISTORY_MODE", false);

        tvName = findViewById(R.id.tvDetailName);
        tvAddress = findViewById(R.id.tvDetailAddress);
        tvDate = findViewById(R.id.tvDetailDate);
        tvWeightPrice = findViewById(R.id.tvDetailWeightPrice);
        tvTotal = findViewById(R.id.tvDetailTotal);

        btnComplete = findViewById(R.id.btnComplete);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);

        if (isHistoryMode) {
            btnComplete.setVisibility(View.GONE);
            btnEdit.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Detail Riwayat");
            }
        }

        btnComplete.setOnClickListener(v -> completeOrder());
        btnEdit.setOnClickListener(v -> editOrder());
        btnDelete.setOnClickListener(v -> confirmDelete());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrderDetails();
    }

    private void loadOrderDetails() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            currentOrder = orderDao.getOrderById(orderId);
            runOnUiThread(() -> {
                if (currentOrder != null) {
                    populateUI();
                } else {
                    Toast.makeText(this, "Pesanan tidak ditemukan", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        });
    }

    private void populateUI() {
        tvName.setText(currentOrder.name);
        tvAddress.setText(currentOrder.address);
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", new Locale("id", "ID"));
        tvDate.setText(sdf.format(new Date(currentOrder.date)));

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        tvWeightPrice.setText(currentOrder.weight + " Kg x " + currencyFormat.format(currentOrder.pricePerKg));

        double total = currentOrder.weight * currentOrder.pricePerKg;
        tvTotal.setText(currencyFormat.format(total));
    }

    private void completeOrder() {
        if (currentOrder != null) {
            currentOrder.isCompleted = true;
            currentOrder.completionDate = System.currentTimeMillis();
            AppDatabase.databaseWriteExecutor.execute(() -> {
                orderDao.update(currentOrder);
                runOnUiThread(() -> {
                    Toast.makeText(OrderDetailActivity.this, "Pesanan diselesaikan!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
        }
    }

    private void editOrder() {
        Intent intent = new Intent(OrderDetailActivity.this, AddOrderActivity.class);
        intent.putExtra("EDIT_ORDER_ID", currentOrder.id);
        startActivity(intent);
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Pesanan")
                .setMessage("Apakah Anda yakin ingin menghapus pesanan ini secara permanen?")
                .setPositiveButton("Hapus", (dialog, which) -> {
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        orderDao.delete(currentOrder);
                        runOnUiThread(() -> {
                            Toast.makeText(OrderDetailActivity.this, "Pesanan dihapus", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    });
                })
                .setNegativeButton("Batal", null)
                .show();
    }
}
