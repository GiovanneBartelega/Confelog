package com.example.confelog.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.confelog.data.AppDatabase
import com.example.confelog.model.Order
import com.example.confelog.repository.OrderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext

class OrderViewModel(application: Application): AndroidViewModel(application) {
    private val repo: OrderRepository
    val orders = AppDatabase.getInstance(application).orderDao().observeAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        val db = AppDatabase.getInstance(application)
        repo = OrderRepository(db.orderDao())
    }

    fun adicionar(order: Order, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            val id = withContext(Dispatchers.IO) { repo.add(order) }
            onDone(id > 0)
        }
    }
}
