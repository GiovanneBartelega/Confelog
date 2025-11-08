package com.example.confelog.repository

import com.example.confelog.data.ProductDao
import com.example.confelog.model.Product
import kotlinx.coroutines.flow.Flow

class ProductRepository(private val dao: ProductDao) {
    suspend fun add(product: Product) = dao.insert(product)
    suspend fun update(product: Product) = dao.update(product)
    suspend fun delete(product: Product) = dao.delete(product)
    fun observeAll(): Flow<List<Product>> = dao.observeAll()
    suspend fun search(q: String) = dao.search(q)
}
