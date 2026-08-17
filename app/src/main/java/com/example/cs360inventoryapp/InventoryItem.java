package com.example.cs360inventoryapp;

/**
 * Model class representing an inventory item in the SQLite database.
 * CS 360: Mobile Architecture and Programming.
 */
public class InventoryItem {
    private long id;
    private String name;
    private int quantity;
    private String category;

    public InventoryItem() {
    }

    public InventoryItem(long id, String name, int quantity, String category) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.category = category;
    }

    public InventoryItem(String name, int quantity, String category) {
        this.name = name;
        this.quantity = quantity;
        this.category = category;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = Math.max(0, quantity);
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
