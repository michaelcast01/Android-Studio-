package com.example.tiendasuplementacion.interfaces


import com.example.tiendasuplementacion.model.Order
import retrofit2.http.*

interface OrderApiService {
    @GET("/api/orders")
    suspend fun getAll(): List<Order>

    @GET("/api/orders/{id}")
    suspend fun getById(@Path("id") id: Long): Order

    @POST("/api/orders")
    suspend fun create(@Body order: Order): Order

    @PUT("/api/orders/{id}")
    suspend fun update(@Path("id") id: Long, @Body order: Order): Order

    @DELETE("/api/orders/{id}")
    suspend fun delete(@Path("id") id: Long)
}
