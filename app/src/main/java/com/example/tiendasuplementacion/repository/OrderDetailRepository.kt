package com.example.tiendasuplementacion.repository

import com.example.tiendasuplementacion.model.OrderDetail
import com.example.tiendasuplementacion.network.RetrofitClient

class OrderDetailRepository {
    private val service = RetrofitClient.orderDetailService

    suspend fun getAll() = service.getAll()
    suspend fun getById(id: Long) = service.getById(id)
    suspend fun create(detail: OrderDetail) = service.create(detail)
    suspend fun update(id: Long, detail: OrderDetail) = service.update(id, detail)
    suspend fun delete(id: Long) = service.delete(id)
}
