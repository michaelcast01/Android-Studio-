package com.example.tiendasuplementacion.repository

import android.util.Log
import com.example.tiendasuplementacion.model.Payment
import com.example.tiendasuplementacion.model.PaymentDetail
import com.example.tiendasuplementacion.network.RetrofitClient
import retrofit2.HttpException
import java.io.IOException
import kotlin.jvm.Throws

class PaymentRepository {
    private val service = RetrofitClient.paymentService

    suspend fun getPaymentDetails(userId: Long): List<PaymentDetail> {
        Log.d("PaymentRepository", "Getting payment details for user: $userId")
        return try {
            val response = service.getPaymentDetails(userId)
            Log.d("PaymentRepository", "Received response: $response")
            response
        } catch (e: HttpException) {
            Log.e("PaymentRepository", "HTTP error: ${e.code()}", e)
            throw Exception("Error del servidor: ${e.message()}")
        } catch (e: IOException) {
            Log.e("PaymentRepository", "IO error", e)
            throw Exception("Error de conexión: ${e.message}")
        } catch (e: Exception) {
            Log.e("PaymentRepository", "Unexpected error", e)
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
        Log.d("PaymentRepository", "Attempting to save payment detail to API")
        Log.d("PaymentRepository", "Request body: $paymentDetail")
        return try {
            val response = service.savePaymentDetail(paymentDetail)
            Log.d("PaymentRepository", "API response: $response")
            response
        } catch (e: HttpException) {
            Log.e("PaymentRepository", "HTTP error: ${e.code()}", e)
            Log.e("PaymentRepository", "Error body: ${e.response()?.errorBody()?.string()}")
            throw Exception("Error del servidor: ${e.message()}")
        } catch (e: IOException) {
            Log.e("PaymentRepository", "IO error", e)
            throw Exception("Error de conexión: ${e.message}")
        } catch (e: Exception) {
            Log.e("PaymentRepository", "Unexpected error", e)
            throw Exception("Error inesperado: ${e.message}")
        }
    }

    suspend fun deletePaymentDetail(id: Long) {
        Log.d("PaymentRepository", "Attempting to delete payment detail: $id")
        try {
            service.deletePaymentDetail(id)
            Log.d("PaymentRepository", "Successfully deleted payment detail")
        } catch (e: HttpException) {
            if (e.code() == 204 || e.code() == 200) {
                Log.d("PaymentRepository", "Delete successful with status code: ${e.code()}")
                return
            }
            Log.e("PaymentRepository", "HTTP error: ${e.code()}", e)
            throw Exception("Error del servidor: ${e.message()}")
        } catch (e: IOException) {
            Log.e("PaymentRepository", "IO error", e)
            throw Exception("Error de conexión: ${e.message}")
        } catch (e: Exception) {
            if (e is KotlinNullPointerException && e.message?.contains("Response") == true) {
                Log.d("PaymentRepository", "Delete successful with empty response")
                return
            }
            Log.e("PaymentRepository", "Unexpected error", e)
            throw Exception("Error inesperado: ${e.message}")
        }
    }

    suspend fun updatePaymentDetail(id: Long, paymentDetail: PaymentDetail): PaymentDetail {
        Log.d("PaymentRepository", "Attempting to update payment detail: $id")
        Log.d("PaymentRepository", "Update data: $paymentDetail")
        return try {
            val response = service.updatePaymentDetail(id, paymentDetail)
            Log.d("PaymentRepository", "Successfully updated payment detail. Response: $response")
            response
        } catch (e: HttpException) {
            Log.e("PaymentRepository", "HTTP error: ${e.code()}", e)
            Log.e("PaymentRepository", "Error body: ${e.response()?.errorBody()?.string()}")
            throw Exception("Error del servidor: ${e.message()}")
        } catch (e: IOException) {
            Log.e("PaymentRepository", "IO error", e)
            throw Exception("Error de conexión: ${e.message}")
        } catch (e: Exception) {
            Log.e("PaymentRepository", "Unexpected error", e)
            throw Exception("Error inesperado: ${e.message}")
        }
    }
}
