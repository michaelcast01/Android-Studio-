package com.example.tiendasuplementacion.network
sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val exception: Exception) : NetworkResult<Nothing>()
    object Loading : NetworkResult<Nothing>()

    companion object {
        fun <T> success(data: T) = Success(data)
        fun error(exception: Exception) = Error(exception)
        fun loading() = Loading
    }
} 