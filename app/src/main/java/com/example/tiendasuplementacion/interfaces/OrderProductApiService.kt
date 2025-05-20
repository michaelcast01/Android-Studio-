package com.example.tiendasuplementacion.interfaces

import com.example.tiendasuplementacion.model.OrderProductDetail
import retrofit2.http.*

interface OrderProductApiService {
    @POST("/api/order-products")
    suspend fun create(@Body orderProduct: OrderProductDetail): OrderProductDetail
} 