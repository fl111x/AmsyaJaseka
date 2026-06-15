package com.example.amsyajaseka.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface OrderDao {
    @Insert
    void insert(Order order);

    @androidx.room.Update
    void update(Order order);

    @androidx.room.Delete
    void delete(Order order);

    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    Order getOrderById(int orderId);

    // Order by Date (Newest first) - Active Orders Only
    @Query("SELECT * FROM orders WHERE isCompleted = 0 ORDER BY date DESC")
    LiveData<List<Order>> getAllOrdersByDateDesc();

    // Order by Name (Alphabetical) - Active Orders Only
    @Query("SELECT * FROM orders WHERE isCompleted = 0 ORDER BY name ASC")
    LiveData<List<Order>> getAllOrdersByNameAsc();

    // Search by Name - Active Orders Only
    @Query("SELECT * FROM orders WHERE isCompleted = 0 AND name LIKE '%' || :searchQuery || '%' ORDER BY date DESC")
    LiveData<List<Order>> searchOrdersByName(String searchQuery);

    // History (Completed Orders) - Newest completion date first
    @Query("SELECT * FROM orders WHERE isCompleted = 1 ORDER BY completionDate DESC")
    LiveData<List<Order>> getHistoryOrders();

    // Get old completed orders before a certain timestamp
    @Query("SELECT * FROM orders WHERE isCompleted = 1 AND completionDate < :timestamp")
    List<Order> getCompletedOrdersBefore(long timestamp);

    // Delete completed orders before a certain timestamp
    @Query("DELETE FROM orders WHERE isCompleted = 1 AND completionDate < :timestamp")
    void deleteCompletedOrdersBefore(long timestamp);

    // Get all orders (active and completed) for full backup
    @Query("SELECT * FROM orders ORDER BY date DESC")
    List<Order> getAllOrdersSync();
}
