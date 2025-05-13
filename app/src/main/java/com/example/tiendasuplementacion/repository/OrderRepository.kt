package com.example.tiendasuplementacion.repository


import com.example.tiendasuplementacion.model.Order
import com.example.tiendasuplementacion.network.RetrofitClient

class OrderRepository {
    private val service = RetrofitClient.orderService

    suspend fun getAll() = service.getAll()
    suspend fun getById(id: Long) = service.getById(id)
    suspend fun create(order: Order) = service.create(order)
    suspend fun update(id: Long, order: Order) = service.update(id, order)
    suspend fun delete(id: Long) = service.delete(id)
}
