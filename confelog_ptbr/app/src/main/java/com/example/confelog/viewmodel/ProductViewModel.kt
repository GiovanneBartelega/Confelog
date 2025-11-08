package com.example.confelog.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.confelog.data.AppDatabase
import com.example.confelog.model.Product
import com.example.confelog.repository.ProductRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductViewModel(application: Application): AndroidViewModel(application) {
    private val repo: ProductRepository
    val products = AppDatabase.getInstance(application).productDao().observeAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        val db = AppDatabase.getInstance(application)
        repo = ProductRepository(db.productDao())
    }

    fun adicionar(product: Product, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            val id = withContext(Dispatchers.IO) { repo.add(product) }
            onDone(id > 0)
        }
    }

    fun atualizar(product: Product, onDone: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.update(product)
            onDone()
        }
    }

    fun remover(product: Product, onDone: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.delete(product)
            onDone()
        }
    }
}
