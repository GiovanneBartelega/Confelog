package com.example.confelog.repository

import com.example.confelog.data.OrderDao
import com.example.confelog.model.Order
import kotlinx.coroutines.flow.Flow

class OrderRepository(private val dao: OrderDao) {
    suspend fun add(order: Order) = dao.insert(order)
    fun observeAll(): Flow<List<Order>> = dao.observeAll()
}
