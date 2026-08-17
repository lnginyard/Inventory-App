package com.example.cs360inventoryapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.R;

import java.util.List;

/**
 * InventoryActivity displays persistent inventory items in a grid/table format.
 * Supports CRUD operations (Read, Update quantity, Delete) and checks SMS permissions
 * to dispatch automated alerts whenever stock drops to zero.
 * CS 360: Project Three Mobile Application Development.
 */
public class InventoryActivity extends Activity implements InventoryAdapter.OnItemActionListener {

    private ListView listViewInventory;
    private InventoryAdapter inventoryAdapter;
    private DatabaseHelper databaseHelper;
    private Button buttonAddItem;
    private Button buttonSmsSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        // Initialize Database Helper
        databaseHelper = new DatabaseHelper(this);

        // Bind layout views
        listViewInventory = findViewById(R.id.listViewInventory);
        buttonAddItem = findViewById(R.id.buttonAddItem);
        buttonSmsSettings = findViewById(R.id.buttonSmsSettings);

        // Setup ListView adapter
        inventoryAdapter = new InventoryAdapter(this, this);
        if (listViewInventory != null) {
            listViewInventory.setAdapter(inventoryAdapter);
        }

        // Setup Navigation Actions
        if (buttonAddItem != null) {
            buttonAddItem.setOnClickListener(v -> {
                Intent intent = new Intent(InventoryActivity.this, AddItemActivity.class);
                startActivity(intent);
            });
        }

        if (buttonSmsSettings != null) {
            buttonSmsSettings.setOnClickListener(v -> {
                Intent intent = new Intent(InventoryActivity.this, SmsPermissionActivity.class);
                startActivity(intent);
            });
        }

        // Load items initially
        loadInventoryItems();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh inventory grid when returning from AddItemActivity or Settings
        loadInventoryItems();
    }

    /**
     * READ: Query all inventory records from SQLite database and update UI adapter.
     */
    private void loadInventoryItems() {
        if (databaseHelper != null && inventoryAdapter != null) {
            List<InventoryItem> items = databaseHelper.getAllItems();
            inventoryAdapter.setItems(items);
        }
    }

    /**
     * UPDATE: Modifies item quantity in SQLite and triggers SMS alert if stock reaches zero.
     */
    @Override
    public void onQuantityChanged(InventoryItem item, int newQuantity) {
        if (databaseHelper == null || item == null) return;

        boolean updated = databaseHelper.updateItemQuantity(item.getId(), newQuantity);
        if (updated) {
            item.setQuantity(newQuantity);
            loadInventoryItems();

            // Zero-stock notification trigger
            if (newQuantity == 0) {
                handleZeroStockNotification(item.getName());
            }
        }
    }

    /**
     * DELETE: Prompts user confirmation and deletes item from SQLite database.
     */
    @Override
    public void onItemDeleted(InventoryItem item) {
        if (databaseHelper == null || item == null) return;

        new AlertDialog.Builder(this)
                .setTitle("Delete Item")
                .setMessage("Are you sure you want to delete '" + item.getName() + "' from inventory?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    boolean deleted = databaseHelper.deleteItem(item.getId());
                    if (deleted) {
                        Toast.makeText(this, item.getName() + " deleted", Toast.LENGTH_SHORT).show();
                        loadInventoryItems();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Checks SMS permissions and dispatches zero-stock alert if granted by the user.
     * If permission is not granted, the app continues to operate normally without crashing.
     */
    private void handleZeroStockNotification(String itemName) {
        if (checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            SharedPreferences prefs = getSharedPreferences("InventoryAppPrefs", Context.MODE_PRIVATE);
            String targetPhone = prefs.getString("alert_phone_number", "5551234567");

            try {
                SmsManager smsManager = getSystemService(SmsManager.class);
                if (smsManager != null) {
                    String message = "CRITICAL INVENTORY ALERT: '" + itemName + "' is now at ZERO stock!";
                    smsManager.sendTextMessage(targetPhone, null, message, null, null);
                    Toast.makeText(this, "SMS Alert sent to " + targetPhone + " for " + itemName + " (0 stock)", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Item reached 0. (SMS simulated for emulator)", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Permission denied: application continues smoothly
            Toast.makeText(this, "Warning: " + itemName + " reached 0 stock! (SMS permission disabled)", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        if (databaseHelper != null) {
            databaseHelper.close();
        }
        super.onDestroy();
    }
}
