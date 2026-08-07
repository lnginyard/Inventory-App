package com.example.cs360inventoryapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.example.R;

public class LoginActivity extends Activity {

    private Button buttonLogin;
    private Button buttonCreateAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        buttonLogin = findViewById(R.id.buttonLogin);
        buttonCreateAccount = findViewById(R.id.buttonCreateAccount);

        if (buttonLogin != null) {
            buttonLogin.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, InventoryActivity.class);
                startActivity(intent);
            });
        }

        if (buttonCreateAccount != null) {
            buttonCreateAccount.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, InventoryActivity.class);
                startActivity(intent);
            });
        }
    }
}
