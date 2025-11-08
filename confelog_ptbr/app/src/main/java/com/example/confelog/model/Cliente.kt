package com.example.confelog.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clientes")
data class Cliente(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nome: String,
    val cpf: String,
    val rua: String,
    val bairro: String,
    val numero: String,
    val telefone: String
)
