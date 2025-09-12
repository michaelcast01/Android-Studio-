package com.example.tiendasuplementacion.repository

import com.example.tiendasuplementacion.network.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay

abstract class BaseRepository {
    // retryNonBlocking: intenta ejecutar una llamada suspendible varias veces con backoff exponencial
    private suspend fun <T> retryNonBlocking(
        times: Int = 3,
        initialDelayMillis: Long = 500,
        maxDelayMillis: Long = 2000,
        factor: Double = 2.0,
        block: suspend () -> T
    ): T {
        var currentDelay = initialDelayMillis
        var attempt = 0
        while (true) {
            try {
                return block()
            } catch (e: Throwable) {
                attempt++
                if (attempt >= times) throw e
                delay(currentDelay)
                currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelayMillis)
            }
        }
    }

    suspend fun <T> safeApiCall(
        apiCall: suspend () -> T
    ): NetworkResult<T> {
        return withContext(Dispatchers.IO) {
            try {
                val result = retryNonBlocking { apiCall.invoke() }
                NetworkResult.success(result)
            } catch (throwable: Throwable) {
                when (throwable) {
                    is java.net.SocketTimeoutException -> {
                        NetworkResult.error(Exception("Error de conexión: Tiempo de espera agotado. Por favor, verifica tu conexión a internet."))
                    }
                    is java.net.UnknownHostException -> {
                        NetworkResult.error(Exception("Error de conexión: No se pudo conectar al servidor. Verifica que el servidor esté en ejecución."))
                    }
                    else -> {
                        NetworkResult.error(Exception("Error: ${throwable.localizedMessage}"))
                    }
                }
            }
        }
    }
} 