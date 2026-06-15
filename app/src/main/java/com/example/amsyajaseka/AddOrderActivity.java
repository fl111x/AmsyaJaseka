package com.example.amsyajaseka;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.amsyajaseka.data.AppDatabase;
import com.example.amsyajaseka.data.Order;
import com.example.amsyajaseka.data.OrderDao;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddOrderActivity extends AppCompatActivity {

    private EditText etName, etAddress, etWeight, etPrice, etDate;
    private Button btnSave;
    private long selectedDateTimestamp;
    private int editOrderId = -1;
    private Order currentOrder;
    private OrderDao orderDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_order);

        orderDao = AppDatabase.getDatabase(this).orderDao();

        etName = findViewById(R.id.etName);
        etAddress = findViewById(R.id.etAddress);
        etWeight = findViewById(R.id.etWeight);
        etPrice = findViewById(R.id.etPrice);
        etDate = findViewById(R.id.etDate);
        btnSave = findViewById(R.id.btnSave);

        // Set Default Date to Today
        Calendar calendar = Calendar.getInstance();
        selectedDateTimestamp = calendar.getTimeInMillis();
        updateDateLabel(calendar);

        editOrderId = getIntent().getIntExtra("EDIT_ORDER_ID", -1);
        if (editOrderId != -1) {
            TextView tvTitle = findViewById(R.id.tvTitle);
            tvTitle.setText("Edit Pesanan");
            btnSave.setText("Simpan Perubahan");
            loadOrderData();
        }

        etDate.setOnClickListener(v -> {
            new DatePickerDialog(AddOrderActivity.this, (view, year, month, dayOfMonth) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                selectedDateTimestamp = calendar.getTimeInMillis();
                updateDateLabel(calendar);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnSave.setOnClickListener(v -> saveOrder());
    }

    private void loadOrderData() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            currentOrder = orderDao.getOrderById(editOrderId);
            runOnUiThread(() -> {
                if (currentOrder != null) {
                    etName.setText(currentOrder.name);
                    etAddress.setText(currentOrder.address);
                    etWeight.setText(String.valueOf(currentOrder.weight));
                    etPrice.setText(String.valueOf((int)currentOrder.pricePerKg));
                    
                    selectedDateTimestamp = currentOrder.date;
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(selectedDateTimestamp);
                    updateDateLabel(cal);
                }
            });
        });
    }

    private void updateDateLabel(Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", new Locale("id", "ID"));
        etDate.setText(sdf.format(calendar.getTime()));
    }

    private void saveOrder() {
        String name = etName.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String weightStr = etWeight.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();

        if (name.isEmpty() || address.isEmpty() || weightStr.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Mohon lengkapi semua data", Toast.LENGTH_SHORT).show();
            return;
        }

        double weight = Double.parseDouble(weightStr);
        double price = Double.parseDouble(priceStr);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            if (editOrderId != -1 && currentOrder != null) {
                currentOrder.name = name;
                currentOrder.address = address;
                currentOrder.weight = weight;
                currentOrder.pricePerKg = price;
                currentOrder.date = selectedDateTimestamp;
                orderDao.update(currentOrder);
            } else {
                Order newOrder = new Order(name, address, weight, price, selectedDateTimestamp);
                orderDao.insert(newOrder);
                
                runOnUiThread(() -> {
                    ReceiptGenerator.generateAndSaveReceipt(AddOrderActivity.this, newOrder);
                });
            }
            
            runOnUiThread(() -> {
                Toast.makeText(AddOrderActivity.this, "Pesanan berhasil disimpan", Toast.LENGTH_SHORT).show();
                finish(); // Return
            });
        });
    }
}
