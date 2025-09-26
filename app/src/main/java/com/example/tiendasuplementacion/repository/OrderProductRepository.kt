package com.example.tiendasuplementacion.repository

import com.example.tiendasuplementacion.model.CreateOrderProductRequest
import com.example.tiendasuplementacion.model.OrderProductDetail
import com.example.tiendasuplementacion.model.UpdateOrderProductRequest
import com.example.tiendasuplementacion.network.RetrofitClient

class OrderProductRepository {
    private val service = RetrofitClient.orderProductService

    suspend fun create(orderProduct: CreateOrderProductRequest): OrderProductDetail {
        return service.create(orderProduct)
    }

    suspend fun update(id: Long, orderProduct: UpdateOrderProductRequest): OrderProductDetail {
        return service.update(id, orderProduct)
    }

    suspend fun delete(id: Long): Boolean {
        val response = service.delete(id)
        return response.isSuccessful
    }

    suspend fun getById(id: Long): OrderProductDetail {
        return service.getById(id)
    }
} 