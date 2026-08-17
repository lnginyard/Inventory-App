package com.example.cs360inventoryapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.R;

/**
 * LoginActivity handles user authentication and account creation for the inventory system.
 * Checks credentials against the local SQLite database via DatabaseHelper.
 * CS 360: Project Three Mobile Application Development.
 */
public class LoginActivity extends Activity {

    private EditText editTextUsername;
    private EditText editTextPassword;
    private Button buttonLogin;
    private Button buttonCreateAccount;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize SQLite Database Helper
        databaseHelper = new DatabaseHelper(this);

        // Bind UI components from XML layout
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonCreateAccount = findViewById(R.id.buttonCreateAccount);

        // Set up click listener for user login
        if (buttonLogin != null) {
            buttonLogin.setOnClickListener(v -> handleLogin());
        }

        // Set up click listener for creating a new user account
        if (buttonCreateAccount != null) {
            buttonCreateAccount.setOnClickListener(v -> handleCreateAccount());
        }
    }

    /**
     * Authenticates existing user credentials against the SQLite database.
     */
    private void handleLogin() {
        String username = editTextUsername != null ? editTextUsername.getText().toString().trim() : "";
        String password = editTextPassword != null ? editTextPassword.getText().toString().trim() : "";

        // Validate user inputs
        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, "Please enter your username", Toast.LENGTH_SHORT).show();
            if (editTextUsername != null) editTextUsername.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
            if (editTextPassword != null) editTextPassword.requestFocus();
            return;
        }

        // Check credentials against the SQLite database table
        boolean isValidUser = databaseHelper.checkUserCredentials(username, password);

        if (isValidUser) {
            Toast.makeText(this, "Login successful! Welcome " + username, Toast.LENGTH_SHORT).show();
            // Navigate to main Inventory Dashboard
            Intent intent = new Intent(LoginActivity.this, InventoryActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Invalid credentials. Please check or click 'Create Account'", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Registers a new user into the SQLite database users table.
     */
    private void handleCreateAccount() {
        String username = editTextUsername != null ? editTextUsername.getText().toString().trim() : "";
        String password = editTextPassword != null ? editTextPassword.getText().toString().trim() : "";

        // Validate user inputs
        if (TextUtils.isEmpty(username) || username.length() < 3) {
            Toast.makeText(this, "Username must be at least 3 characters", Toast.LENGTH_SHORT).show();
            if (editTextUsername != null) editTextUsername.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password) || password.length() < 4) {
            Toast.makeText(this, "Password must be at least 4 characters", Toast.LENGTH_SHORT).show();
            if (editTextPassword != null) editTextPassword.requestFocus();
            return;
        }

        // Check if the username is already taken in the database
        if (databaseHelper.checkUsernameExists(username)) {
            Toast.makeText(this, "Username already exists. Please log in or choose another", Toast.LENGTH_LONG).show();
            return;
        }

        // Save new user credentials to the SQLite database
        boolean registered = databaseHelper.registerUser(username, password);

        if (registered) {
            Toast.makeText(this, "Account successfully created for " + username + "! Logging in...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, InventoryActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Failed to create account. Please try again.", Toast.LENGTH_SHORT).show();
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
