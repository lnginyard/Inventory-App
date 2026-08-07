package com.example.cs360inventoryapp;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.example.R;

public class SmsPermissionActivity extends Activity {

    private static final int SMS_PERMISSION_REQUEST_CODE = 100;

    private Button buttonRequestSmsPermission;
    private Button buttonBackToInventory;
    private TextView textViewSmsPermissionStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_permission);

        buttonRequestSmsPermission = findViewById(R.id.buttonRequestSmsPermission);
        buttonBackToInventory = findViewById(R.id.buttonBackToInventory);
        textViewSmsPermissionStatus = findViewById(R.id.textViewSmsPermissionStatus);

        updateSmsPermissionStatus();

        if (buttonRequestSmsPermission != null) {
            buttonRequestSmsPermission.setOnClickListener(v -> requestSmsPermission());
        }

        if (buttonBackToInventory != null) {
            buttonBackToInventory.setOnClickListener(v -> finish());
        }
    }

    private void updateSmsPermissionStatus() {
        if (textViewSmsPermissionStatus == null) return;

        if (checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            textViewSmsPermissionStatus.setText("SMS permission status: Granted. Zero-stock SMS alerts can be sent.");
        } else {
            textViewSmsPermissionStatus.setText("SMS permission status: Not granted. The app will continue to work without SMS alerts.");
        }
    }

    private void requestSmsPermission() {
        if (checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_REQUEST_CODE);
        } else {
            updateSmsPermissionStatus();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            updateSmsPermissionStatus();
        }
    }
}
