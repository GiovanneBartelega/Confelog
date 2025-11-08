package com.example.confelog.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipe_ingredients")
data class RecipeIngredient(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val recipeId: Int, // ID da receita
    val ingredientId: Int, // ID do insumo (que é um Produto)
    val quantityNeeded: Double // Quantidade do insumo necessária (ex: em gramas)
)
