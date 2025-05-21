package com.example.tiendasuplementacion.interfaces

import com.example.tiendasuplementacion.model.Payment
import com.example.tiendasuplementacion.model.PaymentDetail
import retrofit2.http.*

interface PaymentApiService {
    @GET("/api/additional-info-payments-details/user/{userId}")
    suspend fun getPaymentDetails(@Path("userId") userId: Long): List<PaymentDetail>

    @GET("api/payments")
    suspend fun getAll(): List<Payment>

    @GET("api/payments/{id}")
    suspend fun getById(@Path("id") id: Long): Payment

    @POST("api/payments")
    suspend fun create(@Body payment: Payment): Payment

    @PUT("api/payments/{id}")
    suspend fun update(@Path("id") id: Long, @Body payment: Payment): Payment

    @DELETE("api/payments/{id}")
    suspend fun delete(@Path("id") id: Long)

    @POST("/api/additional-info-payments")
    suspend fun savePaymentDetail(@Body paymentDetail: PaymentDetail): PaymentDetail

    @DELETE("/api/additional-info-payments/{id}")
    suspend fun deletePaymentDetail(@Path("id") id: Long)

    @PUT("/api/additional-info-payments/{id}")
    suspend fun updatePaymentDetail(@Path("id") id: Long, @Body paymentDetail: PaymentDetail): PaymentDetail
}
