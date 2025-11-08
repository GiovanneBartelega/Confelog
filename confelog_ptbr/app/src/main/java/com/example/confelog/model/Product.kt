package com.example.confelog.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val description: String?,
    val price: Double,
    val quantity: Int,
    val category: String, // "Insumo", "Embalagem", "Produto Final"
    val unitType: String, // "unidade", "fardos"
    val measurementValue: Double?, // O valor da medida (ex: 100.0)
    val measurementUnit: String?, // A unidade da medida (ex: "g", "ml")
    val unitsPerFardo: Int? // Número de unidades por fardo
)
