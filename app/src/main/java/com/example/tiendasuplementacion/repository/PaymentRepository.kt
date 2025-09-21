package com.example.tiendasuplementacion.repository

import android.util.Log
import com.example.tiendasuplementacion.model.Payment
import com.example.tiendasuplementacion.model.PaymentDetail
import com.example.tiendasuplementacion.model.TestPaymentRequest
import com.example.tiendasuplementacion.model.TestPaymentResponse
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

    suspend fun savePaymentDetail(paymentDetail: PaymentDetail): PaymentDetail {
        return try {
            service.savePaymentDetail(paymentDetail)
        } catch (e: HttpException) {
            throw Exception("Error del servidor: ${e.message()}")
        } catch (e: IOException) {
            throw Exception("Error de conexión: ${e.message}")
        } catch (e: Exception) {
            throw Exception("Error inesperado: ${e.message}")
        }
    }

    suspend fun deletePaymentDetail(id: Long) {
        try {
            service.deletePaymentDetail(id)
        } catch (e: HttpException) {
            if (e.code() == 204 || e.code() == 200) {
                return
            }
            throw Exception("Error del servidor: ${e.message()}")
        } catch (e: IOException) {
            throw Exception("Error de conexión: ${e.message}")
        } catch (e: Exception) {
            if (e is KotlinNullPointerException && e.message?.contains("Response") == true) {
                return
            }
            throw Exception("Error inesperado: ${e.message}")
        }
    }

    suspend fun updatePaymentDetail(id: Long, paymentDetail: PaymentDetail): PaymentDetail {
        return try {
            service.updatePaymentDetail(id, paymentDetail)
        } catch (e: HttpException) {
            throw Exception("Error del servidor: ${e.message()}")
        } catch (e: IOException) {
            throw Exception("Error de conexión: ${e.message}")
        } catch (e: Exception) {
            throw Exception("Error inesperado: ${e.message}")
        }
    }

    suspend fun createTestPayment(request: TestPaymentRequest): TestPaymentResponse {
        return try {
            service.createTestPayment(request)
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            
            when (e.code()) {
                400 -> {
                    throw Exception("Error en el pago de prueba: ${errorBody ?: e.message()}")
                }
                else -> throw Exception("Error del servidor: ${e.message()}")
            }
        } catch (e: IOException) {
            throw Exception("Error de conexión: ${e.message}")
        } catch (e: Exception) {
            throw Exception("Error inesperado: ${e.message}")
        }
    }
}
