package com.example.cs360inventoryapp;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.R;

/**
 * AddItemActivity allows users to add new inventory items to the SQLite database.
 * CS 360: Project Three Mobile Application Development.
 */
public class AddItemActivity extends Activity {

    private EditText editTextItemName;
    private EditText editTextItemQuantity;
    private Button buttonSaveItem;
    private Button buttonCancelAddItem;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        // Initialize SQLite helper
        databaseHelper = new DatabaseHelper(this);

        // Bind layout views
        editTextItemName = findViewById(R.id.editTextItemName);
        editTextItemQuantity = findViewById(R.id.editTextItemQuantity);
        buttonSaveItem = findViewById(R.id.buttonSaveItem);
        buttonCancelAddItem = findViewById(R.id.buttonCancelAddItem);

        // Handle Save button click
        if (buttonSaveItem != null) {
            buttonSaveItem.setOnClickListener(v -> saveNewItem());
        }

        // Handle Cancel button click
        if (buttonCancelAddItem != null) {
            buttonCancelAddItem.setOnClickListener(v -> finish());
        }
    }

    /**
     * CREATE: Validates input fields and saves new inventory item to SQLite database.
     */
    private void saveNewItem() {
        String name = editTextItemName != null ? editTextItemName.getText().toString().trim() : "";
        String quantityStr = editTextItemQuantity != null ? editTextItemQuantity.getText().toString().trim() : "";

        // Input validation
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Please enter an item name", Toast.LENGTH_SHORT).show();
            if (editTextItemName != null) editTextItemName.requestFocus();
            return;
        }

        int quantity = 0;
        if (!TextUtils.isEmpty(quantityStr)) {
            try {
                quantity = Integer.parseInt(quantityStr);
                if (quantity < 0) {
                    Toast.makeText(this, "Quantity cannot be negative", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter a valid numeric quantity", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Insert new record into SQLite database
        long result = databaseHelper.addItem(name, quantity, "Warehouse Stock");

        if (result != -1) {
            Toast.makeText(this, "Item '" + name + "' added successfully!", Toast.LENGTH_SHORT).show();
            finish(); // Return to inventory screen
        } else {
            Toast.makeText(this, "Failed to save item. Please try again.", Toast.LENGTH_SHORT).show();
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
