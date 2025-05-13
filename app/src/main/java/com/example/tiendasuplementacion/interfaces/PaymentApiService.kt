package com.example.tiendasuplementacion.interfaces


import com.example.tiendasuplementacion.model.Payment
import retrofit2.http.*

interface PaymentApiService {
    @GET("/api/payments")
    suspend fun getAll(): List<Payment>

    @GET("/api/payments/{id}")
    suspend fun getById(@Path("id") id: Long): Payment

    @POST("/api/payments")
    suspend fun create(@Body payment: Payment): Payment

    @PUT("/api/payments/{id}")
    suspend fun update(@Path("id") id: Long, @Body payment: Payment): Payment

    @DELETE("/api/payments/{id}")
    suspend fun delete(@Path("id") id: Long)
}
