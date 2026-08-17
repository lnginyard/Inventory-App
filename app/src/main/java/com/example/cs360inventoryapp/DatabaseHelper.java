package com.example.cs360inventoryapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite Database Helper for the CS 360 Inventory Management Application.
 * Handles database creation, user credential verification, and full CRUD operations
 * for persistent inventory tracking.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "inventory_tracker.db";
    private static final int DATABASE_VERSION = 1;

    // Table: Users
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "id";
    public static final String COLUMN_USER_NAME = "username";
    public static final String COLUMN_USER_PASSWORD = "password";

    // Table: Inventory Items
    public static final String TABLE_INVENTORY = "inventory";
    public static final String COLUMN_ITEM_ID = "id";
    public static final String COLUMN_ITEM_NAME = "name";
    public static final String COLUMN_ITEM_QUANTITY = "quantity";
    public static final String COLUMN_ITEM_CATEGORY = "category";

    // SQL statement to create Users table
    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + " ("
            + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_USER_NAME + " TEXT UNIQUE NOT NULL, "
            + COLUMN_USER_PASSWORD + " TEXT NOT NULL);";

    // SQL statement to create Inventory table
    private static final String CREATE_TABLE_INVENTORY = "CREATE TABLE " + TABLE_INVENTORY + " ("
            + COLUMN_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_ITEM_NAME + " TEXT NOT NULL, "
            + COLUMN_ITEM_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
            + COLUMN_ITEM_CATEGORY + " TEXT DEFAULT 'General');";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create user and inventory tables
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_INVENTORY);

        // Pre-populate with sample default user and starting inventory
        ContentValues userValues = new ContentValues();
        userValues.put(COLUMN_USER_NAME, "admin");
        userValues.put(COLUMN_USER_PASSWORD, "password123");
        db.insert(TABLE_USERS, null, userValues);

        insertDefaultItem(db, "Standard Pallets", 45, "Logistics");
        insertDefaultItem(db, "Heavy Duty Forklift Battery", 4, "Machinery");
        insertDefaultItem(db, "Corrugated Shipping Boxes (L)", 120, "Packaging");
        insertDefaultItem(db, "Packing Tape Rolls (6-Pack)", 18, "Supplies");
        insertDefaultItem(db, "Industrial Barcode Scanners", 1, "Electronics");
        insertDefaultItem(db, "Safety Hard Hats", 0, "Safety");
    }

    private void insertDefaultItem(SQLiteDatabase db, String name, int quantity, String category) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ITEM_NAME, name);
        values.put(COLUMN_ITEM_QUANTITY, quantity);
        values.put(COLUMN_ITEM_CATEGORY, category);
        db.insert(TABLE_INVENTORY, null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INVENTORY);
        onCreate(db);
    }

    // ==========================================
    // USER AUTHENTICATION METHODS
    // ==========================================

    /**
     * Check if a username and password match an existing record in the database.
     */
    public boolean checkUserCredentials(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_USER_ID};
        String selection = COLUMN_USER_NAME + " = ? AND " + COLUMN_USER_PASSWORD + " = ?";
        String[] selectionArgs = {username.trim(), password};

        Cursor cursor = db.query(
                TABLE_USERS,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        int count = cursor != null ? cursor.getCount() : 0;
        if (cursor != null) {
            cursor.close();
        }
        return count > 0;
    }

    /**
     * Check if a username already exists.
     */
    public boolean checkUsernameExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_USER_ID};
        String selection = COLUMN_USER_NAME + " = ?";
        String[] selectionArgs = {username.trim()};

        Cursor cursor = db.query(
                TABLE_USERS,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        int count = cursor != null ? cursor.getCount() : 0;
        if (cursor != null) {
            cursor.close();
        }
        return count > 0;
    }

    /**
     * Register a new user and save credentials into the SQLite database.
     */
    public boolean registerUser(String username, String password) {
        if (checkUsernameExists(username)) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_NAME, username.trim());
        values.put(COLUMN_USER_PASSWORD, password);

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    // ==========================================
    // INVENTORY CRUD OPERATIONS
    // ==========================================

    /**
     * CREATE: Add a new item to the inventory database.
     */
    public long addItem(String name, int quantity, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ITEM_NAME, name.trim());
        values.put(COLUMN_ITEM_QUANTITY, Math.max(0, quantity));
        values.put(COLUMN_ITEM_CATEGORY, category != null && !category.isEmpty() ? category.trim() : "General");

        return db.insert(TABLE_INVENTORY, null, values);
    }

    /**
     * READ: Retrieve all inventory items from the database.
     */
    public List<InventoryItem> getAllItems() {
        List<InventoryItem> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_INVENTORY,
                null,
                null,
                null,
                null,
                null,
                COLUMN_ITEM_NAME + " ASC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int idIndex = cursor.getColumnIndexOrThrow(COLUMN_ITEM_ID);
                int nameIndex = cursor.getColumnIndexOrThrow(COLUMN_ITEM_NAME);
                int qtyIndex = cursor.getColumnIndexOrThrow(COLUMN_ITEM_QUANTITY);
                int catIndex = cursor.getColumnIndexOrThrow(COLUMN_ITEM_CATEGORY);

                long id = cursor.getLong(idIndex);
                String name = cursor.getString(nameIndex);
                int quantity = cursor.getInt(qtyIndex);
                String category = cursor.getString(catIndex);

                items.add(new InventoryItem(id, name, quantity, category));
            } while (cursor.moveToNext());

            cursor.close();
        }

        return items;
    }

    /**
     * UPDATE: Change the quantity or details of an existing inventory item.
     */
    public boolean updateItemQuantity(long id, int newQuantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ITEM_QUANTITY, Math.max(0, newQuantity));

        int rows = db.update(
                TABLE_INVENTORY,
                values,
                COLUMN_ITEM_ID + " = ?",
                new String[]{String.valueOf(id)}
        );
        return rows > 0;
    }

    /**
     * UPDATE: Change the name, quantity, and category of an item.
     */
    public boolean updateItem(long id, String name, int quantity, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ITEM_NAME, name.trim());
        values.put(COLUMN_ITEM_QUANTITY, Math.max(0, quantity));
        values.put(COLUMN_ITEM_CATEGORY, category.trim());

        int rows = db.update(
                TABLE_INVENTORY,
                values,
                COLUMN_ITEM_ID + " = ?",
                new String[]{String.valueOf(id)}
        );
        return rows > 0;
    }

    /**
     * DELETE: Remove an item completely from the database.
     */
    public boolean deleteItem(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(
                TABLE_INVENTORY,
                COLUMN_ITEM_ID + " = ?",
                new String[]{String.valueOf(id)}
        );
        return rows > 0;
    }
}
