package com.example.confelog.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "producao")
data class Producao(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val orderId: Int,
    val productId: Int,
    val productName: String, // Para facilitar a exibição na lista
    val quantity: Int,
    var status: String, // "Pendente", "Em produção", "Concluída"
    var startDate: Date?,
    var completionDate: Date?
)
