package com.example.tiendasuplementacion.interfaces

import com.example.tiendasuplementacion.model.CreateOrderProductRequest
import com.example.tiendasuplementacion.model.OrderProductDetail
import com.example.tiendasuplementacion.model.UpdateOrderProductRequest
import retrofit2.Response
import retrofit2.http.*

interface OrderProductApiService {
    @POST("/api/order-products")
    suspend fun create(@Body orderProduct: CreateOrderProductRequest): OrderProductDetail

    @PUT("/api/order-products/{id}")
    suspend fun update(@Path("id") id: Long, @Body orderProduct: UpdateOrderProductRequest): OrderProductDetail

    @DELETE("/api/order-products/{id}")
    suspend fun delete(@Path("id") id: Long): Response<Unit>

    @GET("/api/order-products/{id}")
    suspend fun getById(@Path("id") id: Long): OrderProductDetail

    @GET("/api/order-products/order/{orderId}")
    suspend fun getByOrderId(@Path("orderId") orderId: Long): List<OrderProductDetail>
} 