package com.example.confelog.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val finalProductId: Int, // ID do produto final ao qual esta receita pertence
    val name: String // Ex: "Receita Padrão Bolo de Chocolate"
)
