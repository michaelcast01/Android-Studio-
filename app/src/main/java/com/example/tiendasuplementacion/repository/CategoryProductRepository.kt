package com.example.tiendasuplementacion.repository


import com.example.tiendasuplementacion.model.CategoryProduct
import com.example.tiendasuplementacion.network.RetrofitClient

class CategoryProductRepository {
    private val service = RetrofitClient.categoryProductService

    suspend fun getAll() = service.getAll()
    suspend fun getById(id: Long) = service.getById(id)
    suspend fun create(data: CategoryProduct) = service.create(data)
    suspend fun update(id: Long, data: CategoryProduct) = service.update(id, data)
    suspend fun delete(id: Long) = service.delete(id)
}
