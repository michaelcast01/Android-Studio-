package com.example.tiendasuplementacion.repository

import com.example.tiendasuplementacion.model.Payment
import com.example.tiendasuplementacion.model.PaymentDetail
import com.example.tiendasuplementacion.network.RetrofitClient
import retrofit2.HttpException
import java.io.IOException

class PaymentRepository {
    private val service = RetrofitClient.paymentService

    suspend fun getPaymentDetails(userId: Long): List<PaymentDetail> {
        return try {
            service.getPaymentDetails(userId)
        } catch (e: HttpException) {
            throw Exception("Error del servidor: ${e.message()}")
        } catch (e: IOException) {
            throw Exception("Error de conexión: ${e.message}")
        } catch (e: Exception) {
            throw Exception("Error inesperado: ${e.message}")
        }
    }

    suspend fun getAll(): List<Payment> {
        return try {
            service.getAll()
        } catch (e: HttpException) {
            throw Exception("Error del servidor: ${e.message()}")
        } catch (e: IOException) {
            throw Exception("Error de conexión: ${e.message}")
        } catch (e: Exception) {
            throw Exception("Error inesperado: ${e.message}")
        }
    }

    suspend fun getById(id: Long): Payment {
        return try {
            service.getById(id)
        } catch (e: HttpException) {
            throw Exception("Error del servidor: ${e.message()}")
        } catch (e: IOException) {
            throw Exception("Error de conexión: ${e.message}")
        } catch (e: Exception) {
            throw Exception("Error inesperado: ${e.message}")
        }
    }

    suspend fun create(payment: Payment): Payment {
        return try {
            service.create(payment)
        } catch (e: HttpException) {
            throw Exception("Error del servidor: ${e.message()}")
        } catch (e: IOException) {
            throw Exception("Error de conexión: ${e.message}")
        } catch (e: Exception) {
            throw Exception("Error inesperado: ${e.message}")
        }
    }

    suspend fun update(id: Long, payment: Payment): Payment {
        return try {
            service.update(id, payment)
        } catch (e: HttpException) {
            throw Exception("Error del servidor: ${e.message()}")
        } catch (e: IOException) {
            throw Exception("Error de conexión: ${e.message}")
        } catch (e: Exception) {
            throw Exception("Error inesperado: ${e.message}")
        }
    }

    suspend fun delete(id: Long) {
        try {
            service.delete(id)
        } catch (e: HttpException) {
            throw Exception("Error del servidor: ${e.message()}")
        } catch (e: IOException) {
            throw Exception("Error de conexión: ${e.message}")
        } catch (e: Exception) {
            throw Exception("Error inesperado: ${e.message}")
        }
    }
}
