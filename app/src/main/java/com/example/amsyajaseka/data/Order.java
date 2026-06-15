package com.example.amsyajaseka.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "orders")
public class Order {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public String address;
    public double weight;
    public double pricePerKg;
    public long date; // Stored as Unix timestamp for easier sorting

    public boolean isCompleted = false;
    public long completionDate = 0;

    // Constructor, Getters, and Setters
    public Order(String name, String address, double weight, double pricePerKg, long date) {
        this.name = name;
        this.address = address;
        this.weight = weight;
        this.pricePerKg = pricePerKg;
        this.date = date;
    }
}
