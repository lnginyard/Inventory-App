package com.example.data.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val username: String,
    val passwordHash: String,
    val fullName: String = "Warehouse Specialist",
    val role: String = "Manager",
    val createdAt: Long = System.currentTimeMillis()
)
