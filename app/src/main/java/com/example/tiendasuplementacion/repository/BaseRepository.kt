package com.example.tiendasuplementacion.repository

import com.example.tiendasuplementacion.network.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

abstract class BaseRepository {
    suspend fun <T> safeApiCall(
        apiCall: suspend () -> T
    ): NetworkResult<T> {
        return withContext(Dispatchers.IO) {
            try {
                NetworkResult.success(apiCall.invoke())
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