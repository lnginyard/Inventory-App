package com.example.data.sms

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SmsManager
import android.util.Log
import androidx.core.content.ContextCompat

class SmsAlertManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("sms_alert_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ALERT_PHONE = "alert_phone_number"
        private const val KEY_SMS_ENABLED = "sms_alerts_enabled"
        private const val DEFAULT_PHONE = "555-0199"
    }

    fun isSmsPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun getAlertPhoneNumber(): String {
        return prefs.getString(KEY_ALERT_PHONE, DEFAULT_PHONE) ?: DEFAULT_PHONE
    }

    fun setAlertPhoneNumber(phoneNumber: String) {
        prefs.edit().putString(KEY_ALERT_PHONE, phoneNumber).apply()
    }

    fun isSmsAlertEnabled(): Boolean {
        return prefs.getBoolean(KEY_SMS_ENABLED, true)
    }

    fun setSmsAlertEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SMS_ENABLED, enabled).apply()
    }

    /**
     * Sends an SMS notification when an item reaches zero quantity.
     * Guaranteed never to crash the app if permission is missing or sending fails.
     */
    fun sendZeroStockAlert(itemName: String, sku: String): SmsSendResult {
        if (!isSmsAlertEnabled()) {
            return SmsSendResult.Disabled("SMS alerts are disabled in settings.")
        }

        if (!isSmsPermissionGranted()) {
            Log.w("SmsAlertManager", "SEND_SMS permission not granted. Skipping SMS for item: $itemName")
            return SmsSendResult.PermissionDenied("SMS permission not granted. App continues operating normally.")
        }

        val phoneNumber = getAlertPhoneNumber().trim()
        if (phoneNumber.isEmpty()) {
            return SmsSendResult.Failed("Alert phone number is blank.")
        }

        val message = "ALERT: Warehouse item '$itemName' (SKU: $sku) has reached ZERO stock! Please restock immediately."

        return try {
            @Suppress("DEPRECATION")
            val smsManager: SmsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            Log.i("SmsAlertManager", "Zero stock SMS sent to $phoneNumber for item $itemName")
            SmsSendResult.Success("Zero-stock alert SMS sent successfully to $phoneNumber.")
        } catch (e: Exception) {
            Log.e("SmsAlertManager", "Failed to send SMS: ${e.message}", e)
            SmsSendResult.Failed("Could not send SMS: ${e.localizedMessage ?: "Telephony error"}")
        }
    }

    fun sendTestSms(): SmsSendResult {
        if (!isSmsPermissionGranted()) {
            return SmsSendResult.PermissionDenied("SEND_SMS permission is not granted.")
        }

        val phoneNumber = getAlertPhoneNumber().trim()
        if (phoneNumber.isEmpty()) {
            return SmsSendResult.Failed("Please specify a valid phone number.")
        }

        val message = "[TEST ALERT] Warehouse Inventory Tracker system test SMS notification."

        return try {
            @Suppress("DEPRECATION")
            val smsManager: SmsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            SmsSendResult.Success("Test SMS alert sent successfully to $phoneNumber.")
        } catch (e: Exception) {
            SmsSendResult.Failed("SMS test failed: ${e.localizedMessage ?: "Device error"}")
        }
    }
}

sealed class SmsSendResult {
    data class Success(val message: String) : SmsSendResult()
    data class PermissionDenied(val message: String) : SmsSendResult()
    data class Disabled(val message: String) : SmsSendResult()
    data class Failed(val message: String) : SmsSendResult()
}
