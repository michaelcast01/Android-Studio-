package com.example.tiendasuplementacion.utils

import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * Utilidades para manejo de errores de la API de OrderProducts.
 */
object OrderProductErrorHandler {

    /**
     * Convierte una excepción en un mensaje de error amigable para el usuario.
     */
    fun getErrorMessage(exception: Throwable): String {
        return when (exception) {
            is HttpException -> {
                when (exception.code()) {
                    400 -> parseHttpErrorMessage(exception) ?: "Datos inválidos en la solicitud"
                    404 -> "Producto o pedido no encontrado"
                    409 -> "Stock insuficiente para este producto"
                    500 -> "Error interno del servidor. Intente nuevamente."
                    else -> "Error del servidor (${exception.code()})"
                }
            }
            is SocketTimeoutException -> "Tiempo de espera agotado. Verifique su conexión."
            is IOException -> "Error de conexión. Verifique su conexión a internet."
            else -> exception.message ?: "Error desconocido"
        }
    }

    /**
     * Intenta extraer el mensaje de error del cuerpo de respuesta HTTP.
     */
    private fun parseHttpErrorMessage(httpException: HttpException): String? {
        return try {
            val errorBody = httpException.response()?.errorBody()?.string()
            // Aquí podrías parsear JSON si tu backend devuelve errores en formato JSON
            // Por ejemplo: {"message": "Stock insuficiente", "code": "INSUFFICIENT_STOCK"}
            errorBody?.let { body ->
                // Extracción simple de mensaje (ajustar según formato del backend)
                when {
                    body.contains("Stock insuficiente", ignoreCase = true) -> 
                        "No hay suficiente stock disponible"
                    body.contains("Product ID es requerido", ignoreCase = true) -> 
                        "ID del producto es requerido"
                    body.contains("Order ID es requerido", ignoreCase = true) -> 
                        "ID del pedido es requerido"
                    else -> null
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Determina si un error es recuperable (el usuario puede intentar de nuevo).
     */
    fun isRetryableError(exception: Throwable): Boolean {
        return when (exception) {
            is SocketTimeoutException -> true
            is IOException -> true
            is HttpException -> exception.code() in 500..599 // Errores de servidor
            else -> false
        }
    }

    /**
     * Determina si un error es relacionado con stock insuficiente.
     */
    fun isStockError(exception: Throwable): Boolean {
        return when (exception) {
            is HttpException -> {
                exception.code() == 409 || 
                parseHttpErrorMessage(exception)?.contains("stock", ignoreCase = true) == true
            }
            else -> exception.message?.contains("stock", ignoreCase = true) == true
        }
    }
}

/**
 * Clase para representar diferentes tipos de errores de OrderProduct.
 */
sealed class OrderProductError(val message: String, val isRetryable: Boolean = false) {
    object InsufficientStock : OrderProductError("No hay suficiente stock disponible")
    object ProductNotFound : OrderProductError("Producto no encontrado")
    object OrderNotFound : OrderProductError("Pedido no encontrado")
    object InvalidData : OrderProductError("Datos inválidos")
    object NetworkError : OrderProductError("Error de conexión", isRetryable = true)
    object ServerError : OrderProductError("Error del servidor", isRetryable = true)
    data class UnknownError(val originalMessage: String) : OrderProductError(originalMessage)

    companion object {
        fun fromException(exception: Throwable): OrderProductError {
            return when {
                OrderProductErrorHandler.isStockError(exception) -> InsufficientStock
                exception is HttpException -> {
                    when (exception.code()) {
                        404 -> ProductNotFound
                        400 -> InvalidData
                        in 500..599 -> ServerError
                        else -> UnknownError(OrderProductErrorHandler.getErrorMessage(exception))
                    }
                }
                exception is IOException -> NetworkError
                else -> UnknownError(OrderProductErrorHandler.getErrorMessage(exception))
            }
        }
    }
}

/**
 * Extensión para manejo fácil de errores en ViewModels.
 */
fun Throwable.toOrderProductError(): OrderProductError {
    return OrderProductError.fromException(this)
}