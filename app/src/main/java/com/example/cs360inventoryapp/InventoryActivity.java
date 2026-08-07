package com.example.cs360inventoryapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.example.R;

public class InventoryActivity extends Activity {

    private Button buttonAddItem;
    private Button buttonSmsSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        buttonAddItem = findViewById(R.id.buttonAddItem);
        buttonSmsSettings = findViewById(R.id.buttonSmsSettings);

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
    }
}
