package com.example.data.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory_items ORDER BY name ASC")
    fun getAllItemsFlow(): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_items WHERE name LIKE '%' || :query || '%' OR sku LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchItemsFlow(query: String): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_items WHERE id = :id LIMIT 1")
    suspend fun getItemById(id: Int): InventoryItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: InventoryItemEntity): Long

    @Update
    suspend fun updateItem(item: InventoryItemEntity)

    @Query("UPDATE inventory_items SET quantity = :newQuantity, lastUpdated = :timestamp WHERE id = :id")
    suspend fun updateQuantity(id: Int, newQuantity: Int, timestamp: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteItem(item: InventoryItemEntity)

    @Query("DELETE FROM inventory_items WHERE id = :id")
    suspend fun deleteItemById(id: Int)

    @Query("SELECT COUNT(*) FROM inventory_items")
    suspend fun getItemCount(): Int
}
