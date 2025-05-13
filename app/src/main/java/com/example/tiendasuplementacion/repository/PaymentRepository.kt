package com.example.tiendasuplementacion.repository


import com.example.tiendasuplementacion.model.Payment
import com.example.tiendasuplementacion.network.RetrofitClient

class PaymentRepository {
    private val service = RetrofitClient.paymentService

    suspend fun getAll() = service.getAll()
    suspend fun getById(id: Long) = service.getById(id)
    suspend fun create(payment: Payment) = service.create(payment)
    suspend fun update(id: Long, payment: Payment) = service.update(id, payment)
    suspend fun delete(id: Long) = service.delete(id)
}
