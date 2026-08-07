package com.example.cs360inventoryapp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;

import com.example.R;

public class AddItemActivity extends Activity {

    private Button buttonSaveItem;
    private Button buttonCancelAddItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        buttonSaveItem = findViewById(R.id.buttonSaveItem);
        buttonCancelAddItem = findViewById(R.id.buttonCancelAddItem);

        if (buttonSaveItem != null) {
            buttonSaveItem.setOnClickListener(v -> finish());
        }
        if (buttonCancelAddItem != null) {
            buttonCancelAddItem.setOnClickListener(v -> finish());
        }
    }
}
