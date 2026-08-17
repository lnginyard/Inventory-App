package com.example.cs360inventoryapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.R;

/**
 * SmsPermissionActivity manages runtime permissions for sending SMS inventory alerts.
 * Implements Android dynamic permissions per the CS 360 rubric requirements:
 * - If granted, SMS messages are sent when stock drops to 0.
 * - If denied, the app gracefully continues functioning without SMS features.
 */
public class SmsPermissionActivity extends Activity {

    private static final int SMS_PERMISSION_REQUEST_CODE = 101;
    private static final String PREFS_NAME = "InventoryAppPrefs";
    private static final String PREF_PHONE_KEY = "alert_phone_number";

    private Button buttonRequestSmsPermission;
    private Button buttonBackToInventory;
    private TextView textViewSmsPermissionStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_permission);

        // Bind layout controls
        buttonRequestSmsPermission = findViewById(R.id.buttonRequestSmsPermission);
        buttonBackToInventory = findViewById(R.id.buttonBackToInventory);
        textViewSmsPermissionStatus = findViewById(R.id.textViewSmsPermissionStatus);

        // Initial permission status update
        updateSmsPermissionStatus();

        if (buttonRequestSmsPermission != null) {
            buttonRequestSmsPermission.setOnClickListener(v -> requestSmsPermission());
        }

        if (buttonBackToInventory != null) {
            buttonBackToInventory.setOnClickListener(v -> finish());
        }
    }

    /**
     * Updates the status TextView reflecting whether SEND_SMS permission is currently granted.
     */
    private void updateSmsPermissionStatus() {
        if (textViewSmsPermissionStatus == null) return;

        if (checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            textViewSmsPermissionStatus.setText("SMS permission status: GRANTED.\n\nAutomated zero-stock text alerts are enabled. Whenever an inventory item drops to 0 quantity, an SMS notification will be sent.");
            if (buttonRequestSmsPermission != null) {
                buttonRequestSmsPermission.setText("Send Test SMS Alert");
            }
        } else {
            textViewSmsPermissionStatus.setText("SMS permission status: NOT GRANTED / DENIED.\n\nThe application will continue to work normally for all inventory tracking without sending text messages.");
            if (buttonRequestSmsPermission != null) {
                buttonRequestSmsPermission.setText(R.string.request_sms_permission);
            }
        }
    }

    /**
     * Prompts the user with Android runtime permission dialog for SMS.
     */
    private void requestSmsPermission() {
        if (checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_REQUEST_CODE);
        } else {
            // Already granted: trigger a test SMS notification
            sendTestSms();
        }
    }

    /**
     * Sends a test SMS notification to verify message dispatch functionality.
     */
    private void sendTestSms() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String phoneNumber = prefs.getString(PREF_PHONE_KEY, "5551234567");

        try {
            SmsManager smsManager = getSystemService(SmsManager.class);
            if (smsManager != null) {
                String testMessage = "Warehouse Inventory Tracker: SMS notification system is active and verified!";
                smsManager.sendTextMessage(phoneNumber, null, testMessage, null, null);
                Toast.makeText(this, "Test SMS sent to " + phoneNumber + "!", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "SMS simulated for emulator: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS Permission GRANTED. Zero-stock alerts enabled.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "SMS Permission DENIED. App will run without SMS.", Toast.LENGTH_LONG).show();
            }
            updateSmsPermissionStatus();
        }
    }
}
