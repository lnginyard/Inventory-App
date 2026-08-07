package com.example.data.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory_items")
data class InventoryItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val quantity: Int,
    val sku: String = "SKU-100",
    val category: String = "General",
    val minThreshold: Int = 5,
    val notes: String = "",
    val location: String = "Aisle 1",
    val lastUpdated: Long = System.currentTimeMillis()
)
