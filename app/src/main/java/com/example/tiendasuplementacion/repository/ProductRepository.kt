package com.example.tiendasuplementacion.repository

import com.example.tiendasuplementacion.model.Product
import com.example.tiendasuplementacion.network.RetrofitClient

class ProductRepository {
    private val service = RetrofitClient.productService

    suspend fun getAll(includeDisabled: Boolean? = null) = service.getAll(includeDisabled)
    suspend fun getById(id: Long) = service.getById(id)
    suspend fun create(product: Product) = service.create(product)
    suspend fun update(id: Long, product: Product) = service.update(id, product)
    suspend fun delete(id: Long) = service.delete(id)
    suspend fun toggleEnabled(id: Long) = service.toggleEnabled(id)
}
