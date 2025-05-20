package com.example.tiendasuplementacion.repository

import com.example.tiendasuplementacion.model.OrderProductDetail
import com.example.tiendasuplementacion.network.RetrofitClient

class OrderProductRepository {
    private val service = RetrofitClient.orderProductService

    suspend fun create(orderProduct: OrderProductDetail) = service.create(orderProduct)
} 