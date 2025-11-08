package com.example.confelog.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "inventory_transactions")
data class InventoryTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: Int,
    val quantityChange: Int, // Positivo para entrada, negativo para saída
    val date: Date,
    val reason: String // Ex: "Venda", "Ajuste manual", "Compra"
)
