package com.example.confelog.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val clientName: String,
    val productId: Int,
    val productName: String,
    val quantity: Int,
    val dateMillis: Long,
    val address: String,
    val value: Double
)
