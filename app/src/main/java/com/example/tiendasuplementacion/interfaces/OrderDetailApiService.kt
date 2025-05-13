package com.example.tiendasuplementacion.interfaces


import com.example.tiendasuplementacion.model.OrderDetail
import retrofit2.http.*

interface OrderDetailApiService {
    @GET("/api/order-details")
    suspend fun getAll(): List<OrderDetail>

    @GET("/api/order-details/{id}")
    suspend fun getById(@Path("id") id: Long): OrderDetail

    @POST("/api/order-details")
    suspend fun create(@Body detail: OrderDetail): OrderDetail

    @PUT("/api/order-details/{id}")
    suspend fun update(@Path("id") id: Long, @Body detail: OrderDetail): OrderDetail

    @DELETE("/api/order-details/{id}")
    suspend fun delete(@Path("id") id: Long)
}
