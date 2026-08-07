package com.example.data.repository

import android.content.Context
import com.example.data.data.InventoryDao
import com.example.data.data.InventoryDatabase
import com.example.data.data.InventoryItemEntity
import com.example.data.data.UserDao
import com.example.data.data.UserEntity
import com.example.data.sms.SmsAlertManager
import com.example.data.sms.SmsSendResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class InventoryRepository(
    private val inventoryDao: InventoryDao,
    private val userDao: UserDao,
    private val smsAlertManager: SmsAlertManager
) {

    val allItemsFlow: Flow<List<InventoryItemEntity>> = inventoryDao.getAllItemsFlow()

    fun searchItemsFlow(query: String): Flow<List<InventoryItemEntity>> {
        return if (query.isBlank()) {
            inventoryDao.getAllItemsFlow()
        } else {
            inventoryDao.searchItemsFlow(query.trim())
        }
    }

    suspend fun seedDefaultDataIfEmpty() = withContext(Dispatchers.IO) {
        if (userDao.getUserCount() == 0) {
            userDao.insertUser(
                UserEntity(
                    username = "admin",
                    passwordHash = "password123",
                    fullName = "Warehouse Supervisor",
                    role = "Lead Admin"
                )
            )
            userDao.insertUser(
                UserEntity(
                    username = "warehouse1",
                    passwordHash = "stock2026",
                    fullName = "John Inventory",
                    role = "Stock Specialist"
                )
            )
        }

        if (inventoryDao.getItemCount() == 0) {
            val defaultItems = listOf(
                InventoryItemEntity(
                    name = "Industrial Safety Helmets (Blue)",
                    quantity = 24,
                    sku = "SKU-HLM-101",
                    category = "Safety Gear",
                    minThreshold = 5,
                    location = "Aisle 1A",
                    notes = "OSHA compliant high-impact helmets"
                ),
                InventoryItemEntity(
                    name = "Heavy Duty Pallet Straps",
                    quantity = 8,
                    sku = "SKU-STR-202",
                    category = "Packaging",
                    minThreshold = 10,
                    location = "Aisle 2B",
                    notes = "1000lb break strength polyester"
                ),
                InventoryItemEntity(
                    name = "Industrial Barcode Scanners",
                    quantity = 3,
                    sku = "SKU-SCN-303",
                    category = "Electronics",
                    minThreshold = 4,
                    location = "Tech Locker 1",
                    notes = "Wireless 2D Bluetooth scanners"
                ),
                InventoryItemEntity(
                    name = "Hydraulic Hand Pallet Truck 5500lbs",
                    quantity = 1,
                    sku = "SKU-TRK-404",
                    category = "Equipment",
                    minThreshold = 2,
                    location = "Dock 3",
                    notes = "Heavy duty steel pallet jack"
                ),
                InventoryItemEntity(
                    name = "Warehouse Forklift Battery Pack",
                    quantity = 0, // Starts at 0 to showcase zero-stock badge and alert!
                    sku = "SKU-BAT-505",
                    category = "Equipment",
                    minThreshold = 2,
                    location = "Maintenance Bay",
                    notes = "48V 600Ah Lead Acid Battery"
                ),
                InventoryItemEntity(
                    name = "Clear Heavy Duty Stretch Film (6 Rolls)",
                    quantity = 42,
                    sku = "SKU-FLM-606",
                    category = "Packaging",
                    minThreshold = 15,
                    location = "Aisle 2C",
                    notes = "80 gauge 18 inch width"
                ),
                InventoryItemEntity(
                    name = "Nitrile Rubber Grip Gloves (Box of 100)",
                    quantity = 15,
                    sku = "SKU-GLV-707",
                    category = "Safety Gear",
                    minThreshold = 8,
                    location = "Aisle 1B",
                    notes = "Size Large extra grip"
                ),
                InventoryItemEntity(
                    name = "Rechargeable LED Work Lights",
                    quantity = 6,
                    sku = "SKU-LGT-808",
                    category = "Tools",
                    minThreshold = 3,
                    location = "Aisle 3A",
                    notes = "5000 Lumens IP65 waterproof"
                )
            )

            for (item in defaultItems) {
                inventoryDao.insertItem(item)
            }
        }
    }

    suspend fun loginUser(username: String, passwordAttempt: String): UserResult = withContext(Dispatchers.IO) {
        val trimmed = username.trim()
        if (trimmed.isEmpty() || passwordAttempt.isEmpty()) {
            return@withContext UserResult.Error("Username and password are required.")
        }

        val user = userDao.getUserByUsername(trimmed)
        if (user == null) {
            return@withContext UserResult.Error("Account '$trimmed' not found. Please create an account.")
        }

        if (user.passwordHash == passwordAttempt) {
            UserResult.Success(user)
        } else {
            UserResult.Error("Incorrect password. Please try again.")
        }
    }

    suspend fun createAccount(username: String, passwordAttempt: String, fullName: String): UserResult = withContext(Dispatchers.IO) {
        val trimmed = username.trim()
        if (trimmed.length < 3) {
            return@withContext UserResult.Error("Username must be at least 3 characters long.")
        }
        if (passwordAttempt.length < 4) {
            return@withContext UserResult.Error("Password must be at least 4 characters long.")
        }

        val existing = userDao.getUserByUsername(trimmed)
        if (existing != null) {
            return@withContext UserResult.Error("Username '$trimmed' is already registered.")
        }

        val newUser = UserEntity(
            username = trimmed,
            passwordHash = passwordAttempt,
            fullName = fullName.ifBlank { "Warehouse Specialist" }
        )
        userDao.insertUser(newUser)
        UserResult.Success(newUser)
    }

    suspend fun insertItem(item: InventoryItemEntity): Long = withContext(Dispatchers.IO) {
        val id = inventoryDao.insertItem(item)
        if (item.quantity == 0) {
            smsAlertManager.sendZeroStockAlert(item.name, item.sku)
        }
        id
    }

    suspend fun updateItem(item: InventoryItemEntity) = withContext(Dispatchers.IO) {
        val oldItem = inventoryDao.getItemById(item.id)
        inventoryDao.updateItem(item)
        if (item.quantity == 0 && (oldItem == null || oldItem.quantity > 0)) {
            smsAlertManager.sendZeroStockAlert(item.name, item.sku)
        }
    }

    suspend fun increaseQuantity(itemId: Int): SmsSendResult? = withContext(Dispatchers.IO) {
        val item = inventoryDao.getItemById(itemId) ?: return@withContext null
        val newQty = item.quantity + 1
        inventoryDao.updateQuantity(itemId, newQty)
        null
    }

    suspend fun decreaseQuantity(itemId: Int): SmsSendResult? = withContext(Dispatchers.IO) {
        val item = inventoryDao.getItemById(itemId) ?: return@withContext null
        if (item.quantity <= 0) return@withContext null

        val newQty = item.quantity - 1
        inventoryDao.updateQuantity(itemId, newQty)

        if (newQty == 0) {
            // Trigger zero-stock SMS alert!
            smsAlertManager.sendZeroStockAlert(item.name, item.sku)
        } else null
    }

    suspend fun deleteItem(item: InventoryItemEntity) = withContext(Dispatchers.IO) {
        inventoryDao.deleteItem(item)
    }

    companion object {
        @Volatile
        private var INSTANCE: InventoryRepository? = null

        fun getRepository(context: Context): InventoryRepository {
            return INSTANCE ?: synchronized(this) {
                val db = InventoryDatabase.getDatabase(context)
                val smsManager = SmsAlertManager(context)
                val instance = InventoryRepository(
                    inventoryDao = db.inventoryDao(),
                    userDao = db.userDao(),
                    smsAlertManager = smsManager
                )
                INSTANCE = instance
                instance
            }
        }
    }
}

sealed class UserResult {
    data class Success(val user: UserEntity) : UserResult()
    data class Error(val message: String) : UserResult()
}
